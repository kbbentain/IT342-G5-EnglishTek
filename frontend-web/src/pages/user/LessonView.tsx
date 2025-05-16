import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import {
  BookOpen,
  Clock,
  FileText,
  GraduationCap,
  ChevronLeft,
  ChevronRight,
  Check,
  Download,
  ArrowLeft
} from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { getLesson, startLesson, finishLesson, downloadLessonPDF } from '../../services/contentService';
import { calculateReadingTime } from '../../utils/helpers';

interface Lesson {
  id: number;
  chapterId: number;
  title: string;
  description: string;
  content: string[];
  completed?: boolean;
}

type LessonState = 'overview' | 'content' | 'completed';

const LessonView = () => {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const chapterId = new URLSearchParams(location.search).get('chapterId');
  
  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [currentState, setCurrentState] = useState<LessonState>('overview');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>(undefined);
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadProgress, setDownloadProgress] = useState(0);
  
  const contentRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadLesson();
  }, [id]);

  const loadLesson = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(undefined);
      const lessonData = await getLesson(Number(id));
      setLesson(lessonData);
    } catch (err) {
      setError('Failed to load lesson');
      console.error('Error loading lesson:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartLesson = async () => {
    if (!id) return;
    
    try {
      // Call the startLesson API endpoint
      await startLesson(Number(id));
      setCurrentState('content');
      // Scroll to top when starting lesson
      window.scrollTo(0, 0);
    } catch (err) {
      setError('Failed to start lesson');
      console.error('Error starting lesson:', err);
    }
  };

  const handleNextPage = () => {
    if (lesson && currentPage < lesson.content.length - 1) {
      setCurrentPage(prev => prev + 1);
      // Scroll to top when changing pages
      if (contentRef.current) {
        contentRef.current.scrollTop = 0;
      }
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(prev => prev - 1);
      // Scroll to top when changing pages
      if (contentRef.current) {
        contentRef.current.scrollTop = 0;
      }
    }
  };

  const handleBackToChapter = () => {
    if (chapterId) {
      navigate(`/chapter/${chapterId}`);
    } else {
      navigate('/home');
    }
  };

  const handleFinishLesson = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      // Call the finishLesson API endpoint (which uses the correct /finish endpoint now)
      const result = await finishLesson(Number(id));
      
      // Update lesson state
      setLesson(prev => prev ? { ...prev, completed: true } : null);
      setCurrentState('overview');
      
      // Show success message
      console.log('Lesson completed successfully:', result);
    } catch (err) {
      setError('Failed to complete lesson');
      console.error('Error completing lesson:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleReviewLesson = () => {
    setCurrentState('content');
    setCurrentPage(0);
    // Scroll to top when reviewing lesson
    window.scrollTo(0, 0);
  };

  const downloadPDF = async () => {
    if (!id) return;
    
    try {
      setIsDownloading(true);
      setDownloadProgress(0);
      
      // Get PDF using the API function
      const response = await downloadLessonPDF(Number(id));
      
      // Create a download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${lesson?.title || 'lesson'}.pdf`);
      document.body.appendChild(link);
      link.click();
      
      // Clean up
      window.URL.revokeObjectURL(url);
      document.body.removeChild(link);
      
      setDownloadProgress(1);
      // Show success message
      // In a real app, you might want to use a toast notification here
      console.log('PDF downloaded successfully');
    } catch (err) {
      setError('Failed to download PDF');
      console.error('Error downloading PDF:', err);
    } finally {
      setIsDownloading(false);
      setDownloadProgress(0);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-base-100 flex items-center justify-center">
        <div className="text-center">
          <span className="loading loading-spinner loading-lg text-primary"></span>
          <p className="mt-4">Loading lesson...</p>
        </div>
      </div>
    );
  }

  if (error || !lesson) {
    return (
      <div className="min-h-screen bg-base-100 flex items-center justify-center">
        <div className="text-center max-w-md">
          <div className="text-error text-6xl mb-4">&#x26A0;</div>
          <h2 className="text-2xl font-bold mb-4">{error || 'Lesson not found'}</h2>
          <button className="btn btn-primary" onClick={loadLesson}>Try Again</button>
          <button className="btn btn-ghost mt-2" onClick={handleBackToChapter}>Back to Chapter</button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-rose-100 to-teal-100 pb-8">
      {/* Header with back button */}
      <div className="bg-base-100 shadow-md sticky top-0 z-10">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center">
            <button 
              className="btn btn-circle btn-ghost mr-4" 
              onClick={handleBackToChapter}
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-2xl font-bold truncate">{lesson.title}</h1>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-6">
        {currentState === 'overview' && (
          <div className="card bg-base-100 shadow-xl">
            <div className="card-body items-center text-center">
              <div className="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center mb-4">
                <BookOpen className="w-10 h-10 text-primary" />
              </div>
              
              <h2 className="card-title text-2xl mb-2">{lesson.title}</h2>
              <p className="text-base-content/70 mb-6 max-w-2xl">{lesson.description}</p>
              
              <div className="flex flex-wrap justify-center gap-8 mb-8">
                <div className="flex flex-col items-center">
                  <div className="flex items-center justify-center w-12 h-12 rounded-full bg-primary/10 mb-2">
                    <Clock className="w-6 h-6 text-primary" />
                  </div>
                  <span className="text-sm">{calculateReadingTime(lesson.content)} read</span>
                </div>
                
                <div className="flex flex-col items-center">
                  <div className="flex items-center justify-center w-12 h-12 rounded-full bg-warning/10 mb-2">
                    <FileText className="w-6 h-6 text-warning" />
                  </div>
                  <span className="text-sm">{lesson.content.length} pages</span>
                </div>
                
                <div className="flex flex-col items-center">
                  <div className="flex items-center justify-center w-12 h-12 rounded-full bg-secondary/10 mb-2">
                    <GraduationCap className="w-6 h-6 text-secondary" />
                  </div>
                  <span className="text-sm">Learning</span>
                </div>
              </div>
              
              <div className="card-actions justify-center gap-4 flex-wrap">
                {lesson.completed ? (
                  <>
                    <button 
                      className="btn btn-primary gap-2" 
                      onClick={handleReviewLesson}
                    >
                      <BookOpen className="w-5 h-5" />
                      Review Lesson
                    </button>
                    
                    <button 
                      className="btn btn-warning gap-2" 
                      onClick={downloadPDF}
                      disabled={isDownloading}
                    >
                      {isDownloading ? (
                        <>
                          <span className="loading loading-spinner loading-xs"></span>
                          Downloading... {Math.round(downloadProgress * 100)}%
                        </>
                      ) : (
                        <>
                          <Download className="w-5 h-5" />
                          Download PDF
                        </>
                      )}
                    </button>
                    
                    <button 
                      className="btn btn-secondary gap-2" 
                      onClick={handleBackToChapter}
                    >
                      Back to Chapter
                    </button>
                  </>
                ) : (
                  <button 
                    className="btn btn-primary gap-2" 
                    onClick={handleStartLesson}
                  >
                    Start Lesson
                    <ChevronRight className="w-5 h-5" />
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
        
        {currentState === 'content' && (
          <div className="card bg-base-100 shadow-xl overflow-hidden">
            {/* Progress Header */}
            <div className="bg-base-200 p-4 border-b border-base-300">
              <div className="flex items-center gap-2 mb-2">
                <BookOpen className="w-5 h-5 text-primary" />
                <span className="text-sm font-medium">
                  Page {currentPage + 1} of {lesson.content.length}
                </span>
              </div>
              <div className="w-full bg-base-300 rounded-full h-2 overflow-hidden">
                <div 
                  className="bg-primary h-2 rounded-full" 
                  style={{ width: `${((currentPage + 1) / lesson.content.length) * 100}%` }}
                ></div>
              </div>
            </div>
            
            {/* Content */}
            <div 
              ref={contentRef}
              className="p-6 overflow-y-auto max-h-[60vh] md:max-h-[70vh]"
            >
              <div className="prose prose-lg max-w-none">
                <ReactMarkdown>
                  {lesson.content[currentPage]}
                </ReactMarkdown>
              </div>
            </div>
            
            {/* Navigation Buttons */}
            <div className="flex justify-between p-4 border-t border-base-300 bg-base-200">
              <button 
                className="btn btn-outline btn-neutral gap-2" 
                onClick={handlePrevPage}
                disabled={currentPage === 0}
              >
                <ChevronLeft className="w-5 h-5" />
                Previous
              </button>
              
              {currentPage === lesson.content.length - 1 ? (
                <button 
                  className="btn btn-success gap-2" 
                  onClick={handleFinishLesson}
                >
                  Complete
                  <Check className="w-5 h-5" />
                </button>
              ) : (
                <button 
                  className="btn btn-primary gap-2" 
                  onClick={handleNextPage}
                >
                  Next
                  <ChevronRight className="w-5 h-5" />
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default LessonView;
