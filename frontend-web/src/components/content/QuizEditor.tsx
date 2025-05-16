import { useState, useEffect } from 'react';
import { ChapterItem, Quiz, QuizQuestion, Badge, badgeService, quizService } from '../../services/contentService';
import BadgeCreationStep from './quiz/BadgeCreationStep';
import QuizDetailsStep from './quiz/QuizDetailsStep';
import QuizQuestionsStep from './quiz/QuizQuestionsStep';
import { X } from 'lucide-react';
import { getFullImageUrl } from '../../utils/urlUtils';

interface QuizEditorProps {
  chapterId: number;
  initialQuizId?: number;
  onClose: () => void;
  onSave: () => void;
}

type Step = 'badge' | 'details' | 'questions';

interface BadgeData {
  name: string;
  description: string;
  icon: File | null;
}

const QuizEditor = ({ chapterId, initialQuizId, onClose, onSave }: QuizEditorProps) => {
  const [currentStep, setCurrentStep] = useState<Step>('badge');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(!!initialQuizId);
  const [badgeData, setBadgeData] = useState<BadgeData>({
    name: '',
    description: '',
    icon: null,
  });
  const [badgeId, setBadgeId] = useState<number | null>(null);
  const [badgeIconUrl, setBadgeIconUrl] = useState<string | null>(null);
  const [quizDetails, setQuizDetails] = useState({
    title: '',
    description: '',
    difficulty: 1,
    isRandom: false,
  });
  const [questions, setQuestions] = useState<QuizQuestion[]>([]);

  // Fetch initial quiz and badge data if in edit mode
  useEffect(() => {
    const fetchInitialData = async () => {
      if (!initialQuizId) return;

      try {
        setInitialLoading(true);
        // Fetch quiz data
        const quiz = await quizService.getQuiz(initialQuizId);
        
        // Fetch badge data
        const badge = await badgeService.getBadge(quiz.badgeId);

        // Set states
        setBadgeId(quiz.badgeId);
        setBadgeIconUrl(badge.iconUrl);
        setBadgeData({
          name: badge.name,
          description: badge.description,
          icon: null, // We don't set the file since we can't get it back from the server
        });
        setQuizDetails({
          title: quiz.title,
          description: quiz.description,
          difficulty: quiz.difficulty,
          isRandom: quiz.isRandom || false,
        });
        setQuestions(quiz.questions);
      } catch (err) {
        setError('Failed to load quiz data');
        console.error(err);
      } finally {
        setInitialLoading(false);
      }
    };

    fetchInitialData();
  }, [initialQuizId]);

  const handleBadgeDataCollected = (data: BadgeData) => {
    setBadgeData(data);
    setCurrentStep('details');
  };

  const handleDetailsSubmitted = (details: typeof quizDetails) => {
    setQuizDetails(details);
    setCurrentStep('questions');
  };

  const handleQuestionsSubmitted = async (finalQuestions: QuizQuestion[]) => {
    try {
      setLoading(true);
      setError(null);

      let finalBadgeId = badgeId;

      // If we're editing or creating a new quiz, update the badge
      if (initialQuizId && badgeId) {
        // Update existing badge
        const badgeResponse = await badgeService.updateBadge(badgeId, {
          name: badgeData.name,
          description: badgeData.description,
          icon: badgeData.icon,
        });
        finalBadgeId = badgeResponse.id;
      } else {
        // Create new badge
        const badgeResponse = await badgeService.createBadge(badgeData);
        finalBadgeId = badgeResponse.id;
      }

      // Create/Update quiz
      const quizData = {
        chapterId,
        title: quizDetails.title,
        description: quizDetails.description,
        difficulty: quizDetails.difficulty,
        maxScore: finalQuestions.length,
        numberOfItems: finalQuestions.length,
        badgeId: finalBadgeId!,
        questions: finalQuestions,
        isRandom: quizDetails.isRandom,
      };

      if (initialQuizId) {
        await quizService.updateQuiz(initialQuizId, quizData);
      } else {
        await quizService.createQuiz(quizData);
      }

      onSave();
    } catch (err) {
      setError('Failed to save quiz');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (initialLoading) {
    return (
      <div className="fixed inset-0 bg-base-300 z-50 flex items-center justify-center">
        <div className="loading loading-spinner loading-lg"></div>
      </div>
    );
  }

  const renderStepContent = () => {
    if (initialLoading) {
      return (
        <div className="flex justify-center items-center py-12">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      );
    }

    switch (currentStep) {
      case 'badge':
        return (
          <BadgeCreationStep
            initialBadge={{
              name: badgeData.name,
              description: badgeData.description,
              iconUrl: badgeIconUrl,
            }}
            onNext={handleBadgeDataCollected}
            onBack={onClose}
          />
        );
      case 'details':
        return (
          <QuizDetailsStep
            initialDetails={quizDetails}
            onSubmit={handleDetailsSubmitted}
            onBack={() => setCurrentStep('badge')}
          />
        );
      case 'questions':
        return (
          <QuizQuestionsStep
            initialQuestions={questions}
            onSubmit={handleQuestionsSubmitted}
            onBack={() => setCurrentStep('details')}
            loading={loading}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="fixed inset-0 bg-base-300 z-50 overflow-y-auto">
      <div className="min-h-full p-4">
        <div className="max-w-4xl mx-auto bg-base-100 rounded-box shadow-lg">
          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-base-300">
            <h2 className="text-xl font-bold">
              {initialQuizId ? 'Edit Quiz' : 'Create Quiz'}
            </h2>
            <button
              className="btn btn-ghost btn-sm btn-square"
              onClick={onClose}
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Progress Bar */}
          <div className="w-full bg-base-300 h-1">
            <div
              className="bg-primary h-1 transition-all duration-300"
              style={{
                width:
                  currentStep === 'badge'
                    ? '33.33%'
                    : currentStep === 'details'
                    ? '66.66%'
                    : '100%',
              }}
            />
          </div>

          {/* Step Indicator */}
          <div className="flex justify-center p-4">
            <ul className="steps">
              <li className={`step ${currentStep === 'badge' ? 'step-primary' : ''}`}>
                {initialQuizId ? 'Edit Badge' : 'Create Badge'}
              </li>
              <li
                className={`step ${
                  currentStep === 'details' || currentStep === 'questions'
                    ? 'step-primary'
                    : ''
                }`}
              >
                Quiz Details
              </li>
              <li className={`step ${currentStep === 'questions' ? 'step-primary' : ''}`}>
                Questions
              </li>
            </ul>
          </div>

          {/* Error Message */}
          {error && (
            <div className="p-4">
              <div className="alert alert-error">
                <span>{error}</span>
              </div>
            </div>
          )}

          {/* Step Content */}
          <div className="p-4">{renderStepContent()}</div>
        </div>
      </div>
    </div>
  );
};

export default QuizEditor;
