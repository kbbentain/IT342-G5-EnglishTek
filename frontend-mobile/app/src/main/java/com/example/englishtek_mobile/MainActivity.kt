package com.example.englishtek_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.auth.TokenManager
import com.example.englishtek_mobile.data.repository.ChapterRepository
import com.example.englishtek_mobile.ui.navigation.AppNavigation
import com.example.englishtek_mobile.ui.theme.EnglishTekTheme
import com.example.englishtek_mobile.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // Lazy initialization of dependencies
    private val tokenManager by lazy { TokenManager(applicationContext) }
    private val apiClient by lazy { ApiClient(applicationContext) }
    private val chapterRepository by lazy { ChapterRepository(apiClient.apiService) }
    private val homeViewModel by lazy { HomeViewModel(chapterRepository) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check if token exists
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            // Log token status (not the actual token for security)
            if (token != null) {
                android.util.Log.d("MainActivity", " Token exists, user is authenticated")
            } else {
                android.util.Log.d("MainActivity", " No token found, user needs to login")
            }
        }
        
        setContent {
            EnglishTekTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    // Pass dependencies to navigation
                    AppNavigation(
                        navController = navController,
                        tokenManager = tokenManager,
                        apiClient = apiClient,
                        homeViewModel = homeViewModel
                    )
                }
            }
        }
    }
}