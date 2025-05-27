package com.example.englishtek_mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.Quiz
import com.example.englishtek_mobile.data.model.QuizQuestion
import com.example.englishtek_mobile.data.model.QuizResult
import com.example.englishtek_mobile.data.model.Badge
import com.example.englishtek_mobile.data.repository.QuizRepository
import com.example.englishtek_mobile.data.repository.BadgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * UI state for the quiz screen
 */
sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Success(val quiz: Quiz) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

/**
 * State of the quiz flow
 */
enum class QuizState {
    OVERVIEW,
    QUESTION,
    RESULT
}

/**
 * ViewModel for the quiz screen
 */
class QuizViewModel(apiClient: ApiClient) : ViewModel() {
    private val repository = QuizRepository(apiClient.apiService)
    private val badgeRepository = BadgeRepository(apiClient.apiService)
    
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    private val _quizState = MutableStateFlow(QuizState.OVERVIEW)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()
    
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    private val _selectedAnswer = MutableStateFlow<List<String>>(emptyList())
    val selectedAnswer: StateFlow<List<String>> = _selectedAnswer.asStateFlow()
    
    private val _showingAnswer = MutableStateFlow(false)
    val showingAnswer: StateFlow<Boolean> = _showingAnswer.asStateFlow()
    
    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult.asStateFlow()
    
    private val _answers = mutableMapOf<String, String>()
    
    /**
     * Load quiz details
     */
    fun loadQuiz(quizId: String) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            repository.getQuiz(quizId).fold(
                onSuccess = { quiz ->
                    _uiState.value = QuizUiState.Success(quiz)
                },
                onFailure = { error ->
                    _uiState.value = QuizUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
    
    /**
     * Start the quiz
     */
    fun startQuiz(quizId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.startQuiz(quizId).fold(
                onSuccess = {
                    _quizState.value = QuizState.QUESTION
                    _currentQuestionIndex.value = 0
                    _answers.clear()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = QuizUiState.Error(error.message ?: "Failed to start quiz")
                }
            )
        }
    }
    
    /**
     * Select an answer for the current question
     */
    fun selectAnswer(answer: String) {
        val currentQuestion = getCurrentQuestion() ?: return
        
        if (_showingAnswer.value) return
        
        if (currentQuestion.type == "multiple_choice") {
            // For multiple choice, toggle the answer in the array
            val currentAnswers = _selectedAnswer.value.toMutableList()
            if (currentAnswers.contains(answer)) {
                currentAnswers.remove(answer)
            } else {
                currentAnswers.add(answer)
            }
            _selectedAnswer.value = currentAnswers
        } else {
            // For identification, just set the single answer
            _selectedAnswer.value = listOf(answer)
        }
    }
    
    /**
     * Submit the answer for the current question
     */
    fun submitAnswer() {
        val currentQuestion = getCurrentQuestion() ?: return
        
        // Store the answer
        currentQuestion.id?.let { questionId ->
            val answer = _selectedAnswer.value.joinToString(",")
            _answers[questionId] = answer
        }
        
        _showingAnswer.value = true
    }
    
    /**
     * Move to the next question or finish the quiz
     */
    fun nextQuestion(quizId: String) {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success) {
            val quiz = currentState.quiz
            val questions = quiz.questions ?: emptyList()
            
            _showingAnswer.value = false
            _selectedAnswer.value = emptyList()
            
            if (_currentQuestionIndex.value < questions.size - 1) {
                // Move to next question
                _currentQuestionIndex.value += 1
            } else {
                // End of quiz, submit results
                submitQuiz(quizId)
            }
        }
    }
    
