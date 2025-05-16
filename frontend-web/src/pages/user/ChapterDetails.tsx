import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { BookOpen, Award, Check, Lock, ChevronRight, ArrowLeft } from 'lucide-react';
import { getChapterDetails, submitFeedback } from '../../services/contentService';
import { formatPercentage } from '../../utils/helpers';
import FeedbackModal from '../../components/FeedbackModal';
import toast from 'react-hot-toast';

// Define the ChapterStatus type to match the API
type ChapterStatus = 'AVAILABLE' | 'LOCKED' | 'IN_PROGRESS' | 'COMPLETED';

// Define the ChapterItem interface
interface ChapterItem {
  id: number;
  type: 'lesson' | 'quiz';
  title: string;
  order: number;
  completed?: boolean;
}

// Define the Chapter interface to match the API response
interface Chapter {
  id: number;
  title: string;
  description: string;
  iconUrl?: string;
  totalTasks: number;
  completedTasks: number;
  progressPercentage: number;
  status: ChapterStatus;
  items: ChapterItem[];
  hasCompletedFeedback?: boolean;
}

const ChapterDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState<Chapter | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>(undefined);
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);
  const [feedbackError, setFeedbackError] = useState<string | null>(null);
  
  // Define status colors to match React Native app but with Tailwind classes
  const ITEM_COLORS = {
    lesson: {
      locked: 'bg-gray-200 border-gray-300',
      available: 'bg-rose-400 border-rose-500',
      completed: 'bg-lime-400 border-lime-500'
    },
    quiz: {
      locked: 'bg-gray-200 border-gray-300',
      available: 'bg-teal-400 border-teal-500',
      completed: 'bg-lime-400 border-lime-500'
    }
  };

  // Define status icons to match React Native app
  const ITEM_ICONS = {
    lesson: {
      locked: <Lock className="w-8 h-8 text-white" />,
      available: <BookOpen className="w-8 h-8 text-white" />,
      completed: <Check className="w-8 h-8 text-white" />
    },
    quiz: {
      locked: <Lock className="w-8 h-8 text-white" />,
      available: <Award className="w-8 h-8 text-white" />,
      completed: <Check className="w-8 h-8 text-white" />
    }
  };

  // Helper function to get full image URL
  const getChapterImageUrl = (url: string | undefined): string => {
    if (!url) return '';
    
    // If it's already a full URL, return as is
    if (url.startsWith('http')) return url;
    
    // Otherwise, prepend the API URL
    const apiUrl = import.meta.env.VITE_API_URL || 'https://api-englishtek.aetherrflare.org';
    return `${apiUrl}${url}`;
  };

  const loadChapter = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(undefined);
      const chapterData = await getChapterDetails(Number(id));
      
      // Log the chapter data to debug
      console.log('Chapter data:', chapterData);
      console.log('Icon URL:', chapterData.iconUrl);
      console.log('Full image URL:', getChapterImageUrl(chapterData.iconUrl));
      
      // Transform the API response to match our Chapter interface
      const formattedChapter: Chapter = {
        id: chapterData.id,
        title: chapterData.title,
        description: chapterData.description || '',
        iconUrl: chapterData.iconUrl,
        totalTasks: chapterData.totalTasks || chapterData.items?.length || 0,
        completedTasks: chapterData.completedTasks || 0,
        progressPercentage: chapterData.progressPercentage || 0,
        status: (chapterData.status as ChapterStatus) || 'AVAILABLE',
        items: chapterData.items || [],
        hasCompletedFeedback: chapterData.hasCompletedFeedback || false
      };
      
      setChapter(formattedChapter);
      
      // Check if we should show the feedback modal
      if (formattedChapter.items.every(item => item.completed) && !formattedChapter.hasCompletedFeedback) {
        setShowFeedbackModal(true);
      }
    } catch (err) {
      setError('Failed to load chapter');
      console.error('Error loading chapter:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadChapter();
  }, [id]);

  const getItemStatus = (item: ChapterItem, index: number, items: ChapterItem[]): 'locked' | 'available' | 'completed' => {
    if (item.completed) {
      return 'completed';
    }
    
    // First item is always available
    if (index === 0) {
      return 'available';
    }
    
    // Item is available if previous item is completed
    if (index > 0 && items[index - 1].completed) {
      return 'available';
    }
    
    return 'locked';
  };

  const handleItemClick = (item: ChapterItem, index: number) => {
    // Check if any previous items are not completed
    const previousItems = chapter?.items.slice(0, index) || [];
    const allPreviousCompleted = previousItems.every(prevItem => prevItem.completed);
    
    // If this is not the first item and previous items are not all completed, don't allow access
    if (index > 0 && !allPreviousCompleted) {
      // Could show a toast or alert here
      return;
    }
    
    // Navigate to the appropriate page based on item type
    if (item.type === 'lesson') {
      navigate(`/lesson/${item.id}`);
    } else {
      navigate(`/quiz/${item.id}`);
    }
  };

  // Handle feedback submission
  const handleFeedbackSubmit = async (feedback: {
    rating: number;
    selectedWord: string;
    additionalFeedback: string;
  }) => {
    try {
      setFeedbackError(null);
      await submitFeedback({
        chapterId: Number(id),
        rating: feedback.rating,
        feedbackKeyword: feedback.selectedWord,
        feedbackText: feedback.additionalFeedback || '',
      });
      
      // Update the chapter data to reflect that feedback has been submitted
      if (chapter) {
        setChapter({
          ...chapter,
          hasCompletedFeedback: true
        });
      }
      
      // Show success toast
      toast.success('Thank you for your feedback!');
    } catch (err: any) {
      console.error('Error submitting feedback:', err);
      setFeedbackError(err.message || 'Failed to submit feedback');
      toast.error(err.message || 'Failed to submit feedback');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-base-100 flex items-center justify-center">
        <div className="text-center">
          <span className="loading loading-spinner loading-lg text-primary"></span>
          <p className="mt-4">Loading chapter...</p>
        </div>
      </div>
    );
  }

  if (error || !chapter) {
    return (
      <div className="min-h-screen bg-base-100 flex items-center justify-center">
        <div className="text-center max-w-md">
          <div className="text-error text-6xl mb-4">&#x26A0;</div>
          <h2 className="text-2xl font-bold mb-4">{error || 'Chapter not found'}</h2>
          <button className="btn btn-primary" onClick={loadChapter}>Try Again</button>
          <button className="btn btn-ghost mt-2" onClick={() => navigate('/home')}>Back to Home</button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-rose-100 to-teal-100">
      {/* Header with back button */}
      <div className="container mx-auto px-4 py-6">
        <div className="flex items-center mb-6">
          <button 
            className="btn btn-circle btn-ghost mr-4" 
            onClick={() => navigate('/home')}
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="text-3xl font-bold">{chapter.title}</h1>
        </div>

        {/* Chapter Info Card */}
        <div className="card bg-base-100 shadow-xl mb-8">
          <div className="card-body items-center text-center">
            {chapter.iconUrl ? (
              <div className="w-24 h-24 rounded-full overflow-hidden border-4 border-white shadow-md mb-4">
                <img 
                  src={getChapterImageUrl(chapter.iconUrl)} 
                  alt={chapter.title}
                  className="w-full h-full object-cover"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    console.error('Image failed to load:', chapter.iconUrl);
                    target.src = `https://placehold.co/200x200/e2e8f0/64748b?text=${chapter.title[0]}`;
                  }}
                />
              </div>
            ) : (
              <div className="w-24 h-24 rounded-full bg-primary flex items-center justify-center text-primary-content text-3xl font-bold mb-4">
                {chapter.title[0]}
              </div>
            )}
            <h2 className="card-title text-2xl mb-2">{chapter.title}</h2>
            <p className="text-base-content/70 mb-6">{chapter.description}</p>
            
            {/* Progress Bar */}
            <div className="w-full max-w-md mb-4">
              <div className="flex justify-between mb-1 text-sm">
                <span>Progress</span>
                <span>{chapter.completedTasks} / {chapter.totalTasks} Completed ({formatPercentage(chapter.progressPercentage)}%)</span>
              </div>
              <div className="w-full bg-base-300 rounded-full h-2.5 overflow-hidden">
                <div 
                  className="bg-primary h-2.5 rounded-full" 
                  style={{ width: `${chapter.progressPercentage}%` }}
                ></div>
              </div>
            </div>
          </div>
        </div>

        {/* Learning Path */}
        <div className="card bg-base-100 shadow-xl mb-8">
          <div className="card-body">
            <h2 className="card-title text-xl mb-6">Learning Path</h2>
            
            <div className="space-y-8 relative">
              {/* Connecting Line */}
              <div className="absolute left-6 top-6 bottom-6 w-0.5 bg-base-300 z-0"></div>
              
              {chapter.items.map((item, index) => {
                const status = getItemStatus(item, index, chapter.items);
                const isLocked = status === 'locked';
                const isAvailable = status === 'available';
                const isCompleted = status === 'completed';
                
                return (
                  <div 
                    key={item.id} 
                    className={`relative z-10 ${isLocked ? 'opacity-60' : ''}`}
                    onClick={() => !isLocked && handleItemClick(item, index)}
                  >
                    <div className="flex">
                      {/* Icon Circle */}
                      <div className={`
                        w-12 h-12 rounded-full flex items-center justify-center 
                        ${ITEM_COLORS[item.type][status]} border-2 shadow-md
                        flex-shrink-0 z-10
                      `}>
                        {ITEM_ICONS[item.type][status]}
                      </div>
                      
                      {/* Content Card */}
                      <div className="
                        ml-4 flex-grow bg-white rounded-lg shadow-md p-4 
                        border-l-4 border-r-4 
                        ${ITEM_COLORS[item.type][status].replace('bg-', 'border-')}
                      ">
                        <div className="flex justify-between items-start">
                          <div>
                            <h3 className="font-bold text-lg mb-1">{item.title}</h3>
                            <div className="flex items-center text-xs text-base-content/70">
                              <span className="badge badge-sm">
                                {item.type === 'lesson' ? (
                                  <>
                                    <BookOpen className="w-4 h-4 mr-1" />
                                    Lesson
                                  </>
                                ) : (
                                  <>
                                    <Award className="w-4 h-4 mr-1" />
                                    Quiz
                                  </>
                                )}
                              </span>
                              <span className="mx-2 text-base-content/30">•</span>
                              <span className={`${isCompleted ? 'text-success font-medium' : isAvailable ? 'text-primary font-medium' : 'text-base-content/50'}`}>
                                {isCompleted ? 'Completed' : isAvailable ? 'Available' : 'Locked'}
                              </span>
                            </div>
                          </div>
                          
                          {/* Action Button */}
                          <div>
                            {isCompleted ? (
                              <div className="badge badge-success gap-1 p-3 shadow-sm">
                                <Check className="w-4 h-4" /> Completed
                              </div>
                            ) : isAvailable ? (
                              <button 
                                className="btn btn-primary btn-sm shadow-sm" 
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleItemClick(item, index);
                                }}
                              >
                                {item.type === 'lesson' ? 'Start Lesson' : 'Take Quiz'} 
                                <ChevronRight className="w-4 h-4" />
                              </button>
                            ) : (
                              <div className="badge badge-neutral gap-1 p-3 shadow-sm">
                                <Lock className="w-4 h-4" /> Locked
                              </div>
                            )}
                          </div>
                        </div>
                        
                        {/* Progress Bar - Only for available or completed items */}
                        {!isLocked && (
                          <div className="mt-3">
                            <div className="w-full bg-base-200 rounded-full h-2 mt-2 overflow-hidden">
                              <div 
                                className={`h-full ${isCompleted ? 'bg-success' : 'bg-primary'}`}
                                style={{ width: isCompleted ? '100%' : '0%' }}
                              ></div>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Feedback Modal */}
        {showFeedbackModal && (
          <FeedbackModal
            visible={showFeedbackModal}
            onClose={() => setShowFeedbackModal(false)}
            onSubmit={handleFeedbackSubmit}
            error={feedbackError}
          />
        )}
      </div>
    </div>
  );
};

export default ChapterDetails;
