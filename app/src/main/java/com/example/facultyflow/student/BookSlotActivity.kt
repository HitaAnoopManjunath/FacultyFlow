package com.example.facultyflow.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.facultyflow.databinding.ActivityBookSlotBinding
import com.example.facultyflow.databinding.ItemDateChipBinding
import com.example.facultyflow.student.adapters.TimeSlotAdapter
import com.example.facultyflow.student.models.TimeSlot
import java.text.SimpleDateFormat
import java.util.*
import com.example.facultyflow.R
import com.example.facultyflow.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookSlotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookSlotBinding
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private var selectedTimeSlot: TimeSlot? = null
    private var selectedDate: String = ""
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var preferencesManager: PreferencesManager

    private var facultyId: String = ""
    private var facultyName: String = ""
    private var facultyDesignation: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        preferencesManager = PreferencesManager(this)

        facultyId = intent.getStringExtra("faculty_id") ?: ""
        // In a real app, we'd fetch faculty details from DB if only ID is passed
        // or pass all info via intent for speed.
        facultyName = intent.getStringExtra("faculty_name") ?: "Dr. John Smith"
        facultyDesignation = intent.getStringExtra("faculty_designation") ?: "Professor"

        setupUI()
        setupDateChips()
        setupTimeSlots()
        setupClickListeners()
        loadFacultyData()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupDateChips() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        for (i in 0..6) {
            val dateChipBinding = ItemDateChipBinding.inflate(LayoutInflater.from(this))
            
            val date = calendar.time
            val formattedDate = dayFormat.format(date)
            dateChipBinding.tvDay.text = dateFormat.format(date).uppercase()
            dateChipBinding.tvDate.text = SimpleDateFormat("d", Locale.getDefault()).format(date)

            dateChipBinding.root.setOnClickListener {
                selectDate(dateChipBinding, formattedDate)
            }

            if (i == 0) {
                selectDate(dateChipBinding, formattedDate)
            }

            binding.dateContainer.addView(dateChipBinding.root)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun selectDate(dateChipBinding: ItemDateChipBinding, date: String) {
        for (i in 0 until binding.dateContainer.childCount) {
            val child = binding.dateContainer.getChildAt(i)
            if (child is androidx.cardview.widget.CardView) {
                child.setCardBackgroundColor(getColor(R.color.ios_gray6))
                val dayText = child.findViewById<android.widget.TextView>(R.id.tvDay)
                val dateText = child.findViewById<android.widget.TextView>(R.id.tvDate)
                dayText.setTextColor(getColor(R.color.apple_secondary_label))
                dateText.setTextColor(getColor(R.color.apple_label))
            }
        }

        dateChipBinding.root.setCardBackgroundColor(getColor(R.color.ios_blue))
        dateChipBinding.tvDay.setTextColor(getColor(R.color.white))
        dateChipBinding.tvDate.setTextColor(getColor(R.color.white))

        selectedDate = date
        loadTimeSlotsForDate()
    }

    private fun setupTimeSlots() {
        timeSlotAdapter = TimeSlotAdapter { timeSlot ->
            selectTimeSlot(timeSlot)
        }

        binding.rvTimeSlots.apply {
            layoutManager = GridLayoutManager(this@BookSlotActivity, 3)
            adapter = timeSlotAdapter
        }

        loadTimeSlotsForDate()
    }

    private fun loadTimeSlotsForDate() {
        val timeSlots = listOf(
            TimeSlot("9:00 AM", "30 min"),
            TimeSlot("9:30 AM", "30 min"),
            TimeSlot("10:00 AM", "30 min"),
            TimeSlot("10:30 AM", "30 min"),
            TimeSlot("11:00 AM", "30 min"),
            TimeSlot("11:30 AM", "30 min")
        )
        timeSlotAdapter.submitList(timeSlots)
    }

    private fun selectTimeSlot(timeSlot: TimeSlot) {
        selectedTimeSlot = timeSlot
        binding.tvSelectedTime.visibility = View.VISIBLE
        binding.tvSelectedTime.text = "Slot selected: ${timeSlot.time}"
        binding.btnConfirmBooking.isEnabled = true
    }

    private fun setupClickListeners() {
        binding.btnConfirmBooking.setOnClickListener {
            confirmBooking()
        }
    }

    private fun loadFacultyData() {
        binding.tvFacultyName.text = facultyName
        binding.tvFacultyDesignation.text = facultyDesignation
    }

    private fun confirmBooking() {
        val slot = selectedTimeSlot ?: return
        val studentId = auth.currentUser?.uid ?: return
        val studentName = preferencesManager.userName
        val note = binding.etNote.text.toString().trim()

        binding.btnConfirmBooking.isEnabled = false
        binding.btnConfirmBooking.text = "Sending Request..."

        val bookingData = hashMapOf(
            "studentId" to studentId,
            "studentName" to studentName,
            "facultyId" to facultyId,
            "facultyName" to facultyName,
            "facultyDesignation" to facultyDesignation,
            "date" to selectedDate,
            "timeSlot" to slot.time,
            "status" to "pending",
            "studentNote" to note,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("bookings")
            .add(bookingData)
            .addOnSuccessListener {
                showSuccessAnimation()
            }
            .addOnFailureListener { e ->
                binding.btnConfirmBooking.isEnabled = true
                binding.btnConfirmBooking.text = "Confirm Booking"
                Toast.makeText(this, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSuccessAnimation() {
        binding.layoutSuccess.successOverlay.visibility = View.VISIBLE
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        binding.layoutSuccess.successCard.startAnimation(scaleUp)

        binding.root.postDelayed({
            finish()
        }, 2000)
    }
}
