package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents feedback data for a chapter
 */
data class FeedbackRequest(
    @SerializedName("chapterId")
    val chapterId: String,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("feedbackKeyword")
    val feedbackKeyword: String,
    
    @SerializedName("feedbackText")
    val feedbackText: String
)

data class FeedbackResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("chapterId")
    val chapterId: Int,
    
    @SerializedName("chapterTitle")
    val chapterTitle: String,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("feedbackKeyword")
    val feedbackKeyword: String,
    
    @SerializedName("feedbackText")
    val feedbackText: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)