    /**
     * Calculate the current score based on correct answers
     */
    fun calculateScore(): Int {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success) {
            val quiz = currentState.quiz
            val questions = quiz.questions ?: emptyList()
            var correctCount = 0
            
            Log.d("QuizViewModel", "ud83dudcca Starting score calculation for ${questions.size} questions")
            
            // Count correct answers
            questions.forEach { question ->
                question.id?.let { questionId ->
                    val userAnswer = _answers[questionId] ?: return@let
                    
                    Log.d("QuizViewModel", "ud83dudcca Processing question ID: $questionId, type: ${question.type}")
                    Log.d("QuizViewModel", "ud83dudcca User answer (raw): $userAnswer")
                    Log.d("QuizViewModel", "ud83dudcca Correct answer (raw): ${question.correctAnswer}")
                    Log.d("QuizViewModel", "ud83dudcca Correct answer type: ${question.correctAnswer?.javaClass?.simpleName}")
                    
                    if (question.type == "multiple_choice") {
                        // For multiple choice, correctAnswer is a List<String>
                        val correctAnswers = when (val answer = question.correctAnswer) {
                            is List<*> -> answer.filterIsInstance<String>().map { it.trim().lowercase() }
                            else -> emptyList()
                        }
                        
                        Log.d("QuizViewModel", "ud83dudcca Multiple choice - Processed correct answers: $correctAnswers")
                        
                        // Parse user answers
                        val userAnswers = userAnswer.split(",").map { it.trim().lowercase() }
                        Log.d("QuizViewModel", "ud83dudcca Multiple choice - User answers after splitting: $userAnswers")
                        
                        // Check if all selected answers match correct answers
                        val isCorrect = userAnswers.size == correctAnswers.size && 
                                userAnswers.all { userAns -> 
                                    val matchFound = correctAnswers.any { correctAns -> userAns == correctAns }
                                    Log.d("QuizViewModel", "ud83dudcca Checking if '$userAns' matches any correct answer: $matchFound")
                                    matchFound
                                }
                        
                        Log.d("QuizViewModel", "${if (isCorrect) "u2705" else "u274c"} Question $questionId is ${if (isCorrect) "correct" else "incorrect"}")
                        if (isCorrect) correctCount++
                    } else {
                        // For identification, correctAnswer is a single String
                        val correctAnswer = when (val answer = question.correctAnswer) {
                            is String -> answer.trim().lowercase()
                            else -> {
                                Log.e("QuizViewModel", "u274c Unexpected correct answer type for identification question")
                                return@let
                            }
                        }
                        
                        Log.d("QuizViewModel", "ud83dudcca Identification - Processed correct answer: '$correctAnswer'")
                        
                        // For identification, user selects a single answer
                        val userSelection = userAnswer.trim().lowercase()
                        Log.d("QuizViewModel", "ud83dudcca Identification - User answer after trimming: '$userSelection'")
                        
                        // Check if the user's answer matches the correct answer
                        val isCorrect = userSelection == correctAnswer
                        Log.d("QuizViewModel", "ud83dudcca Comparing '$userSelection' with '$correctAnswer': $isCorrect")
                        Log.d("QuizViewModel", "${if (isCorrect) "u2705" else "u274c"} Question $questionId is ${if (isCorrect) "correct" else "incorrect"}")
                        
                        if (isCorrect) correctCount++
                    }
                }
            }
            
            Log.d("QuizViewModel", "ud83dudcca Final score: $correctCount out of ${questions.size}")
            return correctCount
        }
        return 0
    }
    
    /**
     * Submit the quiz and get results
     */
    private fun submitQuiz(quizId: String) {
        viewModelScope.launch {
            // Calculate the score
            val score = calculateScore()
            
            repository.submitQuiz(quizId, _answers, score).fold(
                onSuccess = { result ->
                    _quizResult.value = result
                    _quizState.value = QuizState.RESULT
                },
                onFailure = { error ->
                    _uiState.value = QuizUiState.Error(error.message ?: "Failed to submit quiz")
                }
            )
        }
    }
    
    /**
     * Reset the quiz to try again
     */
    fun resetQuiz() {
        _quizState.value = QuizState.OVERVIEW
        _currentQuestionIndex.value = 0
        _selectedAnswer.value = emptyList()
        _showingAnswer.value = false
        _quizResult.value = null
        _answers.clear()
    }
    
    /**
     * Get the current question
     */
    fun getCurrentQuestion(): QuizQuestion? {
        val currentState = _uiState.value
        return if (currentState is QuizUiState.Success) {
            val quiz = currentState.quiz
            val questions = quiz.questions ?: emptyList()
            if (_currentQuestionIndex.value < questions.size) {
                questions[_currentQuestionIndex.value]
            } else {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Check if the current answer is correct
     */
    fun isCurrentAnswerCorrect(): Boolean {
        val currentQuestion = getCurrentQuestion() ?: return false
        val selectedAnswers = _selectedAnswer.value
        
        Log.d("QuizViewModel", "ud83dudcca Checking answer correctness for question type: ${currentQuestion.type}")
        Log.d("QuizViewModel", "ud83dudcca Selected answers: $selectedAnswers")
        Log.d("QuizViewModel", "ud83dudcca Correct answer (raw): ${currentQuestion.correctAnswer}")
        Log.d("QuizViewModel", "ud83dudcca Correct answer type: ${currentQuestion.correctAnswer?.javaClass?.simpleName}")
        
        return if (currentQuestion.type == "multiple_choice") {
            // For multiple choice, correctAnswer is a List<String>
            val correctAnswers = when (val answer = currentQuestion.correctAnswer) {
                is List<*> -> answer.filterIsInstance<String>().map { it.trim().lowercase() }
                else -> emptyList()
            }
            
            Log.d("QuizViewModel", "ud83dudcca Multiple choice - Processed correct answers: $correctAnswers")
            
            // All selected answers must match correct answers
            val isCorrect = selectedAnswers.size == correctAnswers.size && 
                    selectedAnswers.all { selected -> 
                        val trimmedSelected = selected.trim().lowercase()
                        val matchFound = correctAnswers.any { correct -> trimmedSelected == correct }
                        Log.d("QuizViewModel", "ud83dudcca Checking if '$trimmedSelected' matches any correct answer: $matchFound")
                        matchFound
                    }
            
            Log.d("QuizViewModel", "${if (isCorrect) "u2705" else "u274c"} Multiple choice answer is ${if (isCorrect) "correct" else "incorrect"}")
            isCorrect
        } else {
            // For identification, correctAnswer is a single String
            val correctAnswer = when (val answer = currentQuestion.correctAnswer) {
                is String -> answer.trim().lowercase()
                else -> {
                    Log.e("QuizViewModel", "u274c Unexpected correct answer type for identification question")
                    return false
                }
            }
            
            Log.d("QuizViewModel", "ud83dudcca Identification - Processed correct answer: '$correctAnswer'")
            
            // The single answer must match exactly
            val userAnswer = if (selectedAnswers.isNotEmpty()) selectedAnswers[0].trim().lowercase() else ""
            val isCorrect = selectedAnswers.size == 1 && userAnswer == correctAnswer
            
            Log.d("QuizViewModel", "ud83dudcca Comparing user answer '$userAnswer' with correct answer '$correctAnswer'")
            Log.d("QuizViewModel", "${if (isCorrect) "u2705" else "u274c"} Identification answer is ${if (isCorrect) "correct" else "incorrect"}")
            
            isCorrect
        }
    }
    
    /**
     * Set the quiz result directly
     * Used for showing completion screen for already completed quizzes
     */
    fun setQuizResult(result: QuizResult) {
        _quizResult.value = result
    }
    
    /**
     * Set the quiz state directly
     * Used for showing completion screen for already completed quizzes
     */
    fun setQuizState(state: QuizState) {
        _quizState.value = state
    }
    
    /**
     * Fetch a badge by ID
     * Used for showing the correct badge for completed quizzes
     */
    suspend fun getBadgeById(badgeId: String): Result<Badge> {
        return badgeRepository.getBadgeById(badgeId)
    }
}
