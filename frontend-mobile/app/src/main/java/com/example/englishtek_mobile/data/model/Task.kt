package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a learning task within a lesson
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val type: String = "QUIZ",
    val order: Int = 0,
    val lessonId: String = "",
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false,
    val answers: List<Answer>? = null
)
