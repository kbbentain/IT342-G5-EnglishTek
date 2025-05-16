package com.example.englishtek_mobile.data.model

import com.example.englishtek_mobile.data.model.User

data class AuthResponse(
    val token: String,
    val user: User?
)
