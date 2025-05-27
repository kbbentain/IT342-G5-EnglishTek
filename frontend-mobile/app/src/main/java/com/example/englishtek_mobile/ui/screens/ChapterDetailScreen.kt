package com.example.englishtek_mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.ImageResult
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.api.ApiConfig
import com.example.englishtek_mobile.data.model.Chapter
import com.example.englishtek_mobile.data.model.ChapterItem
import com.example.englishtek_mobile.data.model.ChapterItemType
import com.example.englishtek_mobile.ui.components.FeedbackDialog
import com.example.englishtek_mobile.ui.viewmodels.ChapterDetailUiState
import com.example.englishtek_mobile.ui.viewmodels.ChapterDetailViewModel
import com.example.englishtek_mobile.ui.viewmodels.FeedbackState
import com.example.englishtek_mobile.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.*
import com.example.englishtek_mobile.ui.navigation.EventManager
import com.example.englishtek_mobile.ui.navigation.REFRESH_CHAPTER_EVENT
import android.util.Log

// Constants matching React Native implementation
private val ITEM_SIZE = 80.dp
private val PATH_COLOR = Color(0xFFE0E0E0)
private val ACTIVE_PATH_COLOR = Color(0xFF4ECDC4)

private val ITEM_COLORS = mapOf(
    ChapterItemType.LESSON to mapOf(
        "locked" to Color(0xFFE0E0E0),
        "available" to Color(0xFFFF7B7B),
        "completed" to Color(0xFF95CD41)
    ),
    ChapterItemType.QUIZ to mapOf(
        "locked" to Color(0xFFE0E0E0),
        "available" to Color(0xFF4ECDC4),
        "completed" to Color(0xFF95CD41)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapterId: String,
    apiClient: ApiClient,
    onNavigateBack: () -> Unit,
    onNavigateToLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    viewModel: ChapterDetailViewModel = viewModel(
        factory = ViewModelFactory(apiClient)
    )
) {
    // Load chapter data when the screen is first displayed
    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }
    
    // Listen for refresh events
    DisposableEffect(chapterId) {
        val refreshListener: (Map<String, Any>?) -> Unit = { data ->
            // Reload chapter data when refresh event is received
            viewModel.loadChapter(chapterId)
        }
        
        // Add listener for refresh events
        EventManager.addListener(REFRESH_CHAPTER_EVENT, refreshListener)
        
        // Return a dispose callback
        onDispose {
            EventManager.removeListener(REFRESH_CHAPTER_EVENT, refreshListener)
        }
    }
    
    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()
    
    // Observe feedback state
    val feedbackState by viewModel.feedbackState.collectAsState()
    
    // State for feedback dialog
    var showFeedbackDialog by remember { mutableStateOf(false) }
    
    // Check if we should show feedback dialog when chapter is loaded
    LaunchedEffect(uiState) {
        if (uiState is ChapterDetailUiState.Success) {
            val chapter = (uiState as ChapterDetailUiState.Success).chapter
            // Show feedback dialog if all items are completed and feedback hasn't been submitted yet
            val allItemsCompleted = chapter.items?.all { it.completed } ?: false
            if (allItemsCompleted && !chapter.hasCompletedFeedback) {
                showFeedbackDialog = true
            }
        }
    }
    
    // Handle feedback submission
    LaunchedEffect(feedbackState) {
        when (feedbackState) {
            is FeedbackState.Error -> {
                // Show error message
                // You could use a Snackbar or Toast here
            }
            is FeedbackState.Success -> {
                // Feedback submitted successfully
                showFeedbackDialog = false
            }
            else -> { /* No action needed */ }
        }
    }
    
    // Background pattern and gradient
    Box(modifier = Modifier.fillMaxSize()) {
        // Background pattern - using same pattern as React Native
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            // Pattern overlay using the pattern image
            Image(
                painter = painterResource(id = R.drawable.pattern1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Gradient overlay - matching React Native's LinearGradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF7B7B).copy(alpha = 0.9f),
                            Color(0xFF4ECDC4).copy(alpha = 0.9f)
                        )
                    )
                )
        )
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        when (uiState) {
                            is ChapterDetailUiState.Success -> Text(
                                text = (uiState as ChapterDetailUiState.Success).chapter.title ?: "Chapter",
                                color = Color.Black
                            )
                            else -> Text(text = "Chapter", color = Color.Black)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black
                    )
                )
            },
            containerColor = Color.Transparent // Make scaffold background transparent
        ) { paddingValues ->
            when (uiState) {
                is ChapterDetailUiState.Loading -> {
                    ChapterLoadingView()
                }
                is ChapterDetailUiState.Error -> {
                    ChapterErrorView(
                        error = (uiState as ChapterDetailUiState.Error).message,
                        onRetry = { viewModel.loadChapter(chapterId) }
                    )
                }
                is ChapterDetailUiState.Success -> {
                    val chapter = (uiState as ChapterDetailUiState.Success).chapter
                    ChapterDetailContent(
                        chapter = chapter,
                        viewModel = viewModel,
                        onNavigateToLesson = onNavigateToLesson,
                        onNavigateToQuiz = onNavigateToQuiz,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
        
        // Feedback Dialog
        FeedbackDialog(
            visible = showFeedbackDialog,
            onDismiss = { 
                showFeedbackDialog = false 
                viewModel.resetFeedbackState()
            },
            onSubmit = { rating, feedbackKeyword, additionalFeedback ->
                viewModel.submitFeedback(
                    chapterId = chapterId,
                    rating = rating,
                    feedbackKeyword = feedbackKeyword,
                    feedbackText = additionalFeedback
                )
            }
        )
    }
}

@Composable
fun ChapterDetailContent(
    chapter: Chapter,
    viewModel: ChapterDetailViewModel,
    onNavigateToLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chapter Info Card
        item {
            ChapterInfoCard(chapter)
        }
        
        // Chapter Items Path
        item {
            if (chapter.items != null && chapter.items.isNotEmpty()) {
                ChapterItemsPath(
                    items = chapter.items,
                    viewModel = viewModel,
                    onNavigateToLesson = onNavigateToLesson,
                    onNavigateToQuiz = onNavigateToQuiz,
                    chapterId = chapter.id
                )
            }
        }
    }
}

@Composable
fun ChapterInfoCard(chapter: Chapter) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF4F4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chapter Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (chapter.icon != null) {
                    val iconUrl = "${ApiConfig.BASE_URL}${chapter.icon}"
                    // Log the URL for debugging
                    Log.d("ChapterDetailScreen", "Loading chapter icon: $iconUrl")
                    
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(iconUrl)
                                .crossfade(true)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_broken_image)
                                .build()
                        ),
                        contentDescription = chapter.title,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Log success for debugging
                    Log.d("ChapterDetailScreen", "Attempted to load icon: $iconUrl")
                } else {
                    // Fallback icon
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = chapter.title,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chapter Title
            Text(
                text = chapter.title ?: "Untitled Chapter",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Chapter Description
            if (chapter.description != null) {
                Text(
                    text = chapter.description,
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Progress Section
            ProgressSection(chapter)
        }
    }
}

@Composable
fun ProgressSection(chapter: Chapter) {
    val completedItems = chapter.items?.count { it.completed } ?: 0
    val totalItems = chapter.items?.size ?: 0
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$completedItems / $totalItems Completed",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x1A000000)) // 10% black
        ) {
            val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ACTIVE_PATH_COLOR)
            )
        }
    }
}

