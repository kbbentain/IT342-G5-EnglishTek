package com.example.englishtek_mobile.ui.screenview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.auth.TokenManager
import com.example.englishtek_mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val apiClient: ApiClient, private val tokenManager: TokenManager) : ViewModel() {
    private val TAG = "LoginViewModel"
    private val authRepository = AuthRepository(apiClient, tokenManager)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }
    
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun login() {
        val currentState = _uiState.value
        
        // Basic validation
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please fill in all fields"
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        Log.d(TAG, "🔑 Attempting login for user: ${currentState.username}")
        
        viewModelScope.launch {
            val result = authRepository.login(currentState.username, currentState.password)
            
            result.fold(
                onSuccess = { authResponse ->
                    // Add null safety check for user
                    val username = authResponse.user?.username ?: currentState.username
                    Log.d(TAG, "✅ Login successful for user: $username")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Login failed: ${error.message}")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed. Please try again."
                    )
                }
            )
        }
    }
    
    fun resetState() {
        _uiState.value = LoginUiState()
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null
)
