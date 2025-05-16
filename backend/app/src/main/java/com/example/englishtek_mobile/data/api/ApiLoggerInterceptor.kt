package com.example.englishtek_mobile.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Pretty Logger Interceptor with emojis for better readability in Logcat
 */
class ApiLoggerInterceptor : Interceptor {
    companion object {
        private const val TAG = "ApiLoggerInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestTime = System.currentTimeMillis()
        
        // Log request
        Log.d(TAG, "uD83DuDE80 Intercepting request: ${request.method} ${request.url}")
        ApiLogger.logRequest(request)
        
        // Proceed with the request
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val errorTime = System.currentTimeMillis() - requestTime
            Log.e(TAG, "uD83DuDCA5 Network error occurred: ${e.message}")
            ApiLogger.logNetworkError(request, e, errorTime)
            throw e
        }
        
        // Log response
        val responseTime = System.currentTimeMillis() - requestTime
        Log.d(TAG, "uD83DuDCAF Response received in ${responseTime}ms with code ${response.code}")
        ApiLogger.logResponse(response, responseTime)
        
        return response
    }
}
