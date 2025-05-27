package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a learning chapter containing lessons and quizzes
 */
data class Chapter(
    val id: String,
    val title: String?,
    val description: String?,
    @SerializedName(value = "icon", alternate = ["iconUrl"])
    val icon: String?,
    val order: Int = 0,
    val status: ChapterStatus,
    @SerializedName("completedTasks")
    val completedTasks: Int,
    @SerializedName("totalTasks")
    val totalTasks: Int,
    @SerializedName("progressPercentage")
    val progressPercentage: Float = 0f,
    @SerializedName("items")
    val items: List<ChapterItem>? = null,
    @SerializedName("hasCompletedFeedback")
    val hasCompletedFeedback: Boolean = false,
    // Keep these for backward compatibility
    val lessons: List<Lesson>? = null,
    val quizzes: List<Quiz>? = null
)

enum class ChapterStatus(val displayName: String) {
    @SerializedName("AVAILABLE")
    AVAILABLE("Available"),
    
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS("In Progress"),
    
    @SerializedName("COMPLETED")
    COMPLETED("Completed"),
    
    @SerializedName("LOCKED")
    LOCKED("Locked")
}
