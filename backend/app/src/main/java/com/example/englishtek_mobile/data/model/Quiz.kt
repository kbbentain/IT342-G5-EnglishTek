package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a quiz within a chapter
 */
data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: Int,        // 1-5 scale
    @SerializedName("numberOfItems")
    val questionCount: Int,     // Number of questions
    @SerializedName("maxScore")
    val maxScore: Int,          // Maximum possible score
    val badgeId: String?,       // ID of the badge awarded for completing this quiz
    val order: Int,
    val chapterId: String,
    @SerializedName(value = "isCompleted", alternate = ["completed"])
    val isCompleted: Boolean,
    val questions: List<QuizQuestion>? = null
)

/**
 * Represents a quiz question with choices and correct answer
 */
data class QuizQuestion(
    val id: String?,
    @SerializedName("title")
    val text: String?,           // API returns 'title' but we use 'text' in our UI
    val type: String?,           // "multiple_choice" or "identification"
    val choices: List<String>?,
    @SerializedName("correct_answer")
    val correctAnswer: Any?,      // Can be String or List<String> from API
    val quizId: String?,
    val page: Int?
)
