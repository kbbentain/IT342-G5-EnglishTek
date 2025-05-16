package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

data class ActivityLog(
    val type: String,  // "lesson", "quiz", "badge"
    
    val name: String,
    
    @SerializedName("date")
    val date: String
)
