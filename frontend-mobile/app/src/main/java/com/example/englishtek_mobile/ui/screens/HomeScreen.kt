@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "EXPERIMENTAL_API_USAGE")

package com.example.englishtek_mobile.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.englishtek_mobile.BuildConfig
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.api.ApiConfig
import com.example.englishtek_mobile.data.model.Chapter
import com.example.englishtek_mobile.data.model.ChapterStatus
import com.example.englishtek_mobile.data.model.User
import com.example.englishtek_mobile.ui.viewmodels.HomeUiState
import com.example.englishtek_mobile.ui.viewmodels.HomeViewModel
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

// Chapter status colors identical to React Native app
private val CHAPTER_COLORS = mapOf(
    ChapterStatus.AVAILABLE to Color(0xFFFF7B7B),    // Bright coral
    ChapterStatus.IN_PROGRESS to Color(0xFF4ECDC4),  // Turquoise
    ChapterStatus.COMPLETED to Color(0xFF95CD41),    // Lime green
    ChapterStatus.LOCKED to Color(0xFFE0E0E0)        // Light gray
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onChapterClick: (Chapter) -> Unit,
    user: User?
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadChapters()
    }
    
    HomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.refreshChapters() },
        onChapterClick = onChapterClick,
        user = user
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    user: User?
) {
    Scaffold(
        topBar = { HomeAppBar(user = user) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA)) // Matching React Native background color
        ) {
            if (uiState.isLoading && !uiState.isRefreshing) {
                LoadingView()
            } else if (uiState.error != null && uiState.chapters.isEmpty()) {
                ErrorView(error = uiState.error, onRetry = onRefresh)
            } else {
                ChapterList(
                    chapters = uiState.chapters,
                    onChapterClick = onChapterClick,
                    onRefresh = onRefresh,
                    isRefreshing = uiState.isRefreshing
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(user: User?) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side with greeting
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello, ${user?.name?.split(" ")?.firstOrNull() ?: "Student"}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "What do you want to learn today?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }
            
            // Right side with profile image
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                if (user?.avatarUrl != null) {
                    val fullAvatarUrl = "${ApiConfig.BASE_URL}${user.avatarUrl}"
                    Image(
                        painter = rememberAsyncImagePainter(fullAvatarUrl),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.character1),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Color(0xFF4ECDC4))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading chapters...",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            tint = Color.Red.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error: $error",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
        ) {
            Text("Try Again")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterList(
    chapters: List<Chapter>,
    onChapterClick: (Chapter) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chapters) { chapter ->
                if (chapter.status == ChapterStatus.LOCKED) {
                    LockedChapterCard(chapter = chapter)
                } else {
                    UnlockedChapterCard(chapter = chapter, onClick = { onChapterClick(chapter) })
                }
            }
            
            // Add some bottom padding
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = Color(0xFF4ECDC4)
        )
    }
}

@Composable
fun UnlockedChapterCard(chapter: Chapter, onClick: () -> Unit) {
    val backgroundColor = CHAPTER_COLORS[chapter.status] ?: Color.Gray
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon container with badge
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                // Chapter icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(
                            width = 3.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (chapter.icon != null) {
                        // Log the icon URL for debugging
                        val iconUrl = "${ApiConfig.BASE_URL}${chapter.icon}"
                        Log.d("HomeScreen", "Loading chapter icon: $iconUrl")
                        
                        Image(
                            painter = rememberAsyncImagePainter(
                                iconUrl,
                                onLoading = { Log.d("HomeScreen", "Loading icon: $iconUrl") },
                                onSuccess = { Log.d("HomeScreen", "Successfully loaded icon: $iconUrl") },
                                onError = { Log.e("HomeScreen", "Error loading icon: $iconUrl", it.result.throwable) }
                            ),
                            contentDescription = chapter.title,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback icon
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home_filled),
                            contentDescription = chapter.title,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                // Status badge for completed or in-progress chapters
                val statusIcon = when {
                    chapter.status == ChapterStatus.COMPLETED -> "star"
                    chapter.status == ChapterStatus.IN_PROGRESS && chapter.completedTasks > 0 -> "progress-check"
                    else -> null
                }
                
                if (statusIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .offset(x = 5.dp, y = 5.dp)
                            .align(Alignment.BottomEnd)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (statusIcon == "star") {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Completed",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "In Progress",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Chapter title
            Text(
                text = chapter.title ?: "Untitled Chapter",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.2f),
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                        blurRadius = 3f
                    )
                )
            )
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                val progress = if (chapter.totalTasks > 0) {
                    chapter.completedTasks.toFloat() / chapter.totalTasks.toFloat()
                } else 0f
                
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress text
            Text(
                text = "${chapter.completedTasks} / ${chapter.totalTasks} Tasks",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LockedChapterCard(chapter: Chapter) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3C3C3C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chapter icon with lock overlay
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                // Chapter icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(
                            width = 3.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (chapter.icon != null) {
                        // Log the icon URL for debugging
                        val iconUrl = "${ApiConfig.BASE_URL}${chapter.icon}"
                        Log.d("HomeScreen", "Loading chapter icon: $iconUrl")
                        
                        Image(
                            painter = rememberAsyncImagePainter(
                                iconUrl,
                                onLoading = { Log.d("HomeScreen", "Loading icon: $iconUrl") },
                                onSuccess = { Log.d("HomeScreen", "Successfully loaded icon: $iconUrl") },
                                onError = { Log.e("HomeScreen", "Error loading icon: $iconUrl", it.result.throwable) }
                            ),
                            contentDescription = chapter.title,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Crop,
                            alpha = 0.5f  // Dim the image to show it's locked
                        )
                    } else {
                        // Fallback icon
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home_filled),
                            contentDescription = chapter.title,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Lock overlay
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Text(
                text = "Complete previous chapter",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chapter title
            Text(
                text = chapter.title ?: "Untitled Chapter",
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress text
            Text(
                text = "0 / ${chapter.totalTasks} Tasks",
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
