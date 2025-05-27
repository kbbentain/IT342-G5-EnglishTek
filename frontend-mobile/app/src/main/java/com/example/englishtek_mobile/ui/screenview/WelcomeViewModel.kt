package com.example.englishtek_mobile.ui.screenview

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Welcome screen that handles business logic and navigation events.
 */
class WelcomeViewModel : ViewModel() {
    
    // Navigation events
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    
    /**
     * Handle click on the "Start Your Journey" button
     */
    fun onStartJourneyClick() {
        _navigationEvent.value = NavigationEvent.NavigateToRegistration
    }
    
    /**
     * Handle click on the "Already have an account? Sign in" button
     */
    fun onSignInClick() {
        _navigationEvent.value = NavigationEvent.NavigateToLogin
    }
    
    /**
     * Reset the navigation event after it's been handled
     */
    fun onNavigationEventHandled() {
        _navigationEvent.value = null
    }
    
    /**
     * Sealed class representing navigation events from the Welcome screen
     */
    sealed class NavigationEvent {
        object NavigateToRegistration : NavigationEvent()
        object NavigateToLogin : NavigationEvent()
    }
}
