package com.example.englishtek_mobile.data.repository

import android.util.Log
import com.example.englishtek_mobile.data.api.ApiService
import com.example.englishtek_mobile.data.model.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for handling chapter-related data operations
 */
class ChapterRepository(private val apiService: ApiService) {
    private val TAG = "ChapterRepository"

    /**
     * Fetch all chapters from the API
     */
    suspend fun getChapters(): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching chapters from API")
            val response = apiService.getChapters()
            
            if (response.isSuccessful) {
                val chapters = response.body() ?: emptyList()
                Log.d(TAG, "✅ Successfully fetched ${chapters.size} chapters")
                Result.success(chapters)
            } else {
                Log.e(TAG, "❌ Failed to fetch chapters: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to fetch chapters: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching chapters", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch a specific chapter by ID
     */
    suspend fun getChapterDetail(chapterId: String): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Fetching chapter detail for ID: $chapterId")
            val response = apiService.getChapterDetail(chapterId)
            
            if (response.isSuccessful) {
                val chapter = response.body()
                if (chapter != null) {
                    Log.d(TAG, "✅ Successfully fetched chapter: ${chapter.title}")
                    Result.success(chapter)
                } else {
                    Log.e(TAG, "❌ Chapter detail response body was null")
                    Result.failure(Exception("Chapter detail response body was null"))
                }
            } else {
                Log.e(TAG, "❌ Failed to fetch chapter detail: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to fetch chapter detail: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching chapter detail", e)
            Result.failure(e)
        }
    }
}
