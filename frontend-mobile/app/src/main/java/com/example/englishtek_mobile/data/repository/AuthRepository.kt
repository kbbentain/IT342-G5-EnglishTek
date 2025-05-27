package com.example.englishtek_mobile.data.repository

import android.util.Log
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.auth.TokenManager
import com.example.englishtek_mobile.data.model.AuthResponse
import com.example.englishtek_mobile.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Repository for handling authentication-related data operations
 */
class AuthRepository(private val apiClient: ApiClient, private val tokenManager: TokenManager) {
    private val TAG = "AuthRepository"
    private val apiService = apiClient.apiService
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔑 Attempting login for user: $username")
                val response = apiService.login(mapOf(
                    "username" to username,
                    "password" to password
                ))
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save token to DataStore
                        tokenManager.saveToken(authResponse.token)
                        Log.d(TAG, "✅ Login successful, token saved")
                        Result.success(authResponse)
                    } else {
                        Log.e(TAG, "❌ Login failed: User is null")
                        Result.failure(Exception("User is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "{}").optString("error", "Login failed")
                    } catch (e: Exception) {
                        "Login failed"
                    }
                    Log.e(TAG, "❌ Login failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Login exception", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Register a new user
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        name: String
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔑 Attempting registration for user: $username")
                
                // Create multipart form data
                val usernameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
                val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Call the multipart endpoint
                val response = apiService.registerWithAvatar(
                    username = usernameBody,
                    email = emailBody,
                    password = passwordBody,
                    name = nameBody,
                    avatar = null
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // Save token to DataStore
                    authResponse.token?.let { token ->
                        tokenManager.saveToken(token)
                        Log.d(TAG, "✅ Token saved for user: $username")
                    }
                    Log.d(TAG, "✅ Registration successful for user: $username")
                    Result.success(authResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "{}").optString("error", "Registration failed")
                    } catch (e: Exception) {
                        "Registration failed"
                    }
                    Log.e(TAG, "❌ Registration failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Registration exception", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get the current user's profile
     */
    suspend fun getUserProfile(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Fetching user profile")
                val response = apiService.getUserProfile()
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "✅ User profile fetched successfully")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "{}").optString("error", "Failed to get user profile")
                    } catch (e: Exception) {
                        "Failed to get user profile"
                    }
                    Log.e(TAG, "❌ Failed to fetch user profile: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Get user profile exception", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Logout the current user
     */
    suspend fun logout() {
        Log.d(TAG, "👋 Logging out user")
        tokenManager.clearToken()
    }
    
    fun isAuthenticated(): Boolean = tokenManager.isAuthenticated()
}
