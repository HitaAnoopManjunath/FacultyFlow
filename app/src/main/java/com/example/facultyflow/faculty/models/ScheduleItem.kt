package com.example.facultyflow.faculty.models

data class ScheduleItem(
    val startTime: String,
    val endTime: String,
    val className: String,
    val classRoom: String,
    val status: String,
    val statusColor: String // "green", "amber", "grey"
)
