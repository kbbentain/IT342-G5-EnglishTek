package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the result of a submitted quiz
 */
data class QuizResult(
    @SerializedName("score")
    val score: Int,
    
    @SerializedName("maxScore")
    val maxScore: Int,
    
    @SerializedName("isEligibleForRetake")
    val isEligibleForRetake: Boolean,
    
    @SerializedName("isEligibleForBadge")
    val isEligibleForBadge: Boolean,
    
    @SerializedName("badge")
    val badge: Badge? = null,
    
    @SerializedName("badgeAwarded")
    val badgeAwarded: Boolean = false
)
