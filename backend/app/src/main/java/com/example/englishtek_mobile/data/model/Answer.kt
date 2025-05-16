package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents an answer option for a task
 */
data class Answer(
    val id: String,
    val text: String,
    @SerializedName("isCorrect")
    val isCorrect: Boolean = false,
    val explanation: String? = null
)
