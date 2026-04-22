package com.example.facultyflow.faculty

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facultyflow.databinding.ActivityBookingInboxBinding
import com.example.facultyflow.faculty.adapters.BookingRequestAdapter
import com.example.facultyflow.faculty.models.BookingRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class BookingInboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingInboxBinding
    private lateinit var bookingAdapter: BookingRequestAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupUI()
        setupBookingsList()
        fetchBookings()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupBookingsList() {
        bookingAdapter = BookingRequestAdapter(
            onAccept = { booking -> updateBookingStatus(booking.id, "confirmed") },
            onDecline = { booking -> updateBookingStatus(booking.id, "declined") }
        )
        binding.rvBookings.apply {
            layoutManager = LinearLayoutManager(this@BookingInboxActivity)
            adapter = bookingAdapter
        }
    }

    private fun fetchBookings() {
        val facultyId = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("facultyId", facultyId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error fetching bookings: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val bookings = mutableListOf<BookingRequest>()
                value?.forEach { doc ->
                    val booking = doc.toObject(BookingRequest::class.java).copy(id = doc.id)
                    // Only show pending requests in the inbox
                    if (booking.status == "pending") {
                        bookings.add(booking)
                    }
                }

                bookingAdapter.submitList(bookings)

                if (bookings.isEmpty()) {
                    binding.rvBookings.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.rvBookings.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                }
            }
    }

    private fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
