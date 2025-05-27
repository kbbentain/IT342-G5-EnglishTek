package com.example.englishtek_mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.repository.ChapterRepository

/**
 * Factory for creating ViewModels with dependencies
 */
class ViewModelFactory(
    private val apiClient: ApiClient,
    private val chapterRepository: ChapterRepository? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                chapterRepository?.let { HomeViewModel(it) } as T
                    ?: throw IllegalArgumentException("ChapterRepository is required for HomeViewModel")
            }
            modelClass.isAssignableFrom(ChapterDetailViewModel::class.java) -> {
                ChapterDetailViewModel(apiClient) as T
            }
            modelClass.isAssignableFrom(LessonViewModel::class.java) -> {
                LessonViewModel(apiClient) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                QuizViewModel(apiClient) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
