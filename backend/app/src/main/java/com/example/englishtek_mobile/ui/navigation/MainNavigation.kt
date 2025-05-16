package com.example.englishtek_mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.auth.TokenManager
import com.example.englishtek_mobile.ui.navigation.AppDestinations
import com.example.englishtek_mobile.ui.screens.AchievementsScreen
import com.example.englishtek_mobile.ui.screens.ChangePasswordScreen
import com.example.englishtek_mobile.ui.screens.EditProfileScreen
import com.example.englishtek_mobile.ui.screens.HomeScreen
import com.example.englishtek_mobile.ui.screens.LessonScreen
import com.example.englishtek_mobile.ui.screens.ProfileScreen
import com.example.englishtek_mobile.ui.screens.QuizScreen
import com.example.englishtek_mobile.ui.screens.SettingsScreen
import com.example.englishtek_mobile.ui.viewmodels.HomeViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import com.example.englishtek_mobile.data.model.User
import com.example.englishtek_mobile.ui.screens.loadUserProfile

// Define event constants
const val REFRESH_CHAPTER_EVENT = "refresh_chapter"

// Simple event manager for communication between screens
object EventManager {
    private val listeners = mutableMapOf<String, MutableList<(Map<String, Any>?) -> Unit>>()
    
    fun addListener(event: String, listener: (Map<String, Any>?) -> Unit) {
        if (!listeners.containsKey(event)) {
            listeners[event] = mutableListOf()
        }
        listeners[event]?.add(listener)
    }
    
    fun removeListener(event: String, listener: (Map<String, Any>?) -> Unit) {
        listeners[event]?.remove(listener)
    }
    
    fun emit(event: String, data: Map<String, Any>? = null) {
        listeners[event]?.forEach { it(data) }
    }
}

// Define the bottom navigation items
sealed class BottomNavItem(val route: String, val icon: Int, val selectedIcon: Int, val title: String) {
    object Home : BottomNavItem(
        route = "home",
        icon = R.drawable.ic_home_outline,
        selectedIcon = R.drawable.ic_home_filled,
        title = "Home"
    )
    
    object Achievements : BottomNavItem(
        route = "achievements",
        icon = R.drawable.ic_medal_outline,
        selectedIcon = R.drawable.ic_medal_filled,
        title = "Badges"
    )
    
    object Profile : BottomNavItem(
        route = "profile",
        icon = R.drawable.ic_account_outline,
        selectedIcon = R.drawable.ic_account_filled,
        title = "Profile"
    )
}

object MainNavRoutes {
    const val SETTINGS = "settings"
    const val EDIT_PROFILE_SCREEN = "EditProfileScreen"
    const val CHANGE_PASSWORD_SCREEN = "ChangePasswordScreen"
    const val ABOUT = "about"
}

@Composable
fun MainNavigation(
    apiClient: ApiClient,
    tokenManager: TokenManager,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    parentNavController: NavController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // List of bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Achievements,
        BottomNavItem.Profile
    )
    
    // Remember the last tab to restore it when returning to the app
    var lastTab by remember { mutableStateOf(BottomNavItem.Home.route) }
    
    Scaffold(
        bottomBar = {
            // Only show bottom bar if we're on a main tab, not on settings or other screens
            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        id = if (selected) item.selectedIcon else item.icon
                                    ),
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    // Save the last tab
                                    lastTab = item.route
                                    
                                    // Navigate to the selected tab
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = lastTab,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                var user by remember { mutableStateOf<User?>(null) }
                
                // Load user profile when navigating to Home screen
                LaunchedEffect(Unit) {
                    loadUserProfile(apiClient) { userData, _, _ ->
                        user = userData
                    }
                }
                
                HomeScreen(
                    viewModel = homeViewModel,
                    onChapterClick = { chapter ->
                        // Navigate to chapter detail screen
                        // Replace the route placeholder with the actual chapter ID
                        val route = AppDestinations.CHAPTER_DETAIL_ROUTE.replace("{chapterId}", chapter.id.toString())
                        parentNavController.navigate(route)
                    },
                    user = user
                )
            }
            
            composable(BottomNavItem.Achievements.route) {
                AchievementsScreen(apiClient = apiClient)
            }
            
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    apiClient = apiClient,
                    onLogout = onLogout,
                    onNavigateToSettings = {
                        navController.navigate(MainNavRoutes.SETTINGS)
                    }
                )
            }
            
            composable(MainNavRoutes.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    tokenManager = tokenManager,
                    onNavigateBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate(MainNavRoutes.EDIT_PROFILE_SCREEN) },
                    onChangePassword = { navController.navigate(MainNavRoutes.CHANGE_PASSWORD_SCREEN) },
                    onAbout = { navController.navigate(MainNavRoutes.ABOUT) }
                )
            }
            
            composable(MainNavRoutes.EDIT_PROFILE_SCREEN) {
                EditProfileScreen(
                    apiClient = apiClient,
                    user = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(MainNavRoutes.CHANGE_PASSWORD_SCREEN) {
                ChangePasswordScreen(
                    apiClient = apiClient,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            @OptIn(ExperimentalMaterial3Api::class)
            composable(MainNavRoutes.ABOUT) {
                // Simple About placeholder
                androidx.compose.material3.Scaffold(
                    topBar = {
                        androidx.compose.material3.TopAppBar(
                            title = { androidx.compose.material3.Text("About") },
                            navigationIcon = {
                                androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                                    Icons.Filled.ArrowBack
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text("EnglishTek Mobile App\nVersion 1.0", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            
            // Lesson Screen
            composable(
                route = "lesson/{lessonId}?chapterId={chapterId}",
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
                        EventManager.emit(REFRESH_CHAPTER_EVENT)
                    }
                ).Content()
            }
            
            // Quiz Screen
            composable(
                route = "quiz/{quizId}?chapterId={chapterId}",
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
                        EventManager.emit(REFRESH_CHAPTER_EVENT)
                    }
                ).Content()
            }
        }
    }
}
