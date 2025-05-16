package com.example.englishtek_mobile.data.repository

import com.example.englishtek_mobile.data.api.ApiService
import com.example.englishtek_mobile.data.model.Quiz
import com.example.englishtek_mobile.data.model.QuizResult
import com.example.englishtek_mobile.data.model.QuizSubmissionRequest

/**
 * Repository for handling quiz-related operations
 */
class QuizRepository(private val apiService: ApiService) {
    
    /**
     * Get quiz details by ID
     */
    suspend fun getQuiz(quizId: String): Result<Quiz> {
        return try {
            val response = apiService.getQuiz(quizId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch quiz: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark a quiz as started
     */
    suspend fun startQuiz(quizId: String): Result<Unit> {
        return try {
            val response = apiService.startQuiz(quizId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to start quiz: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submit quiz answers and get results
     * @param quizId The ID of the quiz
     * @param answers Map of question IDs to selected answers
     * @param score The calculated score for the quiz
     */
    suspend fun submitQuiz(quizId: String, answers: Map<String, String>, score: Int): Result<QuizResult> {
        return try {
            // Create a submission request with score and answers
            val submissionRequest = QuizSubmissionRequest(
                score = score,
                answers = answers
            )
            
            val response = apiService.submitQuiz(quizId, submissionRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit quiz: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
