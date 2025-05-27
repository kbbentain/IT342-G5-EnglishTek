package com.example.englishtek_mobile.data.repository

import com.example.englishtek_mobile.data.api.ApiService
import com.example.englishtek_mobile.data.model.Lesson
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Repository for handling lesson-related operations
 */
class LessonRepository(private val apiService: ApiService) {
    
    /**
     * Get lesson details by ID
     */
    suspend fun getLesson(lessonId: String): Result<Lesson> {
        return try {
            val response = apiService.getLesson(lessonId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch lesson: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark a lesson as started
     */
    suspend fun startLesson(lessonId: String): Result<Unit> {
        return try {
            val response = apiService.startLesson(lessonId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to start lesson: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark a lesson as completed
     */
    suspend fun finishLesson(lessonId: String): Result<Unit> {
        return try {
            val response = apiService.finishLesson(lessonId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to finish lesson: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download lesson as PDF
     * @param lessonId The ID of the lesson to download
     * @param outputFile The file to save the PDF to
     * @param progressCallback Callback for download progress updates
     */
    suspend fun downloadLessonPdf(
        lessonId: String,
        outputFile: File,
        progressCallback: (Float) -> Unit
    ): Result<File> {
        return try {
            val response = apiService.downloadLessonPdf(lessonId)
            if (response.isSuccessful && response.body() != null) {
                // Save the PDF to the output file
                saveResponseBodyToFile(response.body()!!, outputFile, progressCallback)
                Result.success(outputFile)
            } else {
                Result.failure(Exception("Failed to download PDF: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save a response body to a file with progress updates
     */
    private fun saveResponseBodyToFile(
        body: ResponseBody,
        outputFile: File,
        progressCallback: (Float) -> Unit
    ): File {
        val contentLength = body.contentLength()
        var bytesRead = 0L
        
        body.byteStream().use { inputStream: InputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4096)
                var bytes: Int
                
                while (inputStream.read(buffer).also { bytes = it } != -1) {
                    outputStream.write(buffer, 0, bytes)
                    bytesRead += bytes
                    
                    // Update progress
                    if (contentLength > 0) {
                        val progress = bytesRead.toFloat() / contentLength.toFloat()
                        progressCallback(progress)
                    }
                }
                
                outputStream.flush()
            }
        }
        
        return outputFile
    }
}
