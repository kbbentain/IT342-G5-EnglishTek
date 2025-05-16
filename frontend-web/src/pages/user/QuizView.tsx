import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Award,
  Check,
  CheckCircle,
  ChevronRight,
  XCircle,
  Star,
  ArrowLeft,
  RefreshCw,
  AlertTriangle
} from 'lucide-react';
import { getQuizDetails, startQuiz, submitQuiz } from '../../services/contentService';

interface Badge {
  id: number;
  name: string;
  description: string;
  iconUrl: string | null;
}

interface QuizDetails {
  id: number;
  chapterId: number;
  title: string;
  description: string;
  difficulty: number;
  maxScore: number;
  numberOfItems: number;
  badgeId: number;
  isRandom: boolean;
  questions: {
    id: number;
    page: number;
    type: 'identification' | 'multiple_choice';
    title: string;
    choices: string[];
    correct_answer: string | string[];
  }[];
  completed?: boolean;
}

interface QuizSubmitResponse {
  score: number;
  maxScore: number;
  badgeAwarded: boolean;
  isEligibleForRetake: boolean;
  badge?: Badge;
  passed: boolean; // Add passed property
}

type QuizState = 'overview' | 'question' | 'result';

const QuizView: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const realScore = useRef<number>(0);
  
  const [currentState, setCurrentState] = useState<QuizState>('overview');
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | string[]>('');
  const [showingAnswer, setShowingAnswer] = useState(false);
  const [quizDetails, setQuizDetails] = useState<QuizDetails | null>(null);
  const [quizResult, setQuizResult] = useState<QuizSubmitResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>(undefined);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadQuizDetails();
  }, [id]);

  const loadQuizDetails = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(undefined);
      // Get quiz details using the GET endpoint
      const details = await getQuizDetails(Number(id));
      setQuizDetails(details);
    } catch (err) {
      setError('Failed to load quiz');
      console.error('Error loading quiz:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartQuiz = async () => {
    if (!id) return;
    
    try {
      // Start the quiz using the POST endpoint
      await startQuiz(Number(id));
      setCurrentState('question');
      setCurrentQuestionIndex(0);
      realScore.current = 0;
      window.scrollTo(0, 0);
    } catch (err) {
      setError('Failed to start quiz');
      console.error('Error starting quiz:', err);
    }
  };

  const handleAnswerSelect = (answer: string) => {
    if (showingAnswer) return;
    
    const currentQuestion = quizDetails?.questions[currentQuestionIndex];
    if (!currentQuestion) return;
    
    if (currentQuestion.type === 'multiple_choice') {
      // For multiple choice, toggle the selected answer
      const currentAnswers = Array.isArray(selectedAnswer) ? [...selectedAnswer] : [];
      const index = currentAnswers.indexOf(answer);
      
      if (index === -1) {
        currentAnswers.push(answer);
      } else {
        currentAnswers.splice(index, 1);
      }
      
      setSelectedAnswer(currentAnswers);
    } else {
      // For identification, just set the selected answer
      setSelectedAnswer(answer);
    }
  };

  const handleSubmitAnswer = () => {
    if (!quizDetails) return;
    
    const currentQuestion = quizDetails.questions[currentQuestionIndex];
    const correctAnswer = currentQuestion.correct_answer;
    
    // Check if the answer is correct
    let isCorrect = false;
    
    if (currentQuestion.type === 'multiple_choice') {
      // For multiple choice, check if all selected answers are correct
      const selected = Array.isArray(selectedAnswer) ? selectedAnswer : [];
      const correct = Array.isArray(correctAnswer) ? correctAnswer : [correctAnswer];
      
      // Check if the selected answers match the correct answers
      isCorrect = selected.length === correct.length && 
                  selected.every(s => correct.includes(s));
    } else {
      // For identification, check if the selected answer is correct
      isCorrect = selectedAnswer === correctAnswer;
    }
    
    // Update score if correct
    if (isCorrect) {
      const questionScore = quizDetails.maxScore / quizDetails.questions.length;
      realScore.current += questionScore;
    }
    
    // Show the correct answer
    setShowingAnswer(true);
    
    // Move to the next question after a delay
    setTimeout(() => {
      if (currentQuestionIndex < quizDetails.questions.length - 1) {
        setCurrentQuestionIndex(prev => prev + 1);
        setSelectedAnswer('');
        setShowingAnswer(false);
      } else {
        handleSubmitQuiz();
      }
    }, 1500);
  };

  const handleSubmitQuiz = async () => {
    if (!id || !quizDetails) return;
    
    try {
      setSubmitting(true);
      
      // Calculate final score
      const finalScore = realScore.current;
      
      // Submit quiz and get results
      const result = await submitQuiz(Number(id), finalScore);
      
      // Add passed property based on score
      const passed = finalScore >= quizDetails.maxScore / 2;
      setQuizResult({
        ...result,
        passed
      });
      
      setCurrentState('result');
      window.scrollTo(0, 0);
    } catch (err) {
      setError('Failed to submit quiz');
      console.error('Error submitting quiz:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleRetry = async () => {
    if (!id) return;
    
    try {
      // Reset quiz state
      setCurrentState('overview');
      setCurrentQuestionIndex(0);
      setSelectedAnswer('');
      setShowingAnswer(false);
      realScore.current = 0;
      setQuizResult(null);
      
      // Reload quiz details
      await loadQuizDetails();
    } catch (err) {
      setError('Failed to reset quiz');
      console.error('Error resetting quiz:', err);
    }
  };

  const handleBackToChapter = () => {
    if (!quizDetails) return;
    navigate(`/chapter/${quizDetails.chapterId}`);
  };

  // Function to get full image URL
  const getFullImageUrl = (url: string | null | undefined): string | null => {
    if (!url) return null;
    
    // If the URL is already absolute, return it
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    
    // Otherwise, prepend the API URL
    const apiUrl = import.meta.env.VITE_API_URL || '';
    return `${apiUrl}${url}`;
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[50vh]">
        <span className="loading loading-spinner loading-lg text-primary"></span>
      </div>
    );
  }

  if (error || !quizDetails) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] p-4">
        <div className="alert alert-error shadow-lg max-w-md">
          <AlertTriangle className="w-6 h-6" />
          <span>{error || 'Failed to load quiz'}</span>
        </div>
        <button 
          className="btn btn-primary mt-4"
          onClick={() => navigate(-1)}
        >
          Go Back
        </button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {currentState === 'overview' && (
        <div className="card bg-base-100 shadow-xl overflow-hidden">
          <div className="card-body">
            <div className="flex items-center gap-2 mb-4">
              <Award className="w-6 h-6 text-primary" />
              <h2 className="card-title">{quizDetails.title}</h2>
            </div>
            
            <p className="mb-6">{quizDetails.description}</p>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <div className="flex items-center gap-2 p-4 bg-base-200 rounded-lg">
                <AlertTriangle className="w-5 h-5 text-warning" />
                <div>
                  <div className="text-sm font-medium">Difficulty</div>
                  <div className="font-bold">
                    {quizDetails.difficulty === 1 && 'Easy'}
                    {quizDetails.difficulty === 2 && 'Medium'}
                    {quizDetails.difficulty === 3 && 'Hard'}
                  </div>
                </div>
              </div>
              
              <div className="flex items-center gap-2 p-4 bg-base-200 rounded-lg">
                <Award className="w-5 h-5 text-info" />
                <div>
                  <div className="text-sm font-medium">Questions</div>
                  <div className="font-bold">{quizDetails.numberOfItems}</div>
                </div>
              </div>
              
              <div className="flex items-center gap-2 p-4 bg-base-200 rounded-lg">
                <Star className="w-5 h-5 text-accent" />
                <div>
                  <div className="text-sm font-medium">Badge Reward</div>
                  <div className="font-bold">{quizDetails.badgeId > 0 ? 'Yes' : 'No'}</div>
                </div>
              </div>
            </div>
            
            <div className="card-actions justify-center">
              {quizDetails.completed ? (
                <div className="flex flex-col items-center gap-4 w-full">
                  <div className="alert alert-success shadow-lg">
                    <div>
                      <CheckCircle className="w-6 h-6" />
                      <span>You've already completed this quiz!</span>
                    </div>
                  </div>
                  
                  {/* Display badge if the quiz has a badge */}
                  {quizDetails.badgeId > 0 && (
                    <div className="mb-4 p-6 bg-base-200 rounded-xl">
                      <h3 className="text-xl font-bold text-success mb-4">You earned a badge!</h3>
                      
                      <div className="flex flex-col items-center">
                        <div className="relative mb-4">
                          <div className="w-24 h-24 rounded-full bg-base-100 border-4 border-primary flex items-center justify-center overflow-hidden">
                            <img 
                              src={(getFullImageUrl(`/api/v1/badges/${quizDetails.badgeId}/icon`) || '')} 
                              alt="Badge"
                              className="w-16 h-16 object-contain"
                              onError={(e) => {
                                const target = e.target as HTMLImageElement;
                                target.src = `https://placehold.co/200x200/e2e8f0/64748b?text=Badge`;
                                target.onerror = null; // Prevent infinite loop
                              }}
                            />
                          </div>
                          <div className="absolute -top-2 -right-2 bg-white rounded-full p-1 shadow-md">
                            <Star className="w-6 h-6 text-yellow-500" fill="#FFD700" />
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                  
                  <button 
                    className="btn btn-primary gap-2" 
                    onClick={handleBackToChapter}
                  >
                    <ArrowLeft className="w-5 h-5" />
                    Go Back
                  </button>
                </div>
              ) : (
                <button 
                  className="btn btn-primary gap-2" 
                  onClick={handleStartQuiz}
                >
                  Start Quiz
                  <ChevronRight className="w-5 h-5" />
                </button>
              )}
            </div>
          </div>
        </div>
      )}
      
      {currentState === 'question' && (
        <div className="card bg-base-100 shadow-xl overflow-hidden">
          <div className="bg-base-200 p-4 border-b border-base-300">
            <div className="flex items-center gap-2 mb-2">
              <Award className="w-5 h-5 text-primary" />
              <span className="text-sm font-medium">
                Question {currentQuestionIndex + 1} of {quizDetails.questions.length}
              </span>
            </div>
            <div className="w-full bg-base-300 rounded-full h-2 overflow-hidden">
              <div 
                className="bg-primary h-2 rounded-full" 
                style={{ width: `${((currentQuestionIndex + 1) / quizDetails.questions.length) * 100}%` }}
              ></div>
            </div>
          </div>
          
          <div className="p-6">
            <div className="mb-6">
              <h3 className="text-xl font-bold mb-2">{quizDetails.questions[currentQuestionIndex].title}</h3>
              <p className="text-sm text-base-content/70">
                {quizDetails.questions[currentQuestionIndex].type === 'multiple_choice' 
                  ? '(Select all that apply)' 
                  : '(Choose the correct answer)'}
              </p>
            </div>
            
            <div className="space-y-3 mb-6">
              {quizDetails.questions[currentQuestionIndex].choices.map((choice) => {
                const currentQuestion = quizDetails.questions[currentQuestionIndex];
                const isMultipleChoice = currentQuestion.type === 'multiple_choice';
                
                const isSelected = isMultipleChoice
                  ? (Array.isArray(selectedAnswer) && selectedAnswer.includes(choice))
                  : selectedAnswer === choice;
                
                const isCorrect = isMultipleChoice
                  ? (Array.isArray(currentQuestion.correct_answer) && currentQuestion.correct_answer.includes(choice))
                  : choice === currentQuestion.correct_answer;
                
                return (
                  <div 
                    key={choice}
                    className={`
                      transition-all duration-200 transform hover:scale-[1.01] 
                      ${isSelected ? 'scale-[1.02]' : ''}
                    `}
                  >
                    <button
                      className={`
                        w-full p-4 rounded-lg border-2 flex items-center text-left
                        ${isSelected ? 'border-primary bg-primary/5' : 'border-base-300 bg-base-100'}
                        ${showingAnswer && isCorrect ? 'border-success bg-success/5' : ''}
                        ${showingAnswer && isSelected && !isCorrect ? 'border-error bg-error/5' : ''}
                        ${showingAnswer ? 'cursor-default' : 'hover:border-primary/50 cursor-pointer'}
                      `}
                      onClick={() => !showingAnswer && handleAnswerSelect(choice)}
                      disabled={showingAnswer}
                    >
                      {isMultipleChoice ? (
                        <div className={`
                          w-6 h-6 rounded border-2 flex items-center justify-center mr-3
                          ${isSelected ? 'border-primary bg-primary text-white' : 'border-base-300'}
                          ${showingAnswer && isCorrect ? 'border-success bg-success text-white' : ''}
                        `}>
                          {isSelected && <Check className="w-4 h-4" />}
                        </div>
                      ) : (
                        <div className={`
                          w-6 h-6 rounded-full border-2 flex items-center justify-center mr-3
                          ${isSelected ? 'border-primary bg-primary text-white' : 'border-base-300'}
                          ${showingAnswer && isCorrect ? 'border-success bg-success text-white' : ''}
                        `}>
                          {isSelected && <div className="w-2 h-2 rounded-full bg-white"></div>}
                        </div>
                      )}
                      
                      <span className="flex-1">{choice}</span>
                      
                      {showingAnswer && (
                        isCorrect ? (
                          <CheckCircle className="w-5 h-5 text-success ml-2" />
                        ) : (
                          isSelected && <XCircle className="w-5 h-5 text-error ml-2" />
                        )
                      )}
                    </button>
                  </div>
                );
              })}
            </div>
            
            <div className="card-actions justify-center">
              <button 
                className="btn btn-primary w-full"
                onClick={handleSubmitAnswer}
                disabled={!selectedAnswer || (Array.isArray(selectedAnswer) && selectedAnswer.length === 0) || showingAnswer}
              >
                {showingAnswer ? 'Next Question...' : 'Submit Answer'}
              </button>
            </div>
          </div>
        </div>
      )}
      
      {currentState === 'result' && quizResult && (
        <div className="card bg-base-100 shadow-xl overflow-hidden">
          <div className="card-body">
            <h2 className="card-title text-2xl mb-4 text-center">Quiz Results</h2>
            
            <div className="flex flex-col items-center mb-6">
              <div className="radial-progress text-primary mb-4" style={{ "--value": (quizResult.score / quizResult.maxScore) * 100 } as React.CSSProperties}>
                {Math.round((quizResult.score / quizResult.maxScore) * 100)}%
              </div>
              
              <div className="text-xl font-bold">
                {quizResult.score} / {quizResult.maxScore} points
              </div>
              
              <div className="mt-2 text-center">
                {quizResult.passed ? (
                  <div className="alert alert-success shadow-lg max-w-md">
                    <CheckCircle className="w-6 h-6" />
                    <span>Congratulations! You passed the quiz.</span>
                  </div>
                ) : (
                  <div className="alert alert-warning shadow-lg max-w-md">
                    <AlertTriangle className="w-6 h-6" />
                    <span>You didn't pass this time. Try again!</span>
                  </div>
                )}
              </div>
            </div>
            
            {quizResult.badgeAwarded && quizResult.badge && (
              <div className="mb-8 p-6 bg-base-200 rounded-xl">
                <h3 className="text-xl font-bold text-center text-success mb-4">You earned a badge!</h3>
                
                <div className="flex flex-col items-center">
                  <div className="relative mb-4">
                    <div className="w-24 h-24 rounded-full bg-base-100 border-4 border-primary flex items-center justify-center overflow-hidden">
                      <img 
                        src={(getFullImageUrl(quizResult.badge.iconUrl) || '')} 
                        alt={quizResult.badge.name || 'Badge'}
                        className="w-16 h-16 object-contain"
                        onError={(e) => {
                          const target = e.target as HTMLImageElement;
                          target.src = `https://placehold.co/200x200/e2e8f0/64748b?text=Badge`;
                          target.onerror = null; // Prevent infinite loop
                        }}
                      />
                    </div>
                    <div className="absolute -top-2 -right-2 bg-white rounded-full p-1 shadow-md">
                      <Star className="w-6 h-6 text-yellow-500" fill="#FFD700" />
                    </div>
                  </div>
                  
                  <div className="text-center">
                    <h4 className="font-bold text-lg">{quizResult.badge.name}</h4>
                    <p className="text-sm text-base-content/70">{quizResult.badge.description}</p>
                  </div>
                </div>
              </div>
            )}
            
            <div className="card-actions justify-center mt-6 flex flex-col items-center gap-4">
              {quizResult.passed ? (
                <button 
                  className="btn btn-primary gap-2" 
                  onClick={handleBackToChapter}
                >
                  <ArrowLeft className="w-5 h-5" />
                  Go Back
                </button>
              ) : (
                <button 
                  className="btn btn-primary gap-2" 
                  onClick={handleRetry}
                >
                  <RefreshCw className="w-5 h-5" />
                  Retake Quiz
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default QuizView;
