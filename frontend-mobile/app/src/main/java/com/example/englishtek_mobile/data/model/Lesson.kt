package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a learning lesson within a chapter
 */
data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val content: List<String>,  // Updated to match React Native implementation
    val duration: Int,          // Reading time in minutes
    val difficulty: Int,        // 1-5 scale
    val order: Int,
    val chapterId: String,
    @SerializedName(value = "isCompleted", alternate = ["completed"])
    val isCompleted: Boolean,
    val pdfUrl: String? = null  // Optional PDF download URL
)
