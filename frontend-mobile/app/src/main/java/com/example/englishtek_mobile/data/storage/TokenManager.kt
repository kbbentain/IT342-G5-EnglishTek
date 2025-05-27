package com.example.englishtek_mobile.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    
    private var token: String? = null
    private lateinit var encryptedPrefs: EncryptedSharedPreferences
    
    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
        
        // Load token from storage if available
        token = encryptedPrefs.getString(KEY_TOKEN, null)
    }
    
    fun saveToken(newToken: String) {
        token = newToken
        encryptedPrefs.edit().putString(KEY_TOKEN, newToken).apply()
    }
    
    fun getToken(): String? = token
    
    fun clearToken() {
        token = null
        encryptedPrefs.edit().remove(KEY_TOKEN).apply()
    }
    
    fun isAuthenticated(): Boolean = !token.isNullOrEmpty()
}
