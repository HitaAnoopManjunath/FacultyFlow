package com.example.facultyflow.faculty.models

data class BookingRequest(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val requestedTime: String = "",
    val note: String = "",
    val status: String = "pending" // "pending", "accepted", "declined"
)
