package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

data class Badge(
    val id: String,
    val name: String?,
    val description: String?,
    @SerializedName("level")
    val level: String?,
    @SerializedName("iconUrl")
    val iconUrl: String?,
    @SerializedName("dateObtained")
    val dateObtained: String? = null
)
