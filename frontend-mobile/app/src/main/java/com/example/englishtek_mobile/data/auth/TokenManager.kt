package com.example.englishtek_mobile.data.auth

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Manages the JWT token for authentication
 */
class TokenManager(private val context: Context) {
    private val TAG = "TokenManager"
    
    companion object {
        private val Context.dataStore by preferencesDataStore("auth_prefs")
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    /**
     * Save the JWT token to DataStore
     */
    suspend fun saveToken(token: String) {
        Log.d(TAG, "ud83dudd10 Saving authentication token")
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    /**
     * Get the JWT token as a Flow
     */
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    /**
     * Clear the JWT token (for logout)
     */
    suspend fun clearToken() {
        Log.d(TAG, "ud83dudd13 Clearing authentication token")
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
    
    /**
     * Check if user is authenticated (has a valid token)
     */
    fun isAuthenticated(): Boolean {
        return runBlocking {
            val token = getToken().first()
            val hasToken = !token.isNullOrEmpty()
            Log.d(TAG, if (hasToken) "ud83dudd11 User is authenticated" else "ud83dudd12 User is not authenticated")
            hasToken
        }
    }
}
