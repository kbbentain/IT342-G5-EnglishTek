package com.example.englishtek_mobile.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishtek_mobile.data.model.Chapter
import com.example.englishtek_mobile.data.repository.ChapterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the HomeScreen
 */
class HomeViewModel(private val chapterRepository: ChapterRepository) : ViewModel() {
    private val TAG = "HomeViewModel"
    
    // UI state for the home screen
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadChapters()
    }
    
    /**
     * Load chapters from the repository
     */
    fun loadChapters() {
        _uiState.value = _uiState.value.copy(isLoading = true, isRefreshing = false)
        
        viewModelScope.launch {
            try {
                chapterRepository.getChapters().fold(
                    onSuccess = { chapters ->
                        _uiState.value = HomeUiState(
                            chapters = chapters,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                        Log.d(TAG, "✅ Successfully loaded ${chapters.size} chapters")
                    },
                    onFailure = { exception ->
                        _uiState.value = HomeUiState(
                            chapters = emptyList(),
                            isLoading = false,
                            isRefreshing = false,
                            error = exception.message ?: "Unknown error"
                        )
                        Log.e(TAG, "❌ Error loading chapters", exception)
                    }
                )
            } catch (exception: Exception) {
                _uiState.value = HomeUiState(
                    chapters = emptyList(),
                    isLoading = false,
                    isRefreshing = false,
                    error = exception.message ?: "Unknown error"
                )
                Log.e(TAG, "❌ Unexpected error loading chapters", exception)
            }
        }
    }
    
    /**
     * Refresh chapters - alias for loadChapters for consistency with UI naming
     */
    fun refreshChapters() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        
        viewModelScope.launch {
            try {
                chapterRepository.getChapters().fold(
                    onSuccess = { chapters ->
                        _uiState.value = HomeUiState(
                            chapters = chapters,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                        Log.d(TAG, "✅ Successfully refreshed ${chapters.size} chapters")
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = exception.message ?: "Unknown error"
                        )
                        Log.e(TAG, "❌ Error refreshing chapters", exception)
                    }
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = exception.message ?: "Unknown error"
                )
                Log.e(TAG, "❌ Unexpected error refreshing chapters", exception)
            }
        }
    }
}

/**
 * UI state for the HomeScreen
 */
data class HomeUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
