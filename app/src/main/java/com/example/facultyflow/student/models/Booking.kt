package com.example.facultyflow.student.models

data class Booking(
    val id: String,
    val facultyName: String,
    val facultyDesignation: String,
    val bookingTime: String,
    val status: String, // "pending", "confirmed", "done"
    val studentNote: String,
    val facultyReply: String?
)
