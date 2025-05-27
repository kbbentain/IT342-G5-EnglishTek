package com.example.englishtek_mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.api.ApiConfig
import com.example.englishtek_mobile.data.model.Badge
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(apiClient: ApiClient) {
    // Calculate badge size based on screen width (35% of screen width like in React Native)
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val badgeSize = (screenWidth * 0.35).dp
    val lockIconSize = (badgeSize.value * 0.3).dp
    
    // State for badges
    var allBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }
    var unlockedBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Function to load badges
    val loadBadges = { isRefreshingCall: Boolean ->
        if (!isRefreshingCall) isLoading = true
        loadBadgesData(apiClient) { badges, unlocked, errorMsg ->
            allBadges = badges
            unlockedBadges = unlocked
            error = errorMsg
            isLoading = false
            isRefreshing = false
        }
    }
    
    // Load badges on first composition and when returning to screen
    LaunchedEffect(key1 = Unit) {
        loadBadges(false)
    }
    
    // Force refresh when screen becomes active
    DisposableEffect(Unit) {
        loadBadges(false)
        onDispose { }
    }
    
    // Helper function to check if a badge is unlocked
    val isUnlocked = { badgeId: String ->
        unlockedBadges.any { it.id == badgeId }
    }
    
    // Helper function to get the date a badge was obtained
    val getBadgeDate = { badgeId: String ->
        unlockedBadges.find { it.id == badgeId }?.dateObtained
    }
    
    // Background pattern
    val patternPainter = painterResource(id = R.drawable.bg_pattern)
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background with pattern and gradient overlay
        Box(modifier = Modifier.fillMaxSize()) {
            // Pattern background
            Image(
                painter = patternPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.15f
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4ECDC4).copy(alpha = 0.8f),
                                Color(0xFF556270).copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Badges",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (isLoading && !isRefreshing) {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (error != null && allBadges.isEmpty()) {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Error",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Failed to load badges",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { loadBadges(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Try Again",
                                color = Color(0xFF4ECDC4)
                            )
                        }
                    }
                }
            } else {
                // Sort badges: unlocked badges first, then locked badges
                val sortedBadges = allBadges.sortedByDescending { badge ->
                    isUnlocked(badge.id ?: "")
                }
                
                // Content - Badge list
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = {
                        isRefreshing = true
                        loadBadges(true)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Display sorted badges
                        items(sortedBadges) { badge ->
                            BadgeItem(
                                badge = badge,
                                isUnlocked = isUnlocked(badge.id ?: ""),
                                dateObtained = getBadgeDate(badge.id ?: ""),
                                badgeSize = badgeSize,
                                lockIconSize = lockIconSize
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(
    badge: Badge,
    isUnlocked: Boolean,
    dateObtained: String?,
    badgeSize: androidx.compose.ui.unit.Dp,
    lockIconSize: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xF2FFFFFF) // Slightly transparent white
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge image container with star positioned outside
            Box(modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
                Box(
                    modifier = Modifier
                        .size(badgeSize)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            color = if (isUnlocked) Color.White else Color(0xFFF5F5F5)
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Badge image
                    if (badge.iconUrl != null) {
                        // Load badge icon from API using Coil
                        val fullIconUrl = "${ApiConfig.BASE_URL}${badge.iconUrl}"
                        Image(
                            painter = rememberAsyncImagePainter(fullIconUrl),
                            contentDescription = badge.name,
                            modifier = Modifier.size(badgeSize * 0.7f),
                            contentScale = ContentScale.Fit,
                            alpha = if (isUnlocked) 1f else 0.3f
                        )
                    } else {
                        // Fallback icon if no iconUrl is available
                        Icon(
                            painter = painterResource(id = R.drawable.ic_medal_filled),
                            contentDescription = badge.name,
                            tint = if (isUnlocked) Color(0xFF4ECDC4) else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(badgeSize * 0.4f)
                        )
                    }
                    
                    // Lock overlay for locked badges
                    if (!isUnlocked) {
                        Box(
                            modifier = Modifier
                                .size(badgeSize)
                                .background(Color(0x80000000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(lockIconSize)
                            )
                        }
                    }
                }
                
                // Sparkle for unlocked badges
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .size(lockIconSize)
                            .offset(x = badgeSize - (lockIconSize / 2), y = -(lockIconSize / 2))
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Unlocked",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier
                                .size(lockIconSize * 0.8f)
                                .rotate(45f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Badge info
            Column(modifier = Modifier.weight(1f)) {
                // Badge name
                Text(
                    text = badge.name ?: "Unknown Badge",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isUnlocked) Color(0xFF2C3E50) else Color(0xFFBDC3C7),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Badge description
                Text(
                    text = badge.description ?: "",
                    fontSize = 14.sp,
                    color = if (isUnlocked) Color(0xFF7F8C8D) else Color(0xFFBDC3C7),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Badge date obtained (if unlocked)
                if (isUnlocked && dateObtained != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_medal_filled),
                            contentDescription = "Date Earned",
                            tint = Color(0xFF4ECDC4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Earned ${formatDate(dateObtained)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4ECDC4)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateStr) ?: return "Recently"
        
        val now = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }
        
        // Check if it's today
        if (now.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)) {
            return "Today"
        }
        
        // Check if it's yesterday
        val yesterday = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, -1) 
        }
        if (yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday"
        }
        
        // Check if it's within the last week
        val lastWeek = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, -7) 
        }
        if (date.after(lastWeek.time)) {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            return dayFormat.format(date) // Returns day name (e.g., "Monday")
        }
        
        // If it's this year, show month and day
        if (now.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR)) {
            val monthDayFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
            return monthDayFormat.format(date) // Returns "January 15"
        }
        
        // Otherwise show month, day and year
        val fullFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return fullFormat.format(date)
    } catch (e: Exception) {
        "Recently" // Fallback if date parsing fails
    }
}

private fun loadBadgesData(
    apiClient: ApiClient,
    callback: (List<Badge>, List<Badge>, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Make actual API calls instead of using mock data
            val allBadgesResponse = apiClient.apiService.getAllBadges()
            val myBadgesResponse = apiClient.apiService.getMyBadges()
            
            val allBadges = allBadgesResponse.body() ?: emptyList()
            val myBadges = myBadgesResponse.body() ?: emptyList()
            
            withContext(Dispatchers.Main) {
                callback(allBadges, myBadges, null)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(emptyList(), emptyList(), e.message ?: "Failed to load badges")
            }
        }
    }
}
