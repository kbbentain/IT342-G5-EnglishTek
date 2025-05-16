package com.example.englishtek_mobile

import android.app.Application
import android.util.Log
import com.example.englishtek_mobile.data.storage.TokenManager

/**
 * Application class for EnglishTek Mobile
 * Initializes app-wide components and services
 */
class EnglishTekApplication : Application() {
    companion object {
        private const val TAG = "EnglishTekApp"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize TokenManager
        TokenManager.init(applicationContext)
        
        Log.d(TAG, "🚀 EnglishTek Mobile Application initialized")
    }
}
