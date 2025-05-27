package com.example.englishtek_mobile.data.api

import com.example.englishtek_mobile.BuildConfig

/**
 * Configuration class for API endpoints
 */
object ApiConfig {
    // Base URL for the API from BuildConfig
    val BASE_URL = BuildConfig.API_BASE_URL
    
    // Auth endpoints
    const val LOGIN_ENDPOINT = "api/v1/auth/login"
    const val REGISTER_ENDPOINT = "api/v1/auth/register"
    
    // User endpoints
    const val PROFILE_ENDPOINT = "api/v1/users/me"
    const val RECENT_ACTIVITIES_ENDPOINT = "api/v1/activity-log/short"
    
    // Chapter endpoints
    const val CHAPTER_LIST_ENDPOINT = "api/v1/chapters"
    const val CHAPTER_DETAIL_ENDPOINT = "api/v1/chapters/{chapterId}"
    
    // Lesson endpoints
    const val LESSON_DETAIL_ENDPOINT = "api/v1/lessons/{lessonId}"
    const val LESSON_COMPLETE_ENDPOINT = "api/v1/lessons/{lessonId}/complete"
    
    // Quiz endpoints
    const val QUIZ_DETAIL_ENDPOINT = "api/v1/quizzes/{quizId}"
    const val QUIZ_SUBMIT_ENDPOINT = "api/v1/quizzes/{quizId}/submit"
    
    // Badge endpoints
    const val BADGE_LIST_ENDPOINT = "api/v1/badges"
    const val USER_BADGES_ENDPOINT = "api/v1/badges/my"
    const val BADGE_BY_ID_ENDPOINT = "api/v1/badges/{id}"
    
    // Feedback endpoint
    const val SUBMIT_FEEDBACK_ENDPOINT = "api/v1/feedbacks/submit"
    
    // PDF endpoints
    const val LESSON_PDF_ENDPOINT = "api/v1/lessons/{id}/pdf"
}
