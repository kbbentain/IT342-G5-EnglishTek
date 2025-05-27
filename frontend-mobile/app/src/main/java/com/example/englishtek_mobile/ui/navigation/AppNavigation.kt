package com.example.englishtek_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.auth.TokenManager
import com.example.englishtek_mobile.data.model.Chapter
import com.example.englishtek_mobile.ui.screens.ChapterDetailScreen
import com.example.englishtek_mobile.ui.screens.LoginScreen
import com.example.englishtek_mobile.ui.screens.LessonScreen
import com.example.englishtek_mobile.ui.screens.QuizScreen
import com.example.englishtek_mobile.ui.screens.RegisterScreen
import com.example.englishtek_mobile.ui.screens.WelcomeScreen
import com.example.englishtek_mobile.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object AppDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main"
    const val CHAPTER_DETAIL_ROUTE = "chapter/{chapterId}"
    const val LESSON_ROUTE = "lesson/{lessonId}?chapterId={chapterId}"
    const val QUIZ_ROUTE = "quiz/{quizId}?chapterId={chapterId}"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    tokenManager: TokenManager,
    apiClient: ApiClient,
    homeViewModel: HomeViewModel
) {
    // Check if user is already authenticated
    LaunchedEffect(Unit) {
        val token = tokenManager.getToken().first()
        if (!token.isNullOrEmpty()) {
            // If token exists, navigate directly to main screen
            navController.navigate(AppDestinations.MAIN_ROUTE) {
                popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = AppDestinations.WELCOME_ROUTE
    ) {
        composable(AppDestinations.WELCOME_ROUTE) {
            WelcomeScreen(
                onStartJourneyClick = { navController.navigate(AppDestinations.REGISTER_ROUTE) },
                onSignInClick = { navController.navigate(AppDestinations.LOGIN_ROUTE) }
            )
        }
        
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                apiClient = apiClient,
                tokenManager = tokenManager,
                onLoginSuccess = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER_ROUTE)
                }
            )
        }
        
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                apiClient = apiClient,
                tokenManager = tokenManager,
                onRegisterSuccess = {
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE)
                }
            )
        }
        
        composable(AppDestinations.MAIN_ROUTE) {
            MainNavigation(
                apiClient = apiClient,
                tokenManager = tokenManager,
                homeViewModel = homeViewModel,
                onLogout = {
                    // Clear token and navigate back to welcome screen
                    // Use coroutine scope to call the suspend function
                    navController.currentBackStackEntry?.lifecycleScope?.launch {
                        tokenManager.clearToken()
                        navController.navigate(AppDestinations.WELCOME_ROUTE) {
                            popUpTo(AppDestinations.MAIN_ROUTE) { inclusive = true }
                        }
                    }
                },
                parentNavController = navController
            )
        }
        
        // Chapter detail screen route
        composable(
            route = AppDestinations.CHAPTER_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            ChapterDetailScreen(
                chapterId = chapterId,
                apiClient = apiClient,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLesson = { lessonId, chId ->
                    navController.navigate("lesson/$lessonId?chapterId=$chId")
                },
                onNavigateToQuiz = { quizId, chId ->
                    navController.navigate("quiz/$quizId?chapterId=$chId")
                }
            )
        }
        
        // Lesson screen route
        composable(
            route = AppDestinations.LESSON_ROUTE,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            
            LessonScreen(
                lessonId = lessonId,
                chapterId = chapterId,
                apiClient = apiClient,
                onNavigateBack = { navController.popBackStack() },
                onLessonCompleted = {
                    // Refresh chapter when returning
                    EventManager.emit(REFRESH_CHAPTER_EVENT, mapOf(
                        "lessonId" to lessonId,
                        "chapterId" to chapterId
                    ))
                }
            ).Content()
        }
        
        // Quiz screen route
        composable(
            route = AppDestinations.QUIZ_ROUTE,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            
            QuizScreen(
                quizId = quizId,
                chapterId = chapterId,
                apiClient = apiClient,
                onNavigateBack = { navController.popBackStack() },
                onQuizCompleted = {
                    // Refresh chapter when returning
                    EventManager.emit(REFRESH_CHAPTER_EVENT, mapOf(
                        "quizId" to quizId,
                        "chapterId" to chapterId
                    ))
                }
            ).Content()
        }
    }
}
