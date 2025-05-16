package com.example.englishtek_mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.Lesson
import com.example.englishtek_mobile.data.repository.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * UI state for the lesson screen
 */
sealed class LessonUiState {
    object Loading : LessonUiState()
    data class Success(val lesson: Lesson) : LessonUiState()
    data class Error(val message: String) : LessonUiState()
}

/**
 * ViewModel for the lesson screen
 */
class LessonViewModel(apiClient: ApiClient) : ViewModel() {
    private val repository = LessonRepository(apiClient.apiService)
    
    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    
    /**
     * Load lesson details
     */
    fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading
            repository.getLesson(lessonId).fold(
                onSuccess = { lesson ->
                    _uiState.value = LessonUiState.Success(lesson)
                },
                onFailure = { error ->
                    _uiState.value = LessonUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    /**
     * Mark lesson as started
     */
    fun startLesson(lessonId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.startLesson(lessonId).fold(
                onSuccess = {
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = LessonUiState.Error(error.message ?: "Failed to start lesson")
                }
            )
        }
    }
    
    /**
     * Mark lesson as completed
     */
    fun finishLesson(lessonId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.finishLesson(lessonId).fold(
                onSuccess = {
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = LessonUiState.Error(error.message ?: "Failed to complete lesson")
                }
            )
        }
    }
    
    /**
     * Navigate to next page
     */
    fun nextPage() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Success) {
            val lesson = currentState.lesson
            if (_currentPage.value < lesson.content.size - 1) {
                _currentPage.value = _currentPage.value + 1
            }
        }
    }
    
    /**
     * Navigate to previous page
     */
    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value = _currentPage.value - 1
        }
    }
    
    /**
     * Reset to first page
     */
    fun resetToFirstPage() {
        _currentPage.value = 0
    }
    
    /**
     * Update download progress
     */
    fun updateDownloadProgress(progress: Float) {
        _downloadProgress.value = progress
    }
    
    /**
     * Set downloading state
     */
    fun setDownloading(isDownloading: Boolean) {
        _isDownloading.value = isDownloading
        if (!isDownloading) {
            _downloadProgress.value = 0f
        }
    }
    
    /**
     * Download lesson as PDF
     */
    fun downloadPdf(lessonId: String, context: Context, onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isDownloading.value = true
                _downloadProgress.value = 0f
                
                // Create a file in the downloads directory
                val fileName = "lesson_${lessonId}.pdf"
                val downloadsDir = context.getExternalFilesDir(null)
                val outputFile = File(downloadsDir, fileName)
                
                repository.downloadLessonPdf(
                    lessonId = lessonId,
                    outputFile = outputFile,
                    progressCallback = { progress ->
                        _downloadProgress.value = progress
                    }
                ).fold(
                    onSuccess = { file ->
                        _isDownloading.value = false
                        _downloadProgress.value = 1f
                        onSuccess(file)
                    },
                    onFailure = { error ->
                        _isDownloading.value = false
                        _downloadProgress.value = 0f
                        onError(error.message ?: "Failed to download PDF")
                    }
                )
            } catch (e: Exception) {
                _isDownloading.value = false
                _downloadProgress.value = 0f
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}
