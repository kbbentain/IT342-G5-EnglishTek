package com.example.englishtek_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.api.ApiConfig
import com.example.englishtek_mobile.data.model.User
import com.example.englishtek_mobile.data.model.ActivityLog
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    apiClient: ApiClient,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // State for user profile
    var user by remember { mutableStateOf<User?>(null) }
    var recentActivities by remember { mutableStateOf<List<ActivityLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Function to load user profile
    val loadProfile = {
        loadUserProfile(apiClient) { userData, activities, errorMsg ->
            user = userData
            recentActivities = activities
            error = errorMsg
            isLoading = false
            isRefreshing = false
        }
    }
    
    // Load user profile
    LaunchedEffect(key1 = Unit) {
        loadProfile()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                loadProfile()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4ECDC4))
                }
            } else if (error != null && recentActivities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Profile Info Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 15.dp)
                                    .shadow(4.dp, CircleShape)
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                val avatarUrl = user?.avatarUrl
                                if (avatarUrl != null && avatarUrl.isNotBlank()) {
                                    // Directly prepend the base URL to the avatar path
                                    val fullAvatarUrl = "${ApiConfig.BASE_URL}${avatarUrl}"
                                    
                                    // Log the avatar URL for debugging
                                    Log.d("ProfileScreen", "Original avatarUrl: $avatarUrl")
                                    Log.d("ProfileScreen", "Full avatar URL: $fullAvatarUrl")
                                    Log.d("ProfileScreen", "Base URL: ${ApiConfig.BASE_URL}")
                                    
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(fullAvatarUrl),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Use the exact same preset profile image as React Native app
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.character1),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            
                            // Name
                            Text(
                                text = user?.name ?: "User",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                            
                            // Email
                            Text(
                                text = user?.email ?: "email@example.com",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                            
                            // Bio
                            Text(
                                text = user?.bio ?: "No bio yet. Tell us about yourself!",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .padding(horizontal = 32.dp, vertical = 12.dp)
                            )
                            
                            // Role
                            Text(
                                text = user?.role?.uppercase() ?: "STUDENT",
                                fontSize = 12.sp,
                                color = Color(0xFF888888),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    // Stats Section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Completed Tasks
                            StatCard(
                                icon = R.drawable.ic_home_filled,
                                value = "${user?.totalCompletedTasks ?: 0}",
                                label = "Completed Tasks",
                                backgroundColor = Color(0xFFFFE8E8),
                                contentColor = Color(0xFFFF6B6B),
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            // Total Badges
                            StatCard(
                                icon = R.drawable.ic_medal_filled,
                                value = "${user?.totalBadges ?: 0}",
                                label = "Total Badges",
                                backgroundColor = Color(0xFFE8FFE8),
                                contentColor = Color(0xFF4CD964),
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            // Current Chapter
                            StatCard(
                                icon = R.drawable.ic_home_filled, // Replace with fire icon if available
                                value = "${user?.completedLessons?.div(5)?.plus(1) ?: 1}",
                                label = "Current Chapter",
                                backgroundColor = Color(0xFFFFF4E5),
                                contentColor = Color(0xFFFF9500),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Recent Activity Section
                    item {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Recent Activity",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                            
                            if (recentActivities.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_home_outline),
                                        contentDescription = "No Activity",
                                        tint = Color(0xFF666666),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = "No recent activity yet. Start learning to see your progress here!",
                                        fontSize = 16.sp,
                                        color = Color(0xFF666666),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    
                    // Activity Items
                    if (recentActivities.isNotEmpty()) {
                        items(recentActivities) { activity ->
                            ActivityItemCard(activity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: Int,
    value: String,
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(5.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 5.dp)
            )
            
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityItemCard(activity: ActivityLog) {
    val activityColor = getActivityColor(activity.type)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.5.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = activityColor.bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Icon(
                painter = painterResource(id = getActivityIcon(activity.type)),
                contentDescription = activity.type,
                tint = activityColor.fg,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(15.dp))
            
            // Activity details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = activityColor.fg,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                
                Text(
                    text = getRelativeTime(activity.date),
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

private fun getActivityIcon(type: String): Int {
    return when (type) {
        "quiz" -> R.drawable.ic_medal_filled
        "lesson" -> R.drawable.ic_home_filled
        "badge" -> R.drawable.ic_medal_filled
        else -> R.drawable.ic_home_filled
    }
}

private fun getActivityColor(type: String): ActivityColor {
    return when (type) {
        "quiz" -> ActivityColor(Color(0xFFFFE8E8), Color(0xFFFF6B6B))
        "lesson" -> ActivityColor(Color(0xFFE8FFE8), Color(0xFF4CD964))
        "badge" -> ActivityColor(Color(0xFFFFF4E5), Color(0xFFFF9500))
        else -> ActivityColor(Color(0xFFE8E8E8), Color(0xFF666666))
    }
}

private fun getRelativeTime(timestamp: String): String {
    try {
        // The API returns date in format "yyyy-MM-dd HH:mm:ss"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timestamp) ?: return "Unknown time"
        val now = Date()
        val diffInMillis = now.time - date.time
        
        return when {
            diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "just now"
            diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            else -> {
                val sdfOut = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdfOut.format(date)
            }
        }
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error parsing date: $timestamp", e)
        return "Unknown time"
    }
}

fun loadUserProfile(
    apiClient: ApiClient,
    callback: (User?, List<ActivityLog>, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val userResponse = apiClient.apiService.getUserProfile()
            val activitiesResponse = apiClient.apiService.getRecentActivities()
            
            val user = userResponse.body()
            val activities = activitiesResponse.body() ?: emptyList()
            
            withContext(Dispatchers.Main) {
                callback(user, activities, null)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(null, emptyList(), e.message ?: "Failed to load profile")
            }
        }
    }
}

data class ActivityColor(
    val bg: Color,
    val fg: Color
)
