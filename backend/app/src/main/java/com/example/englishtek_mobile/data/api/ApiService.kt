package com.example.englishtek_mobile.data.api

import com.example.englishtek_mobile.data.model.AuthResponse
import com.example.englishtek_mobile.data.model.Chapter
import com.example.englishtek_mobile.data.model.User
import com.example.englishtek_mobile.data.model.ActivityLog
import com.example.englishtek_mobile.data.model.ChangePasswordRequest
import com.example.englishtek_mobile.data.model.Badge
import com.example.englishtek_mobile.data.model.Lesson
import com.example.englishtek_mobile.data.model.Quiz
import com.example.englishtek_mobile.data.model.QuizResult
import com.example.englishtek_mobile.data.model.QuizSubmissionRequest
import com.example.englishtek_mobile.data.model.FeedbackRequest
import com.example.englishtek_mobile.data.model.FeedbackResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST(ApiConfig.LOGIN_ENDPOINT)
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): Response<AuthResponse>

    @POST(ApiConfig.REGISTER_ENDPOINT)
    suspend fun register(
        @Body registerRequest: Map<String, String>
    ): Response<AuthResponse>
    
    @Multipart
    @POST(ApiConfig.REGISTER_ENDPOINT)
    suspend fun registerWithAvatar(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Response<AuthResponse>

    @GET(ApiConfig.PROFILE_ENDPOINT)
    suspend fun getUserProfile(): Response<User>
    
    @GET(ApiConfig.RECENT_ACTIVITIES_ENDPOINT)
    suspend fun getRecentActivities(): Response<List<ActivityLog>>
    
    @GET(ApiConfig.CHAPTER_LIST_ENDPOINT)
    suspend fun getChapters(): Response<List<Chapter>>
    
    @GET(ApiConfig.CHAPTER_DETAIL_ENDPOINT)
    suspend fun getChapterDetail(
        @Path("chapterId") chapterId: String
    ): Response<Chapter>

    // Badge endpoints
    @GET(ApiConfig.BADGE_LIST_ENDPOINT)
    suspend fun getAllBadges(): Response<List<Badge>>
    
    @GET(ApiConfig.USER_BADGES_ENDPOINT)
    suspend fun getMyBadges(): Response<List<Badge>>
    
    @GET(ApiConfig.BADGE_BY_ID_ENDPOINT)
    suspend fun getBadgeById(@Path("id") badgeId: String): Response<Badge>

    // Change Password
    @POST("api/v1/users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    // Update Profile
    @Multipart
    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part avatar: MultipartBody.Part?,
        @Part("presetAvatar") presetAvatar: RequestBody?
    ): Response<User>
    
    // Delete User
    @DELETE("api/v1/users/me")
    suspend fun deleteUser(): Response<Unit>
    
    // Lesson endpoints
    @GET("api/v1/lessons/{lessonId}")
    suspend fun getLesson(
        @Path("lessonId") lessonId: String
    ): Response<Lesson>
    
    @POST("api/v1/lessons/{lessonId}/start")
    suspend fun startLesson(
        @Path("lessonId") lessonId: String
    ): Response<Unit>
    
    @POST("api/v1/lessons/{lessonId}/finish")
    suspend fun finishLesson(
        @Path("lessonId") lessonId: String
    ): Response<Unit>
    
    // Quiz endpoints
    @GET("api/v1/quizzes/{quizId}")
    suspend fun getQuiz(
        @Path("quizId") quizId: String
    ): Response<Quiz>
    
    @POST("api/v1/quizzes/{quizId}/start")
    suspend fun startQuiz(
        @Path("quizId") quizId: String
    ): Response<Unit>
    
    @POST("api/v1/quizzes/{quizId}/submit")
    suspend fun submitQuiz(
        @Path("quizId") quizId: String,
        @Body request: QuizSubmissionRequest
    ): Response<QuizResult>
    
    // Feedback endpoint
    @POST(ApiConfig.SUBMIT_FEEDBACK_ENDPOINT)
    suspend fun submitFeedback(
        @Body request: FeedbackRequest
    ): Response<FeedbackResponse>
    
    // PDF download endpoints
    @GET(ApiConfig.LESSON_PDF_ENDPOINT)
    @Streaming
    suspend fun downloadLessonPdf(
        @Path("id") lessonId: String
    ): Response<ResponseBody>
}
