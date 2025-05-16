package com.example.englishtek_mobile.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.Lesson
import com.example.englishtek_mobile.ui.components.MarkdownText
import com.example.englishtek_mobile.ui.navigation.EventManager
import com.example.englishtek_mobile.ui.navigation.REFRESH_CHAPTER_EVENT
import com.example.englishtek_mobile.ui.viewmodels.LessonUiState
import com.example.englishtek_mobile.ui.viewmodels.LessonViewModel
import com.example.englishtek_mobile.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class LessonScreen(
    private val lessonId: String,
    private val chapterId: String,
    private val apiClient: ApiClient,
    private val onNavigateBack: () -> Unit,
    private val onLessonCompleted: () -> Unit
) {
    @Composable
    fun Content() {
        val viewModel: LessonViewModel = viewModel(
            factory = ViewModelFactory(apiClient)
        )
        
        val uiState by viewModel.uiState.collectAsState()
        val currentPage by viewModel.currentPage.collectAsState()
        val isDownloading by viewModel.isDownloading.collectAsState()
        val downloadProgress by viewModel.downloadProgress.collectAsState()
        
        // States for the lesson flow
        var currentState by remember { mutableStateOf("overview") }
        
        // Animation states
        val fadeAnim by animateFloatAsState(
            targetValue = if (currentState == "content") 1f else 0f,
            label = "fadeAnimation"
        )
        
        // Load lesson when the screen is first displayed
        LaunchedEffect(lessonId) {
            viewModel.loadLesson(lessonId)
        }
        
        // Check if lesson is already completed when it loads
        LaunchedEffect(uiState) {
            if (uiState is LessonUiState.Success) {
                val lesson = (uiState as LessonUiState.Success).lesson
                if (lesson.isCompleted && currentState == "overview") {
                    // If lesson is already completed, show the completed state
                    Log.d("LessonScreen", "✅ Lesson is already completed, showing completed state")
                    currentState = "completed"
                }
            }
        }
        
        // Context for downloading PDF
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val downloadPdf: () -> Unit = {
            val lesson = (uiState as? LessonUiState.Success)?.lesson
            if (lesson != null) {
                viewModel.downloadPdf(
                    lessonId = lessonId,
                    context = context,
                    onSuccess = { file ->
                        // Show success message
                        Toast.makeText(
                            context,
                            "PDF downloaded successfully: ${file.name}",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Open the PDF file
                        val intent = Intent(Intent.ACTION_VIEW)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        intent.setDataAndType(uri, "application/pdf")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // If no PDF viewer is available, open in browser
                            val browserIntent = Intent(Intent.ACTION_VIEW)
                            browserIntent.data = Uri.parse("https://docs.google.com/viewer?url=${uri}")
                            context.startActivity(browserIntent)
                        }
                    },
                    onError = { errorMessage ->
                        // Show error message
                        Toast.makeText(
                            context,
                            "Failed to download PDF: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Reset download state
                        viewModel.setDownloading(false)
                    }
                )
            } else {
                Toast.makeText(
                    context,
                    "Lesson not loaded yet. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        // Handle page navigation
        val handleNextPage: () -> Unit = {
            viewModel.nextPage()
        }
        
        val handlePrevPage: () -> Unit = {
            viewModel.previousPage()
        }
        
        // Handle starting a lesson
        val handleStartLesson: () -> Unit = {
            scope.launch {
                Log.d("LessonScreen", "🔑 Starting lesson $lessonId")
                viewModel.startLesson(lessonId) {
                    // On success, transition to content state
                    Log.d("LessonScreen", "✅ Lesson started successfully")
                    currentState = "content"
                }
            }
        }
        
        // Handle finishing a lesson
        val handleFinishLesson: () -> Unit = {
            scope.launch {
                Log.d("LessonScreen", "📚 Lesson Summary:")
                val lesson = (uiState as? LessonUiState.Success)?.lesson
                Log.d("LessonScreen", "Total Pages: ${lesson?.content?.size ?: 0}")
                Log.d("LessonScreen", "🎯 Starting lesson completion flow...")
                
                // Submit lesson completion
                Log.d("LessonScreen", "📝 Submitting lesson $lessonId completion to API")
                viewModel.finishLesson(lessonId) {
                    Log.d("LessonScreen", "✅ Lesson completed successfully")
                    // Update state to show lesson as completed
                    currentState = "completed"
                    // Emit event to refresh chapter data
                    EventManager.emit(REFRESH_CHAPTER_EVENT)
                }
            }
        }
        
        // Handle returning to chapter
        val handleBackToChapter: () -> Unit = {
            // Log chapter return flow
            Log.d("LessonScreen", "Starting chapter return flow...")
            
            // Emit lesson completed event
            Log.d("LessonScreen", "Emitting lesson completed event")
            onLessonCompleted()
            onNavigateBack()
        }
        
        // Handle reviewing lesson
        val handleReviewLesson: () -> Unit = {
            currentState = "content"
            viewModel.resetToFirstPage()
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
            
            // Scaffold with transparent background
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Lesson", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (uiState) {
                        is LessonUiState.Loading -> {
                            LessonLoadingView()
                        }
                        is LessonUiState.Error -> {
                            val error = (uiState as LessonUiState.Error).message
                            LessonErrorView(error = error) {
                                viewModel.loadLesson(lessonId)
                            }
                        }
                        is LessonUiState.Success -> {
                            val lesson = (uiState as LessonUiState.Success).lesson
                            
                            when (currentState) {
                                "overview" -> {
                                    LessonOverview(
                                        lesson = lesson,
                                        onStartLesson = handleStartLesson
                                    )
                                }
                                "content" -> {
                                    LessonContent(
                                        lesson = lesson,
                                        currentPage = currentPage,
                                        onPrevPage = handlePrevPage,
                                        onNextPage = handleNextPage,
                                        onFinish = handleFinishLesson
                                    )
                                }
                                "completed" -> {
                                    LessonCompleted(
                                        lesson = lesson,
                                        onBackToChapter = handleBackToChapter,
                                        onReviewLesson = handleReviewLesson,
                                        onDownloadPDF = downloadPdf,
                                        isDownloading = isDownloading,
                                        downloadProgress = downloadProgress
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonOverview(lesson: Lesson, onStartLesson: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lesson icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ECDC4).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Lesson",
                        tint = Color(0xFF4ECDC4),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lesson title
                Text(
                    text = lesson.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lesson description
                Text(
                    text = lesson.description,
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Reading time
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Duration",
                            tint = Color(0xFF666666)
                        )
                        Text(
                            text = "${lesson.duration} min",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Difficulty
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Difficulty",
                            tint = getDifficultyColor(lesson.difficulty)
                        )
                        Text(
                            text = getDifficultyText(lesson.difficulty),
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Pages
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Pages",
                            tint = Color(0xFF666666)
                        )
                        Text(
                            text = "${lesson.content.size} pages",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Start button
                Button(
                    onClick = onStartLesson,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
                ) {
                    Text(
                        text = "Start Lesson",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun LessonContent(
    lesson: Lesson,
    currentPage: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onFinish: () -> Unit
) {
    val isLastPage = currentPage == lesson.content.size - 1
    val progress = (currentPage + 1).toFloat() / lesson.content.size.toFloat()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Page indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page ${currentPage + 1} of ${lesson.content.size}",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress bar
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF4ECDC4),
                        trackColor = Color(0xFFF0F0F0)
                    )
                }
                
                Divider(color = Color(0xFFF0F0F0))
                
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        // Display the current page content
                        if (currentPage < lesson.content.size) {
                            MarkdownText(
                                markdown = lesson.content[currentPage],
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 16.sp,
                                color = Color(0xFF333333),
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                
                Divider(color = Color(0xFFF0F0F0))
                
                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous button
                    Button(
                        onClick = onPrevPage,
                        enabled = currentPage > 0,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPage > 0) Color(0xFFEEEEEE) else Color(0xFFF5F5F5),
                            contentColor = if (currentPage > 0) Color(0xFF666666) else Color(0xFFCCCCCC)
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }
                    
                    // Next or Finish button
                    Button(
                        onClick = if (isLastPage) onFinish else onNextPage,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastPage) Color(0xFF95CD41) else Color(0xFF4ECDC4)
                        )
                    ) {
                        Text(if (isLastPage) "Finish" else "Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (isLastPage) Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = if (isLastPage) "Finish" else "Next"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCompleted(
    lesson: Lesson,
    onBackToChapter: () -> Unit,
    onReviewLesson: () -> Unit,
    onDownloadPDF: () -> Unit,
    isDownloading: Boolean,
    downloadProgress: Float
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Completion icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Lesson Completed!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "You've successfully completed this lesson. Great job!",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Download PDF button (always show for completed lessons)
                Button(
                    onClick = onDownloadPDF,
                    enabled = !isDownloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Downloading... ${(downloadProgress * 100).toInt()}%")
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download PDF",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Review lesson button
                Button(
                    onClick = onReviewLesson,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review Lesson",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Back to chapter button
                Button(
                    onClick = onBackToChapter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7B7B))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Back to Chapter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LessonLoadingView() {
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
fun LessonErrorView(error: String, onRetry: () -> Unit) {
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
            color = Color.White,
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

// Helper function to get difficulty text
fun getDifficultyText(level: Int): String {
    return when (level) {
        1 -> "Beginner"
        2 -> "Easy"
        3 -> "Intermediate"
        4 -> "Advanced"
        5 -> "Expert"
        else -> "Unknown"
    }
}

// Helper function to get difficulty color
fun getDifficultyColor(level: Int): Color {
    return when (level) {
        1 -> Color(0xFF4CAF50) // Green for beginner
        2 -> Color(0xFF8BC34A) // Light green for easy
        3 -> Color(0xFFFFC107) // Amber for intermediate
        4 -> Color(0xFFFF9800) // Orange for advanced
        5 -> Color(0xFFF44336) // Red for expert
        else -> Color(0xFF9E9E9E) // Grey for unknown
    }
}
