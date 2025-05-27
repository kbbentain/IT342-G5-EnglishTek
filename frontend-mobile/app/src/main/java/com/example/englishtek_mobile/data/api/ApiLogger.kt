package com.example.englishtek_mobile.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Request
import okhttp3.Response

/**
 * API Logger utility class for pretty-printing API requests and responses with emojis
 */
object ApiLogger {
    private const val TAG = "ApiLogger"
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    /**
     * Log an API request with emojis and formatted output
     */
    fun logRequest(request: Request) {
        val method = request.method
        val url = request.url
        val headers = request.headers
        val hasBody = request.body != null
        
        val logBuilder = StringBuilder()
        logBuilder.append("\n")
        logBuilder.append("🚀 REQUEST STARTED ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        logBuilder.append("📡 URL: $url\n")
        logBuilder.append("📝 METHOD: $method\n")
        
        // Log headers
        if (headers.size > 0) {
            logBuilder.append("📋 HEADERS:\n")
            for (i in 0 until headers.size) {
                logBuilder.append("   ${headers.name(i)}: ${headers.value(i)}\n")
            }
        }
        
        // Log request body if exists
        if (hasBody) {
            logBuilder.append("📦 BODY: [Request body not logged for security]\n")
        }
        
        logBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        
        Log.d(TAG, logBuilder.toString())
    }
    
    /**
     * Log an API response with emojis and formatted output
     */
    fun logResponse(response: Response, timeMs: Long) {
        val request = response.request
        val url = request.url
        val statusCode = response.code
        val isSuccessful = response.isSuccessful
        val responseBody = response.peekBody(Long.MAX_VALUE).string()
        
        val logBuilder = StringBuilder()
        logBuilder.append("\n")
        
        // Choose emoji based on status code
        val statusEmoji = when {
            statusCode < 300 -> "✅"
            statusCode < 400 -> "⚠️"
            else -> "❌"
        }
        
        logBuilder.append("$statusEmoji RESPONSE RECEIVED ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        logBuilder.append("📡 URL: $url\n")
        logBuilder.append("⏱️ TIME: ${timeMs}ms\n")
        logBuilder.append("📊 STATUS: $statusCode (${if (isSuccessful) "Success" else "Error"})\n")
        
        // Log response body
        if (responseBody.isNotEmpty()) {
            logBuilder.append("📄 BODY:\n")
            try {
                // Try to parse and pretty print JSON
                val json = JsonParser.parseString(responseBody)
                val prettyJson = gson.toJson(json)
                logBuilder.append(prettyJson)
            } catch (e: Exception) {
                // If not valid JSON, print as is
                logBuilder.append(responseBody)
            }
            logBuilder.append("\n")
        } else {
            logBuilder.append("📄 BODY: [Empty]\n")
        }
        
        logBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        
        if (isSuccessful) {
            Log.d(TAG, logBuilder.toString())
        } else {
            Log.e(TAG, logBuilder.toString())
        }
    }
    
    /**
     * Log a network error with emojis and formatted output
     */
    fun logNetworkError(request: Request, error: Exception, timeMs: Long) {
        val url = request.url
        val method = request.method
        
        val logBuilder = StringBuilder()
        logBuilder.append("\n")
        logBuilder.append("❌ NETWORK ERROR ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        logBuilder.append("📡 URL: $url\n")
        logBuilder.append("📝 METHOD: $method\n")
        logBuilder.append("⏱️ TIME: ${timeMs}ms\n")
        logBuilder.append("💥 ERROR: ${error.javaClass.simpleName}\n")
        logBuilder.append("📝 MESSAGE: ${error.message}\n")
        logBuilder.append("📚 STACK TRACE:\n")
        error.stackTrace.take(5).forEach { stackTraceElement ->
            logBuilder.append("   at $stackTraceElement\n")
        }
        logBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        
        Log.e(TAG, logBuilder.toString())
    }
}
