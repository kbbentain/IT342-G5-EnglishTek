package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String? = null,
    val username: String,
    val email: String,
    val name: String,
    val role: String? = "USER",
    val avatarUrl: String? = null,
    val bio: String? = null,
    @SerializedName("totalCompletedTasks")
    val totalCompletedTasks: Int = 0,
    @SerializedName("totalBadges")
    val totalBadges: Int = 0,
    @SerializedName("completedLessons")
    val completedLessons: Int = 0,
    @SerializedName("completedQuizzes")
    val completedQuizzes: Int = 0,
    @SerializedName("badges")
    val badges: Int = 0
)
