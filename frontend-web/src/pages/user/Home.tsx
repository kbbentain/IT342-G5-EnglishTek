import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen, Award, BookCheck, Check, Lock, ChevronRight, Activity } from 'lucide-react';
import { chapterService, getMyBadges } from '../../services/contentService';
import { getFullImageUrl } from '../../utils/urlUtils';
import { formatPercentage } from '../../utils/helpers';
import logo from '../../assets/Logo.png';

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

const Home = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [userProgress, setUserProgress] = useState({
    completedChapters: 0,
    completedTasks: 0,
    earnedBadges: 0
  });
  
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // Define status colors to match React Native app
  const CHAPTER_COLORS = {
    AVAILABLE: 'bg-rose-100 border-rose-300',
    IN_PROGRESS: 'bg-teal-100 border-teal-300',
    COMPLETED: 'bg-lime-100 border-lime-300',
    LOCKED: 'bg-gray-100 border-gray-300 opacity-70'
  };

  useEffect(() => {
    const fetchHomeData = async () => {
      try {
        setLoading(true);
        // Fetch chapters
        const chaptersData = await chapterService.getAllChapters();
        
        // Transform the API response to match our Chapter interface
        const formattedChapters: Chapter[] = chaptersData.map(chapter => ({
          id: chapter.id,
          title: chapter.title,
          description: chapter.description || '',
          iconUrl: chapter.iconUrl,
          totalTasks: chapter.totalTasks || 0,
          completedTasks: chapter.completedTasks || 0,
          progressPercentage: chapter.progressPercentage || 0,
          status: (chapter.status as ChapterStatus) || 'LOCKED',
          items: chapter.items || [],
          hasCompletedFeedback: chapter.hasCompletedFeedback || false
        }));
        
        setChapters(formattedChapters);
        
        // Calculate progress stats from the chapters data
        const completedChapters = formattedChapters.filter(c => c.status === 'COMPLETED').length;
        
        // Calculate total completed tasks across all chapters
        const completedTasks = formattedChapters.reduce((total, chapter) => {
          return total + (chapter.completedTasks || 0);
        }, 0);
        
        // Fetch badges
        const myBadges = await getMyBadges();
        
        setUserProgress({
          completedChapters,
          completedTasks,
          earnedBadges: myBadges.length
        });
        
        setError(null);
      } catch (err) {
        console.error('Error fetching home data:', err);
        setError('Failed to load home data');
      } finally {
        setLoading(false);
      }
    };

    fetchHomeData();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-base-100 flex items-center justify-center">
        <div className="text-center">
          <span className="loading loading-spinner loading-lg text-primary"></span>
          <p className="mt-4">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  // Function to get chapter status class based on status
  const getChapterStatusClass = (status: ChapterStatus) => {
    return CHAPTER_COLORS[status] || 'bg-base-200 border-base-300';
  };

  // Function to get status icon based on status and completed tasks
  const getStatusIcon = (status: ChapterStatus, completedTasks: number) => {
    if (status === 'LOCKED') return undefined;
    if (status === 'COMPLETED') return <Check className="w-5 h-5 text-lime-500" />;
    if (status === 'IN_PROGRESS' && completedTasks > 0) return <Activity className="w-5 h-5 text-teal-500" />;
    return undefined;
  };

  // Function to handle chapter click
  const handleChapterClick = (chapter: Chapter) => {
    // Don't navigate to locked chapters
    if (chapter.status === 'LOCKED') return;
    
    // Navigate to chapter details page
    navigate(`/chapter/${chapter.id}`);
  };

  return (
    <div className="min-h-screen bg-base-100">
      {/* Main Content */}
      <div className="container mx-auto px-4 py-8">
        {error && (
          <div className="alert alert-error mb-6">
            <span>{error}</span>
          </div>
        )}

        {/* Hero Section */}
        <section className="mb-12 hero bg-base-200 rounded-box p-8">
          <div className="hero-content flex-col lg:flex-row-reverse gap-8">
            <img 
              src={logo} 
              alt="EnglishTek Logo" 
              className="max-w-sm rounded-lg shadow-2xl" 
            />
            <div>
              <h1 className="text-4xl font-bold">Welcome back, {user.name || 'Learner'}!</h1>
              <p className="py-6">Continue your English language learning journey. Track your progress, earn badges, and master new skills at your own pace.</p>
              <button className="btn btn-primary" onClick={() => document.getElementById('learning-path')?.scrollIntoView({ behavior: 'smooth' })}>
                Continue Learning
              </button>
            </div>
          </div>
        </section>

        {/* Progress Stats */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold mb-6">Your Progress</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="card bg-primary/10 shadow-md">
              <div className="card-body">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="card-title">Chapters</h3>
                    <p className="text-3xl font-bold">{userProgress.completedChapters}</p>
                    <p className="text-sm opacity-70">completed</p>
                  </div>
                  <BookCheck className="w-12 h-12 text-primary opacity-70" />
                </div>
              </div>
            </div>

            <div className="card bg-secondary/10 shadow-md">
              <div className="card-body">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="card-title">Tasks</h3>
                    <p className="text-3xl font-bold">{userProgress.completedTasks}</p>
                    <p className="text-sm opacity-70">completed</p>
                  </div>
                  <BookOpen className="w-12 h-12 text-secondary opacity-70" />
                </div>
              </div>
            </div>

            <div className="card bg-accent/10 shadow-md">
              <div className="card-body">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="card-title">Badges</h3>
                    <p className="text-3xl font-bold">{userProgress.earnedBadges}</p>
                    <p className="text-sm opacity-70">earned</p>
                  </div>
                  <Award className="w-12 h-12 text-accent opacity-70" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Learning Path Section */}
        <section id="learning-path" className="mb-12">
          <h2 className="text-2xl font-bold mb-6">Learning Path</h2>
          <div className="space-y-6">
            {chapters.map((chapter, index) => (
              <div 
                key={chapter.id} 
                className={`card border-2 shadow-md hover:shadow-lg transition-all ${getChapterStatusClass(chapter.status)} relative cursor-pointer`}
                onClick={() => handleChapterClick(chapter)}
              >
                <div className="card-body">
                  <div className="flex items-center gap-4">
                    <div className="relative">
                      {chapter.iconUrl ? (
                        <img 
                          src={getFullImageUrl(chapter.iconUrl) || `https://placehold.co/100x100/e2e8f0/64748b?text=${index + 1}`} 
                          alt={chapter.title}
                          className="w-12 h-12 rounded-full object-cover border-2 border-white shadow-sm"
                          onError={(e) => {
                            const target = e.target as HTMLImageElement;
                            target.src = `https://placehold.co/100x100/e2e8f0/64748b?text=${index + 1}`;
                          }}
                        />
                      ) : (
                        <div className="bg-base-100 rounded-full w-12 h-12 flex items-center justify-center font-bold text-primary text-xl shadow-sm">
                          {index + 1}
                        </div>
                      )}
                      {getStatusIcon(chapter.status, chapter.completedTasks) && (
                        <div className="absolute -bottom-1 -right-1 bg-white rounded-full p-1 shadow-sm">
                          {getStatusIcon(chapter.status, chapter.completedTasks)}
                        </div>
                      )}
                    </div>
                    <div className="flex-1">
                      <h3 className="card-title text-xl">{chapter.title}</h3>
                      <p className="text-sm opacity-70">{chapter.description}</p>
                      
                      {/* Progress bar */}
                      {chapter.status !== 'LOCKED' && (
                        <div className="mt-3">
                          <div className="flex justify-between text-xs mb-1">
                            <span>Progress</span>
                            <span>{formatPercentage(chapter.progressPercentage)}%</span>
                          </div>
                          <div className="w-full bg-base-200 rounded-full h-2.5">
                            <div 
                              className="bg-primary h-2.5 rounded-full" 
                              style={{ width: `${chapter.progressPercentage}%` }}
                            ></div>
                          </div>
                          <div className="text-xs mt-1 opacity-70">
                            {chapter.completedTasks} / {chapter.totalTasks} Tasks
                          </div>
                        </div>
                      )}
                    </div>
                    <div>
                      {chapter.status === 'COMPLETED' ? (
                        <div className="badge badge-success gap-1">
                          <Check className="w-3 h-3" /> Completed
                        </div>
                      ) : chapter.status === 'IN_PROGRESS' ? (
                        <button 
                          className="btn btn-primary btn-sm gap-1"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleChapterClick(chapter);
                          }}
                        >
                          Continue <ChevronRight className="w-4 h-4" />
                        </button>
                      ) : chapter.status === 'AVAILABLE' ? (
                        <button 
                          className="btn btn-outline btn-primary btn-sm gap-1"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleChapterClick(chapter);
                          }}
                        >
                          Start <ChevronRight className="w-4 h-4" />
                        </button>
                      ) : (
                        <div className="badge badge-neutral gap-1">
                          <Lock className="w-3 h-3" /> Locked
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                {chapter.status === 'LOCKED' && (
                  <div className="absolute inset-0 bg-base-100/50 backdrop-blur-[1px] flex items-center justify-center rounded-lg">
                    <div className="text-center p-4">
                      <Lock className="w-8 h-8 mx-auto mb-2 text-neutral" />
                      <p className="text-sm font-medium">Complete previous chapter to unlock</p>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Home;
