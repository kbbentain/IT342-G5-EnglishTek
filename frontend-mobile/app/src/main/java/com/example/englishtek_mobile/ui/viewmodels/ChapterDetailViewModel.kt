package com.example.englishtek_mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.Chapter
import com.example.englishtek_mobile.data.model.ChapterItem
import com.example.englishtek_mobile.data.model.FeedbackRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterDetailViewModel(private val apiClient: ApiClient) : ViewModel() {

    // UI state for chapter detail screen
    private val _uiState = MutableStateFlow<ChapterDetailUiState>(ChapterDetailUiState.Loading)
    val uiState: StateFlow<ChapterDetailUiState> = _uiState.asStateFlow()
    
    // Feedback submission state
    private val _feedbackState = MutableStateFlow<FeedbackState>(FeedbackState.Idle)
    val feedbackState: StateFlow<FeedbackState> = _feedbackState.asStateFlow()

    // Load chapter details
    fun loadChapter(chapterId: String) {
        _uiState.value = ChapterDetailUiState.Loading
        viewModelScope.launch {
            try {
                val response = apiClient.apiService.getChapterDetail(chapterId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ChapterDetailUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ChapterDetailUiState.Error("Failed to load chapter: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ChapterDetailUiState.Error("Error loading chapter: ${e.message}")
            }
        }
    }

    // Submit feedback for a chapter
    fun submitFeedback(chapterId: String, rating: Int, feedbackKeyword: String, feedbackText: String) {
        _feedbackState.value = FeedbackState.Submitting
        viewModelScope.launch {
            try {
                val request = FeedbackRequest(
                    chapterId = chapterId,
                    rating = rating,
                    feedbackKeyword = feedbackKeyword,
                    feedbackText = feedbackText
                )
                
                val response = apiClient.apiService.submitFeedback(request)
                
                if (response.isSuccessful) {
                    _feedbackState.value = FeedbackState.Success
                    // Reload the chapter to update the hasCompletedFeedback flag
                    loadChapter(chapterId)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    _feedbackState.value = FeedbackState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _feedbackState.value = FeedbackState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Determine if an item is locked based on previous items
    fun isItemLocked(item: ChapterItem, items: List<ChapterItem>): Boolean {
        val index = items.indexOf(item)
        // First item is never locked
        if (index == 0) return false
        // Item is locked if any previous item is not completed
        return items.subList(0, index).any { !it.completed }
    }
    
    // Reset feedback state
    fun resetFeedbackState() {
        _feedbackState.value = FeedbackState.Idle
    }
}

// UI states for chapter detail screen
sealed class ChapterDetailUiState {
    object Loading : ChapterDetailUiState()
    data class Success(val chapter: Chapter) : ChapterDetailUiState()
    data class Error(val message: String) : ChapterDetailUiState()
}

// Feedback submission states
sealed class FeedbackState {
    object Idle : FeedbackState()
    object Submitting : FeedbackState()
    object Success : FeedbackState()
    data class Error(val message: String) : FeedbackState()
}
