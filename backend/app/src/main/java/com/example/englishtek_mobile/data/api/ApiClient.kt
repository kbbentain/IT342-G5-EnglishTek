package com.example.englishtek_mobile.data.api

import android.content.Context
import com.example.englishtek_mobile.data.auth.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton class for creating and managing the Retrofit API client
 */
class ApiClient(private val context: Context) {
    private val tokenManager = TokenManager(context)
    
    // Create an interceptor to add the Authorization header
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Get token from TokenManager (blocking call in interceptor context)
        val token = runBlocking { tokenManager.getToken().first() }
        
        // If token exists, add it to the request header
        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(request)
    }
    
    // Create OkHttpClient with interceptors
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(ApiLoggerInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Create the Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Create the API service
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
