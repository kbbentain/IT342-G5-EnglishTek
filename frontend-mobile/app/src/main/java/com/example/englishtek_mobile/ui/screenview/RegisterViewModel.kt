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

class RegisterViewModel(private val apiClient: ApiClient, private val tokenManager: TokenManager) : ViewModel() {
    private val TAG = "RegisterViewModel"
    private val authRepository = AuthRepository(apiClient, tokenManager)
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }
    
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }
    
    fun register() {
        val currentState = _uiState.value
        
        // Basic validation
        if (currentState.username.isBlank() || currentState.name.isBlank() || 
            currentState.email.isBlank() || currentState.password.isBlank() || 
            currentState.confirmPassword.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please fill in all fields"
            )
            return
        }
        
        // Password validation
        if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(
                errorMessage = "Password must be at least 6 characters long"
            )
            return
        }
        
        if (currentState.password != currentState.confirmPassword) {
            _uiState.value = currentState.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        Log.d(TAG, "\uD83D\uDD11 Attempting registration for user: ${currentState.username}")
        
        viewModelScope.launch {
            val result = authRepository.register(
                username = currentState.username,
                email = currentState.email,
                password = currentState.password,
                name = currentState.name
            )
            
            result.fold(
                onSuccess = { authResponse ->
                    val username = authResponse.user?.username ?: currentState.username
                    Log.d(TAG, "\u2705 Registration successful for user: $username")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "\u274C Registration failed: ${error.message}")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed. Please try again."
                    )
                }
            )
        }
    }
    
    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}

data class RegisterUiState(
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false
)
