package com.example.englishtek_mobile.data.repository

import com.example.englishtek_mobile.data.api.ApiService
import com.example.englishtek_mobile.data.model.Badge

/**
 * Repository for handling badge-related operations
 */
class BadgeRepository(private val apiService: ApiService) {
    
    /**
     * Get all available badges
     */
    suspend fun getAllBadges(): Result<List<Badge>> {
        return try {
            val response = apiService.getAllBadges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch badges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get badges earned by the current user
     */
    suspend fun getUserBadges(): Result<List<Badge>> {
        return try {
            val response = apiService.getMyBadges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user badges: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific badge by ID
     */
    suspend fun getBadgeById(badgeId: String): Result<Badge> {
        return try {
            val response = apiService.getBadgeById(badgeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch badge: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
