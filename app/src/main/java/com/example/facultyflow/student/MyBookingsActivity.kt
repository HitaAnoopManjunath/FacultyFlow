package com.example.facultyflow.student

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facultyflow.databinding.ActivityMyBookingsBinding
import com.example.facultyflow.student.adapters.MyBookingsAdapter
import com.example.facultyflow.student.models.Booking
import com.example.facultyflow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var bookingsAdapter: MyBookingsAdapter
    private var currentFilter = "all"
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var allBookings = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupUI()
        setupBookingsList()
        setupFilters()
        fetchBookings()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupBookingsList() {
        bookingsAdapter = MyBookingsAdapter()
        binding.rvBookings.apply {
            layoutManager = LinearLayoutManager(this@MyBookingsActivity)
            adapter = bookingsAdapter
        }
    }

    private fun setupFilters() {
        binding.tabAll.setOnClickListener { selectTab(binding.tabAll, "all") }
        binding.tabPending.setOnClickListener { selectTab(binding.tabPending, "pending") }
        binding.tabConfirmed.setOnClickListener { selectTab(binding.tabConfirmed, "confirmed") }
        binding.tabDone.setOnClickListener { selectTab(binding.tabDone, "done") }
    }

    private fun selectTab(selectedTab: android.widget.TextView, filter: String) {
        listOf(binding.tabAll, binding.tabPending, binding.tabConfirmed, binding.tabDone).forEach { tab ->
            tab.background = null
            tab.setTextColor(ContextCompat.getColor(this, R.color.apple_secondary_label))
        }

        selectedTab.setBackgroundResource(R.drawable.segmented_control_selected)
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.apple_label))

        currentFilter = filter
        applyFilter()
    }

    private fun fetchBookings() {
        val studentId = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    if (error.message?.contains("index") == true) {
                        fetchBookingsWithoutOrder(studentId)
                    } else {
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }

                allBookings.clear()
                value?.forEach { doc ->
                    val booking = Booking(
                        id = doc.id,
                        facultyName = doc.getString("facultyName") ?: "Faculty",
                        facultyDesignation = doc.getString("facultyDesignation") ?: "",
                        bookingTime = "${doc.getString("date")}, ${doc.getString("timeSlot")}",
                        status = doc.getString("status") ?: "pending",
                        studentNote = doc.getString("studentNote") ?: "",
                        facultyReply = doc.getString("facultyReply")
                    )
                    allBookings.add(booking)
                }
                applyFilter()
            }
    }

    private fun fetchBookingsWithoutOrder(studentId: String) {
        db.collection("bookings")
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allBookings.clear()
                value?.forEach { doc ->
                    val booking = Booking(
                        id = doc.id,
                        facultyName = doc.getString("facultyName") ?: "Faculty",
                        facultyDesignation = doc.getString("facultyDesignation") ?: "",
                        bookingTime = "${doc.getString("date")}, ${doc.getString("timeSlot")}",
                        status = doc.getString("status") ?: "pending",
                        studentNote = doc.getString("studentNote") ?: "",
                        facultyReply = doc.getString("facultyReply")
                    )
                    allBookings.add(booking)
                }
                applyFilter()
            }
    }

    private fun applyFilter() {
        val filteredList = when (currentFilter) {
            "all" -> allBookings
            "done" -> allBookings.filter { it.status == "confirmed" || it.status == "declined" }
            else -> allBookings.filter { it.status == currentFilter }
        }

        bookingsAdapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            binding.rvBookings.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvBookings.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }
}
