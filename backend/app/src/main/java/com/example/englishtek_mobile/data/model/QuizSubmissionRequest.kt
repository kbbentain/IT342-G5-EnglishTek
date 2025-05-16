package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a request to submit a completed quiz
 * Maps to the API's QuizSubmissionRequest schema
 */
data class QuizSubmissionRequest(
    /**
     * The score achieved in the quiz (0-100)
     */
    @SerializedName("score")
    val score: Int,
    
    /**
     * Optional map of question IDs to selected answers
     * Not required by the API but useful for tracking user selections
     */
    @Transient
    val answers: Map<String, String>? = null
)
