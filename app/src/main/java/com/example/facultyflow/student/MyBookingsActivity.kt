package com.example.facultyflow.student

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        // Set initial filter state
        binding.tabAll.setBackgroundColor(getColor(R.color.ios_blue))
        binding.tabAll.setTextColor(getColor(R.color.white))
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
            tab.setBackgroundColor(getColor(android.R.color.transparent))
            tab.setTextColor(getColor(R.color.apple_secondary_label))
        }

        selectedTab.setBackgroundColor(getColor(R.color.ios_blue))
        selectedTab.setTextColor(getColor(R.color.white))

        currentFilter = filter
        fetchBookings()
    }

    private fun fetchBookings() {
        val studentId = auth.currentUser?.uid ?: return

        var query = db.collection("bookings")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        if (currentFilter != "all") {
            query = query.whereEqualTo("status", currentFilter)
        }

        query.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, "Error fetching bookings: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val bookings = mutableListOf<Booking>()
            value?.forEach { doc ->
                // Map Firestore doc to Booking model
                val booking = Booking(
                    id = doc.id,
                    facultyName = doc.getString("facultyName") ?: "Faculty",
                    facultyDesignation = doc.getString("facultyDesignation") ?: "",
                    bookingTime = "${doc.getString("date")}, ${doc.getString("timeSlot")}",
                    status = doc.getString("status") ?: "pending",
                    studentNote = doc.getString("studentNote") ?: "",
                    facultyReply = doc.getString("facultyReply")
                )
                bookings.add(booking)
            }

            bookingsAdapter.submitList(bookings)

            if (bookings.isEmpty()) {
                binding.rvBookings.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.rvBookings.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }
    }
}