@Composable
fun ChapterItemsPath(
    items: List<ChapterItem>,
    viewModel: ChapterDetailViewModel,
    onNavigateToLesson: (String, String) -> Unit,
    onNavigateToQuiz: (String, String) -> Unit,
    chapterId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { index, item ->
            val isLocked = viewModel.isItemLocked(item, items)
            val isLast = index == items.size - 1
            
            // Create a bounce animation for each item
            val bounceState = remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (bounceState.value) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessLow
                ),
                finishedListener = { if (bounceState.value) bounceState.value = false }
            )
            
            ChapterItemNode(
                item = item,
                isLocked = isLocked,
                scale = scale,
                onItemClick = {
                    if (isLocked) {
                        // Bounce animation for locked items
                        bounceState.value = true
                    } else {
                        if (item.type == ChapterItemType.LESSON) {
                            onNavigateToLesson(item.id, chapterId)
                        } else if (item.type == ChapterItemType.QUIZ) {
                            onNavigateToQuiz(item.id, chapterId)
                        }
                    }
                }
            )
            
            // Add path line between items
            if (!isLast) {
                PathLine(
                    isCompleted = item.completed,
                    isLocked = isLocked
                )
            }
        }
    }
}

@Composable
fun ChapterItemNode(
    item: ChapterItem,
    isLocked: Boolean,
    scale: Float = 1f,
    onItemClick: () -> Unit
) {
    val status = when {
        item.completed -> "completed"
        isLocked -> "locked"
        else -> "available"
    }
    
    val itemColor = ITEM_COLORS[item.type]?.get(status) ?: Color.Gray
    
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Node Button with badge positioned outside
        Box(modifier = Modifier.padding(top = 10.dp, end = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(ITEM_SIZE)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(itemColor)
                    .clickable { onItemClick() },
                contentAlignment = Alignment.Center
            ) {
                // Icon based on type and status
                val icon = when {
                    isLocked -> if (item.type == ChapterItemType.LESSON) Icons.Default.Lock else Icons.Default.Lock
                    item.completed -> if (item.type == ChapterItemType.LESSON) Icons.Default.CheckCircle else Icons.Default.CheckCircle
                    item.type == ChapterItemType.LESSON -> Icons.Default.MenuBook
                    else -> Icons.Default.Quiz
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Completed star badge
            if (item.completed) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = ITEM_SIZE - 16.dp, y = -8.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Completed",
                        tint = Color(0xFFFFD700), // Gold
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label
        Card(
            modifier = Modifier
                .widthIn(min = ITEM_SIZE * 1.5f)
                .shadow(2.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = getItemStatusText(item.type, status),
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PathLine(
    isCompleted: Boolean,
    isLocked: Boolean
) {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                color = when {
                    isCompleted -> Color(0xFF95CD41) // Completed path
                    isLocked -> PATH_COLOR // Locked path
                    else -> ACTIVE_PATH_COLOR // Active path
                }
            )
    )
}

@Composable
fun ChapterLoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4ECDC4),
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun ChapterErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFFF6B6B)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
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

// Helper function to get item status text
private fun getItemStatusText(type: ChapterItemType, status: String): String {
    val itemType = if (type == ChapterItemType.LESSON) "Lesson" else "Quiz"
    val icon = when (status) {
        "locked" -> "🔒"
        "available" -> if (type == ChapterItemType.LESSON) "📚" else "✏️"
        "completed" -> "✅"
        else -> ""
    }
    
    val statusDisplay = status.replaceFirstChar { it.uppercase() }
    return "$icon $itemType • $statusDisplay"
}
