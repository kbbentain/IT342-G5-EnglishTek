package com.example.englishtek_mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.Badge
import com.example.englishtek_mobile.data.model.Quiz
import com.example.englishtek_mobile.data.model.QuizQuestion
import com.example.englishtek_mobile.data.model.QuizResult
import com.example.englishtek_mobile.ui.viewmodels.QuizState
import com.example.englishtek_mobile.ui.viewmodels.QuizUiState
import com.example.englishtek_mobile.ui.viewmodels.QuizViewModel
import com.example.englishtek_mobile.ui.viewmodels.ViewModelFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.englishtek_mobile.ui.utils.getDifficultyColor
import com.example.englishtek_mobile.ui.utils.getDifficultyText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class QuizScreen(
    private val quizId: String,
    private val chapterId: String,
    private val apiClient: ApiClient,
    private val onNavigateBack: () -> Unit,
    private val onQuizCompleted: () -> Unit
) {
    @Composable
    fun Content() {
        val viewModel: QuizViewModel = viewModel(
            factory = ViewModelFactory(apiClient)
        )
        
        val uiState by viewModel.uiState.collectAsState()
        val quizState by viewModel.quizState.collectAsState()
        val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
        val selectedAnswer by viewModel.selectedAnswer.collectAsState()
        val showingAnswer by viewModel.showingAnswer.collectAsState()
        val quizResult by viewModel.quizResult.collectAsState()
        
        // Animation states
        val scaleAnim = remember { Animatable(1f) }
        val fadeAnim = remember { Animatable(1f) }
        
        // Load quiz when the screen is first displayed
        LaunchedEffect(quizId) {
            viewModel.loadQuiz(quizId)
        }
        
        // Check if quiz is already completed when it loads
        LaunchedEffect(uiState) {
            if (uiState is QuizUiState.Success) {
                val quiz = (uiState as QuizUiState.Success).quiz
                if (quiz.isCompleted && quizState == QuizState.OVERVIEW) {
                    // Fetch the actual badge for this quiz
                    quiz.badgeId?.let { badgeId ->
                        viewModel.getBadgeById(badgeId).fold(
                            onSuccess = { badge ->
                                // Create a result with the actual badge
                                val result = QuizResult(
                                    score = quiz.questions?.size ?: 0,
                                    maxScore = quiz.maxScore,
                                    isEligibleForRetake = false,
                                    isEligibleForBadge = true,
                                    badge = badge,
                                    badgeAwarded = true
                                )
                                viewModel.setQuizResult(result)
                                viewModel.setQuizState(QuizState.RESULT)
                            },
                            onFailure = { error ->
                                // If badge fetch fails, use a default badge
                                val defaultBadge = Badge(
                                    id = quiz.badgeId,
                                    name = "Quiz Badge",
                                    description = "You've already completed this quiz!",
                                    level = "Gold",
                                    iconUrl = null,
                                    dateObtained = null
                                )
                                val result = QuizResult(
                                    score = quiz.questions?.size ?: 0,
                                    maxScore = quiz.maxScore,
                                    isEligibleForRetake = false,
                                    isEligibleForBadge = true,
                                    badge = defaultBadge,
                                    badgeAwarded = true
                                )
                                viewModel.setQuizResult(result)
                                viewModel.setQuizState(QuizState.RESULT)
                            }
                        )
                    } ?: run {
                        // If no badgeId is available, use a default badge
                        val defaultBadge = Badge(
                            id = "",
                            name = "Quiz Badge",
                            description = "You've already completed this quiz!",
                            level = "Gold",
                            iconUrl = null,
                            dateObtained = null
                        )
                        val result = QuizResult(
                            score = quiz.questions?.size ?: 0,
                            maxScore = quiz.maxScore,
                            isEligibleForRetake = false,
                            isEligibleForBadge = true,
                            badge = defaultBadge,
                            badgeAwarded = true
                        )
                        viewModel.setQuizResult(result)
                        viewModel.setQuizState(QuizState.RESULT)
                    }
                }
            }
        }
        
        // Animation functions
        val animateSelection: (String) -> Unit = { answer ->
            viewModel.selectAnswer(answer)
        }
        
        val animateTransition: () -> Unit = {
            viewModel.nextQuestion(quizId)
        }
        
        val startQuizWithAnimation: () -> Unit = {
            viewModel.startQuiz(quizId) {}
        }
        
        // Animation effects
        LaunchedEffect(selectedAnswer) {
            if (selectedAnswer.isNotEmpty()) {
                scaleAnim.animateTo(1.1f, spring(stiffness = 400f))
                scaleAnim.animateTo(1f, spring(stiffness = 400f))
            }
        }
        
        LaunchedEffect(currentQuestionIndex) {
            fadeAnim.animateTo(0f, tween(150))
            delay(150)
            fadeAnim.animateTo(1f, tween(150))
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
                                Color(0xFFFFD166).copy(alpha = 0.9f),
                                Color(0xFFFFA726).copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            
            // Scaffold with transparent background
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Quiz", color = Color.White) },
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
                        is QuizUiState.Loading -> {
                            QuizLoadingView()
                        }
                        is QuizUiState.Error -> {
                            val error = (uiState as QuizUiState.Error).message
                            QuizErrorView(error = error) {
                                viewModel.loadQuiz(quizId)
                            }
                        }
                        is QuizUiState.Success -> {
                            val quiz = (uiState as QuizUiState.Success).quiz
                            
                            when (quizState) {
                                QuizState.OVERVIEW -> {
                                    QuizOverview(
                                        quiz = quiz,
                                        onStartQuiz = startQuizWithAnimation
                                    )
                                }
                                QuizState.QUESTION -> {
                                    val currentQuestion = viewModel.getCurrentQuestion()
                                    if (currentQuestion != null) {
                                        QuizQuestion(
                                            quiz = quiz,
                                            question = currentQuestion,
                                            currentQuestionIndex = currentQuestionIndex,
                                            selectedAnswers = selectedAnswer,
                                            showingAnswer = showingAnswer,
                                            isCorrect = viewModel.isCurrentAnswerCorrect(),
                                            onAnswerSelected = animateSelection,
                                            onSubmitAnswer = { viewModel.submitAnswer() },
                                            onNextQuestion = animateTransition,
                                            fadeAnim = fadeAnim.value,
                                            scaleAnim = scaleAnim.value
                                        )
                                    }
                                }
                                QuizState.RESULT -> {
                                    quizResult?.let { result ->
                                        QuizResultView(
                                            result = result,
                                            onRetry = { viewModel.resetQuiz() },
                                            onBackToChapter = onNavigateBack
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
}

@Composable
fun QuizOverview(quiz: Quiz, onStartQuiz: () -> Unit) {
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
                // Quiz icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ECDC4).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = "Quiz",
                        tint = Color(0xFF4ECDC4),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quiz title
                Text(
                    text = quiz.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quiz description
                Text(
                    text = quiz.description,
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
                    // Questions count
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = "Questions",
                            tint = Color(0xFF666666)
                        )
                        Text(
                            text = "${quiz.questionCount} questions",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // Estimated time
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Time",
                            tint = Color(0xFF666666)
                        )
                        Text(
                            text = "${quiz.questionCount * 2} min", 
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
                            tint = getDifficultyColor(quiz.difficulty)
                        )
                        Text(
                            text = getDifficultyText(quiz.difficulty),
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Start button
                Button(
                    onClick = onStartQuiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4))
                ) {
                    Text(
                        text = "Start Quiz",
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
fun QuizQuestion(
    quiz: Quiz,
    question: QuizQuestion,
    currentQuestionIndex: Int,
    selectedAnswers: List<String>,
    showingAnswer: Boolean,
    isCorrect: Boolean,
    onAnswerSelected: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    fadeAnim: Float,
    scaleAnim: Float
) {
    val isMultipleChoice = question.type == "multiple_choice"
    
    // Handle correctAnswer which can be String, List<String>, or null
    val correctAnswers = when (val answer = question.correctAnswer) {
        is String -> if (isMultipleChoice) answer.split(",") else listOf(answer)  
        is List<*> -> answer.filterIsInstance<String>()
        else -> emptyList()
    }
    
    Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = fadeAnim }, contentAlignment = Alignment.Center) {
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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1).toFloat() / (quiz.questions?.size ?: 1).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4ECDC4),
                    trackColor = Color(0xFFE0E0E0)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Question counter
                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${quiz.questions?.size ?: 0}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Question text
                Text(
                    text = question.text ?: "Question text not available",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Answer choices
                Column(modifier = Modifier.fillMaxWidth()) {
                    question.choices?.forEach { choice ->
                        val isSelected = selectedAnswers.contains(choice)
                        // For identification questions, check exact match
                        // For multiple choice, check if the choice is in the correctAnswers list
                        val isCorrectChoice = if (isMultipleChoice) {
                            correctAnswers.contains(choice)
                        } else {
                            // For identification, the correct answer is the first (and only) element
                            correctAnswers.isNotEmpty() && choice == correctAnswers[0]
                        }
                        
                        val backgroundColor = when {
                            !showingAnswer -> if (isSelected) Color(0xFFE3F2FD) else Color.White
                            isCorrectChoice -> Color(0xFFE8F5E9)  // Green background for correct
                            isSelected && !isCorrectChoice -> Color(0xFFFFEBEE)  // Red background for incorrect
                            else -> Color.White
                        }
                        
                        val borderColor = when {
                            !showingAnswer -> if (isSelected) Color(0xFF4ECDC4) else Color(0xFFE0E0E0)
                            isCorrectChoice -> Color(0xFF4CAF50)  // Green border for correct
                            isSelected && !isCorrectChoice -> Color(0xFFF44336)  // Red border for incorrect
                            else -> Color(0xFFE0E0E0)
                        }
                        
                        val textColor = when {
                            !showingAnswer -> if (isSelected) Color(0xFF4ECDC4) else Color(0xFF333333)
                            isCorrectChoice -> Color(0xFF4CAF50)  // Green text for correct
                            isSelected && !isCorrectChoice -> Color(0xFFF44336)  // Red text for incorrect
                            else -> Color(0xFF333333)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !showingAnswer) { onAnswerSelected(choice) }
                                .scale(if (isSelected) scaleAnim else 1f)
                                .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = backgroundColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox or radio button
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(if (isMultipleChoice) RoundedCornerShape(4.dp) else CircleShape)
                                        .background(if (isSelected) borderColor else Color.Transparent)
                                        .border(
                                            width = 2.dp,
                                            color = borderColor,
                                            shape = if (isMultipleChoice) RoundedCornerShape(4.dp) else CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = if (isMultipleChoice) Icons.Default.Check else Icons.Default.Circle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Choice text
                                Text(
                                    text = choice,
                                    fontSize = 16.sp,
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit or Next button
                Button(
                    onClick = if (showingAnswer) onNextQuestion else onSubmitAnswer,
                    enabled = selectedAnswers.isNotEmpty() || showingAnswer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showingAnswer) {
                            if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                        } else {
                            Color(0xFF4ECDC4)
                        },
                        disabledContainerColor = Color(0xFFE0E0E0)
                    )
                ) {
                    Text(
                        text = if (showingAnswer) {
                            if (isCorrect) "Correct! Next Question" else "Incorrect. Next Question"
                        } else {
                            "Submit Answer"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultView(result: QuizResult, onRetry: () -> Unit, onBackToChapter: () -> Unit) {
    val scorePercentage = (result.score.toFloat() / result.maxScore.toFloat() * 100).toInt()
    
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
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Result title
                Text(
                    text = if (result.badgeAwarded) "Congratulations! 🎉" else "Keep Trying! 💪",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.badgeAwarded) Color(0xFF4CAF50) else Color(0xFFF44336),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Score
                Text(
                    text = "Your Score: ($scorePercentage%)",
                    fontSize = 20.sp,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Badge earned (if any)
                if (result.badgeAwarded && result.badge != null) {
                    Text(
                        text = "🎉 Congratulations! You earned a badge!",
                        fontSize = 18.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Badge image
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color(0xFF4ECDC4), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // In a real app, load the badge image from URL
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Badge",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(64.dp)
                        )
                        
                        // Sparkle icon
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(8.dp, (-8).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .shadow(4.dp, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Sparkle",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Badge name
                    Text(
                        text = result.badge.name ?: "Badge",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Badge description
                    Text(
                        text = result.badge.description ?: "",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (result.isEligibleForRetake && !result.badgeAwarded) {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                    
                    Button(
                        onClick = onBackToChapter,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (result.isEligibleForRetake && !result.badgeAwarded) 8.dp else 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ECDC4)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Back to Chapter")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizLoadingView() {
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
fun QuizErrorView(error: String, onRetry: () -> Unit) {
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
