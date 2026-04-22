package com.example.facultyflow.faculty

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facultyflow.databinding.ActivityFacultyHomeBinding
import com.example.facultyflow.faculty.adapters.ScheduleAdapter
import com.example.facultyflow.faculty.models.ScheduleItem
import com.example.facultyflow.R
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FacultyHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacultyHomeBinding
    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesManager: PreferencesManager
    private var isUpdatingFromDb = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacultyHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        preferencesManager = PreferencesManager(this)

        setupUI()
        setupSchedule()
        setupNavigation()
        setupClickListeners()
        fetchFacultyData()
        listenForPendingBookings()
    }

    private fun setupUI() {
        val greeting = when {
            java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) < 12 -> "Good Morning"
            java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        binding.tvGreeting.text = greeting
        binding.tvFacultyName.text = preferencesManager.userName
    }

    private fun fetchFacultyData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val name = snapshot.getString("name") ?: ""
                val availability = snapshot.getString("availability") ?: "green"
                
                binding.tvFacultyName.text = name
                
                isUpdatingFromDb = true
                binding.switchBusy.isChecked = availability == "red"
                isUpdatingFromDb = false

                updateStatusUI(availability == "red")
                
                // Update local pref if name changed
                preferencesManager.userName = name
            }
        }
    }

    private fun updateStatusUI(isBusy: Boolean) {
        val color = if (isBusy) R.color.ios_red else R.color.ios_green
        binding.statusIndicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color))
        binding.switchBusy.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color))
    }

    private fun listenForPendingBookings() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("bookings")
            .whereEqualTo("facultyId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                binding.tvPendingCount.text = count.toString()
            }
    }

    private fun setupSchedule() {
        scheduleAdapter = ScheduleAdapter()
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(this@FacultyHomeActivity)
            adapter = scheduleAdapter
        }

        val scheduleItems = listOf(
            ScheduleItem("09:00 AM", "10:00 AM", "Data Structures", "Room 301, Block A", "In Class", "grey"),
            ScheduleItem("10:00 AM", "11:00 AM", "Free Period", "Office", "Free", "green"),
            ScheduleItem("11:00 AM", "12:00 PM", "Algorithms", "Room 205, Block B", "In Class", "grey"),
            ScheduleItem("02:00 PM", "03:00 PM", "Office Hours", "Office", "Busy", "red")
        )
        scheduleAdapter.submitList(scheduleItems)
    }

    private fun setupNavigation() {
        binding.navHome.setOnClickListener {
            // Already home
        }
        binding.navTimetable.setOnClickListener {
            startActivity(Intent(this, TimetableUploadActivity::class.java))
        }
        binding.navBookings.setOnClickListener {
            startActivity(Intent(this, BookingInboxActivity::class.java))
        }
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditorActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        binding.cardPendingBookings.setOnClickListener {
            startActivity(Intent(this, BookingInboxActivity::class.java))
        }

        binding.switchBusy.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingFromDb) return@setOnCheckedChangeListener
            
            val userId = auth.currentUser?.uid ?: return@setOnCheckedChangeListener
            val newStatus = if (isChecked) "red" else "green"
            
            db.collection("users").document(userId)
                .update("availability", newStatus)
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
                }
        }
        
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditorActivity::class.java))
        }
    }
}
