import { useState } from 'react';
import { Quiz } from '../../services/contentService';
import { X, ChevronLeft, ChevronRight, Award, HelpCircle, CheckCircle, AlertCircle, Eye, PenTool } from 'lucide-react';
import { getFullImageUrl } from '../../utils/urlUtils';

interface PreviewQuizModalProps {
  isOpen: boolean;
  onClose: () => void;
  quiz: Quiz & { isRandom?: boolean };
  badge?: {
    name: string;
    description: string;
    iconUrl: string | null;
  } | null;
}

type QuizMode = 'preview' | 'answer';

const PreviewQuizModal = ({ isOpen, onClose, quiz, badge }: PreviewQuizModalProps) => {
  const [mode, setMode] = useState<QuizMode>('preview');
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [selectedAnswers, setSelectedAnswers] = useState<Record<number, string | string[]>>({});
  const [showResults, setShowResults] = useState(false);
  const [showAnswerFeedback, setShowAnswerFeedback] = useState(false);

  if (!isOpen) return null;

  const currentQuestion = quiz.questions[currentQuestionIndex];
  const isLastQuestion = currentQuestionIndex === quiz.questions.length - 1;

  const handleAnswerSelect = (choice: string) => {
    if (currentQuestion.type === 'identification') {
      setSelectedAnswers({
        ...selectedAnswers,
        [currentQuestionIndex]: choice
      });
    } else {
      // For multiple choice, toggle the selection
      const currentSelections = Array.isArray(selectedAnswers[currentQuestionIndex]) 
        ? selectedAnswers[currentQuestionIndex] as string[] 
        : [];
      
      const newSelections = currentSelections.includes(choice)
        ? currentSelections.filter(c => c !== choice)
        : [...currentSelections, choice];
      
      setSelectedAnswers({
        ...selectedAnswers,
        [currentQuestionIndex]: newSelections
      });
    }
  };

  const isAnswerCorrect = (questionIndex: number): boolean => {
    const question = quiz.questions[questionIndex];
    const selectedAnswer = selectedAnswers[questionIndex];

    if (!selectedAnswer) return false;

    if (question.type === 'identification') {
      return selectedAnswer === question.correct_answer;
    } else {
      const correctAnswers = Array.isArray(question.correct_answer) 
        ? question.correct_answer 
        : [question.correct_answer];
      const selectedAnswerArray = Array.isArray(selectedAnswer) ? selectedAnswer : [selectedAnswer];
      
      // Check if arrays have the same elements
      return correctAnswers.length === selectedAnswerArray.length &&
        correctAnswers.every(answer => selectedAnswerArray.includes(answer));
    }
  };

  const calculateScore = (): number => {
    return quiz.questions.reduce((score, _, index) => {
      return score + (isAnswerCorrect(index) ? 1 : 0);
    }, 0);
  };

  const handleShowAnswer = () => {
    setShowAnswerFeedback(true);
  };

  const handleNext = () => {
    if (mode === 'preview') {
      if (isLastQuestion) {
        setCurrentQuestionIndex(0);
      } else {
        setCurrentQuestionIndex(prev => prev + 1);
      }
      return;
    }

    // In answer mode
    if (showAnswerFeedback) {
      // If already showing feedback, move to next question or results
      setShowAnswerFeedback(false);
      if (isLastQuestion) {
        setShowResults(true);
      } else {
        setCurrentQuestionIndex(prev => prev + 1);
      }
    } else {
      // Show feedback first
      setShowAnswerFeedback(true);
    }
  };

  const resetQuiz = () => {
    setCurrentQuestionIndex(0);
    setSelectedAnswers({});
    setShowResults(false);
    setShowAnswerFeedback(false);
  };

  const switchMode = (newMode: QuizMode) => {
    setMode(newMode);
    resetQuiz();
  };

  // Render results screen
  if (showResults) {
    const score = calculateScore();
    const percentage = Math.round((score / quiz.questions.length) * 100);
    const passed = percentage >= 70; // Assuming 70% is passing

    return (
      <div className="fixed inset-0 bg-base-300/80 backdrop-blur-sm z-50 flex items-center justify-center overflow-y-auto py-8">
        <div className="bg-base-100 w-full max-w-2xl rounded-xl shadow-xl flex flex-col max-h-[90vh]">
          {/* Header */}
          <div className="p-6 border-b border-base-300 flex items-center justify-between">
            <h2 className="text-xl font-bold">Quiz Results</h2>
            <button
              onClick={onClose}
              className="btn btn-ghost btn-sm btn-circle"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Results */}
          <div className="flex-1 overflow-y-auto p-6">
            <div className="text-center mb-6">
              <div className={`text-6xl font-bold mb-2 ${passed ? 'text-success' : 'text-error'}`}>
                {percentage}%
              </div>
              <div className="text-xl">
                Score: {score} / {quiz.questions.length}
              </div>
              <div className="mt-4">
                {passed ? (
                  <div className="flex flex-col items-center text-success">
                    <CheckCircle className="w-16 h-16 mb-2" />
                    <p className="text-lg font-semibold">Congratulations!</p>
                  </div>
                ) : (
                  <div className="flex flex-col items-center text-error">
                    <AlertCircle className="w-16 h-16 mb-2" />
                    <p className="text-lg font-semibold">Try again!</p>
                  </div>
                )}
              </div>
            </div>

            {/* Badge earned (only show if passed) */}
            {passed && badge && (
              <div className="card bg-base-200 p-4 mb-6">
                <div className="flex items-center gap-4">
                  <div className="avatar">
                    <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
                      {badge.iconUrl ? (
                        <img 
                          src={getFullImageUrl(badge.iconUrl)} 
                          alt={badge.name} 
                          className="w-12 h-12 object-contain"
                        />
                      ) : (
                        <Award className="w-8 h-8 text-primary" />
                      )}
                    </div>
                  </div>
                  <div>
                    <h3 className="font-bold text-lg">Badge Earned: {badge.name}</h3>
                    <p className="text-sm opacity-70">{badge.description}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Question results */}
            <div className="space-y-4 mt-8">
              <h3 className="font-bold text-lg">Question Summary</h3>
              {quiz.questions.map((question, index) => (
                <div 
                  key={index} 
                  className={`p-4 rounded-lg border ${isAnswerCorrect(index) ? 'border-success bg-success/10' : 'border-error bg-error/10'}`}
                >
                  <div className="flex items-start gap-2">
                    {isAnswerCorrect(index) ? (
                      <CheckCircle className="w-5 h-5 text-success mt-0.5" />
                    ) : (
                      <AlertCircle className="w-5 h-5 text-error mt-0.5" />
                    )}
                    <div>
                      <p className="font-medium">{question.title}</p>
                      <p className="text-sm mt-1">
                        <span className="opacity-70">Your answer: </span>
                        <span className={isAnswerCorrect(index) ? 'text-success font-medium' : 'text-error font-medium'}>
                          {selectedAnswers[index] ? (
                            Array.isArray(selectedAnswers[index]) 
                              ? (selectedAnswers[index] as string[]).join(', ') 
                              : selectedAnswers[index]
                          ) : 'No answer'}
                        </span>
                      </p>
                      {!isAnswerCorrect(index) && (
                        <p className="text-sm mt-1">
                          <span className="opacity-70">Correct answer: </span>
                          <span className="text-success font-medium">
                            {Array.isArray(question.correct_answer) 
                              ? question.correct_answer.join(', ') 
                              : question.correct_answer}
                          </span>
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Footer */}
          <div className="p-4 border-t border-base-300 flex justify-between">
            <button
              className="btn btn-ghost"
              onClick={resetQuiz}
            >
              Try Again
            </button>
            <button
              className="btn btn-primary"
              onClick={onClose}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-base-300/80 backdrop-blur-sm z-50 flex items-center justify-center overflow-y-auto py-8">
      <div className="bg-base-100 w-full max-w-2xl rounded-xl shadow-xl flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-6 border-b border-base-300 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold">{quiz.title}</h2>
            <p className="text-base-content/70">{quiz.description}</p>
          </div>
          <button
            onClick={onClose}
            className="btn btn-ghost btn-sm btn-circle"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Mode Selector */}
        <div className="bg-base-200 p-2">
          <div className="join w-full">
            <button 
              className={`join-item btn flex-1 ${mode === 'preview' ? 'btn-active' : ''}`}
              onClick={() => switchMode('preview')}
            >
              <Eye className="w-4 h-4 mr-2" />
              Preview Mode
            </button>
            <button 
              className={`join-item btn flex-1 ${mode === 'answer' ? 'btn-active' : ''}`}
              onClick={() => switchMode('answer')}
            >
              <PenTool className="w-4 h-4 mr-2" />
              Answer Mode
            </button>
          </div>
        </div>

        {/* Quiz info */}
        <div className="bg-base-200 p-4 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <div>
              <span className="text-sm opacity-70">Difficulty</span>
              <div className="flex items-center gap-1">
                {[...Array(quiz.difficulty)].map((_, i) => (
                  <span key={i} className="w-2 h-2 bg-primary rounded-full"></span>
                ))}
                {[...Array(3 - quiz.difficulty)].map((_, i) => (
                  <span key={i} className="w-2 h-2 bg-base-300 rounded-full"></span>
                ))}
              </div>
            </div>
            <div>
              <span className="text-sm opacity-70">Questions</span>
              <div className="font-medium">{quiz.questions.length}</div>
            </div>
            {quiz.isRandom && (
              <div className="badge badge-primary">Randomized</div>
            )}
          </div>
          <div className="text-sm">
            Question {currentQuestionIndex + 1} of {quiz.questions.length}
          </div>
        </div>

        {/* Badge Information */}
        {badge && (
          <div className="bg-base-100 p-4 border-b border-base-300">
            <div className="flex items-center gap-4">
              <div className="avatar">
                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
                  {badge.iconUrl ? (
                    <img 
                      src={getFullImageUrl(badge.iconUrl)} 
                      alt={badge.name} 
                      className="w-8 h-8 object-contain"
                    />
                  ) : (
                    <Award className="w-6 h-6 text-primary" />
                  )}
                </div>
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <h3 className="font-bold">Badge: {badge.name}</h3>
                  <div className="badge badge-secondary badge-sm">Reward</div>
                </div>
                <p className="text-sm text-base-content/70">{badge.description}</p>
              </div>
            </div>
          </div>
        )}

        {/* Question */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="mb-6">
            <div className="flex items-start gap-2">
              <HelpCircle className="w-5 h-5 text-primary mt-1" />
              <h3 className="text-lg font-medium">{currentQuestion.title}</h3>
            </div>
          </div>

          {/* Answer Feedback */}
          {mode === 'answer' && showAnswerFeedback && (
            <div className={`p-4 mb-6 rounded-lg ${isAnswerCorrect(currentQuestionIndex) ? 'bg-success/10 border border-success' : 'bg-error/10 border border-error'}`}>
              <div className="flex items-start gap-3">
                {isAnswerCorrect(currentQuestionIndex) ? (
                  <CheckCircle className="w-5 h-5 text-success mt-0.5" />
                ) : (
                  <AlertCircle className="w-5 h-5 text-error mt-0.5" />
                )}
                <div>
                  <p className="font-medium">
                    {isAnswerCorrect(currentQuestionIndex) ? 'Correct!' : 'Incorrect'}
                  </p>
                  {!isAnswerCorrect(currentQuestionIndex) && (
                    <div className="mt-2">
                      <p className="text-sm">The correct answer is:</p>
                      <p className="font-medium text-success">
                        {Array.isArray(currentQuestion.correct_answer) 
                          ? currentQuestion.correct_answer.join(', ') 
                          : currentQuestion.correct_answer}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Choices */}
          <div className="space-y-3">
            {currentQuestion.choices.map((choice, index) => {
              // Determine if this choice is a correct answer
              const isCorrectAnswer = currentQuestion.type === 'identification' 
                ? currentQuestion.correct_answer === choice
                : Array.isArray(currentQuestion.correct_answer) && currentQuestion.correct_answer.includes(choice);
              
              return (
                <div 
                  key={index} 
                  className={`p-4 border rounded-lg ${mode === 'preview' ? '' : 'cursor-pointer transition-all hover:bg-base-200'} ${
                    mode === 'answer' && (
                      showAnswerFeedback && isCorrectAnswer
                        ? 'border-success bg-success/10'
                        : currentQuestion.type === 'identification'
                          ? selectedAnswers[currentQuestionIndex] === choice ? 'border-primary bg-primary/10' : 'border-base-300'
                          : Array.isArray(selectedAnswers[currentQuestionIndex]) && 
                            (selectedAnswers[currentQuestionIndex] as string[]).includes(choice)
                            ? 'border-primary bg-primary/10' 
                            : 'border-base-300'
                    )
                  }`}
                  onClick={() => mode === 'answer' && !showAnswerFeedback && handleAnswerSelect(choice)}
                >
                  <div className="flex items-center gap-3">
                    {mode === 'answer' && (
                      currentQuestion.type === 'identification' ? (
                        <div className={`w-5 h-5 rounded-full border flex-shrink-0 flex items-center justify-center ${
                          showAnswerFeedback && isCorrectAnswer
                            ? 'border-success bg-success'
                            : selectedAnswers[currentQuestionIndex] === choice 
                              ? 'border-primary bg-primary' 
                              : 'border-base-300'
                        }`}>
                          {(selectedAnswers[currentQuestionIndex] === choice || (showAnswerFeedback && isCorrectAnswer)) && (
                            <div className="w-2 h-2 bg-primary-content rounded-full"></div>
                          )}
                        </div>
                      ) : (
                        <div className={`w-5 h-5 rounded border flex-shrink-0 flex items-center justify-center ${
                          showAnswerFeedback && isCorrectAnswer
                            ? 'border-success bg-success'
                            : Array.isArray(selectedAnswers[currentQuestionIndex]) && 
                              (selectedAnswers[currentQuestionIndex] as string[]).includes(choice)
                              ? 'border-primary bg-primary' 
                              : 'border-base-300'
                        }`}>
                          {(Array.isArray(selectedAnswers[currentQuestionIndex]) && 
                           (selectedAnswers[currentQuestionIndex] as string[]).includes(choice) || 
                           (showAnswerFeedback && isCorrectAnswer)) && (
                            <CheckCircle className="w-3 h-3 text-primary-content" />
                          )}
                        </div>
                      )
                    )}
                    <span className={showAnswerFeedback && isCorrectAnswer ? 'text-success font-medium' : ''}>{choice}</span>
                    
                    {/* Show correct/incorrect indicators in preview mode */}
                    {mode === 'preview' && isCorrectAnswer && (
                      <CheckCircle className="w-4 h-4 text-success ml-auto" />
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-base-300 flex items-center justify-between">
          <button
            className="btn btn-ghost"
            onClick={() => setCurrentQuestionIndex(prev => Math.max(0, prev - 1))}
            disabled={currentQuestionIndex === 0 || (mode === 'answer' && showAnswerFeedback)}
          >
            <ChevronLeft className="w-4 h-4 mr-1" />
            Previous
          </button>
          
          {mode === 'answer' && !showAnswerFeedback ? (
            <button
              className="btn btn-primary"
              onClick={handleShowAnswer}
              disabled={!selectedAnswers[currentQuestionIndex]}
            >
              Check Answer
            </button>
          ) : (
            <button
              className="btn btn-primary"
              onClick={handleNext}
              disabled={mode === 'answer' && !showAnswerFeedback && !selectedAnswers[currentQuestionIndex]}
            >
              {mode === 'preview' 
                ? isLastQuestion ? 'Restart' : 'Next'
                : showAnswerFeedback 
                  ? isLastQuestion ? 'See Results' : 'Next Question' 
                  : 'Next'}
              {!isLastQuestion && <ChevronRight className="w-4 h-4 ml-1" />}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default PreviewQuizModal;
