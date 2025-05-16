import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen, BookCheck, Award, BarChart, Settings, LogOut, Clock } from 'lucide-react';
import { getChapters, getRecentActivities, getMyBadges } from '../../services/contentService';
import { logout } from '../../services/authService';
import type { Chapter, ActivityLog, ActivityType, Badge } from '../../types/api';
import { getFullImageUrl } from '../../utils/urlUtils';
import { formatPercentage } from '../../utils/helpers';

const getActivityIcon = (type: ActivityType) => {
  switch (type) {
    case 'quiz':
      return <BarChart className="w-5 h-5" />;
    case 'lesson':
      return <BookOpen className="w-5 h-5" />;
    case 'badge':
      return <Award className="w-5 h-5" />;
    default:
      return <BookCheck className="w-5 h-5" />;
  }
};

const getActivityColor = (type: ActivityType): { bg: string; fg: string } => {
  switch (type) {
    case 'quiz':
      return { bg: 'bg-error/10', fg: 'text-error' };
    case 'lesson':
      return { bg: 'bg-success/10', fg: 'text-success' };
    case 'badge':
      return { bg: 'bg-warning/10', fg: 'text-warning' };
    default:
      return { bg: 'bg-base-300', fg: 'text-base-content' };
  }
};

const getActivityDescription = (activity: ActivityLog): string => {
  const action = (() => {
    switch (activity.type) {
      case 'badge':
        return 'Earned the';
      case 'quiz':
        return 'Completed';
      case 'lesson':
        return 'Finished';
      default:
        return '';
    }
  })();

  // Extract any numbers from the name to use in the description
  const chapterMatch = activity.name.match(/Chapter (\d+)/i);
  const lessonMatch = activity.name.match(/Lesson (\d+)/i);
  const quizMatch = activity.name.match(/Quiz (\d+)/i);

  if (activity.type === 'badge') {
    return `${action} ${activity.name} Badge!`;
  } else if (activity.type === 'quiz') {
    const chapter = chapterMatch ? ` in Chapter ${chapterMatch[1]}` : '';
    const quiz = quizMatch ? `Quiz ${quizMatch[1]}` : activity.name;
    return `${action} ${quiz}${chapter}`;
  } else if (activity.type === 'lesson') {
    const chapter = chapterMatch ? ` in Chapter ${chapterMatch[1]}` : '';
    const lesson = lessonMatch ? `Lesson ${lessonMatch[1]}` : activity.name;
    return `${action} ${lesson}${chapter}`;
  }
  
  return activity.name;
};

const getRelativeTime = (dateStr: string): string => {
  try {
    const date = new Date(dateStr);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    // Less than a minute
    if (diffInSeconds < 60) {
      return 'just now';
    }

    // Less than an hour
    if (diffInSeconds < 3600) {
      const mins = Math.floor(diffInSeconds / 60);
      return `${mins} ${mins === 1 ? 'min' : 'mins'} ago`;
    }

    // Less than a day
    if (diffInSeconds < 86400) {
      const hrs = Math.floor(diffInSeconds / 3600);
      return `${hrs} ${hrs === 1 ? 'hr' : 'hrs'} ago`;
    }

    // Less than a month
    if (diffInSeconds < 2592000) {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days} ${days === 1 ? 'day' : 'days'} ago`;
    }

    // Less than a year
    if (diffInSeconds < 31536000) {
      const months = Math.floor(diffInSeconds / 2592000);
      return `${months} ${months === 1 ? 'month' : 'months'} ago`;
    }

    // More than a year
    const years = Math.floor(diffInSeconds / 31536000);
    return `${years} ${years === 1 ? 'year' : 'years'} ago`;
  } catch (error) {
    console.error('Error formatting date:', error);
    return 'recently';
  }
};

const Profile = () => {
  const navigate = useNavigate();
  const [userData, setUserData] = useState<any>(null);
  const [currentChapter, setCurrentChapter] = useState<Chapter | null>(null);
  const [activities, setActivities] = useState<ActivityLog[]>([]);
  const [badges, setBadges] = useState<Badge[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [userStats, setUserStats] = useState({
    totalBadges: 0,
    totalCompletedTasks: 0
  });

  const loadCurrentChapter = useCallback(async () => {
    try {
      const chapters = await getChapters();
      const inProgressChapter = chapters.find(chapter => chapter.status === 'IN_PROGRESS');
      if (inProgressChapter) {
        setCurrentChapter(inProgressChapter as any); 
      } else {
        setCurrentChapter(null);
      }
      return chapters;
    } catch (err) {
      console.error('Error loading current chapter:', err);
      return [];
    }
  }, []);

  const loadRecentActivities = useCallback(async () => {
    try {
      const activities = await getRecentActivities();
      setActivities(activities);
    } catch (err) {
      console.error('Error loading activities:', err);
    }
  }, []);

  const loadMyBadges = useCallback(async () => {
    try {
      const myBadges = await getMyBadges();
      setBadges(myBadges);
      return myBadges;
    } catch (err) {
      console.error('Error loading badges:', err);
      return [];
    }
  }, []);

  const calculateUserStats = useCallback(async () => {
    try {
      // Get badges count
      const myBadges = await loadMyBadges();
      
      // Get completed tasks from chapters
      const chapters = await getChapters();
      const completedTasks = chapters.reduce((total, chapter) => {
        return total + (chapter.completedTasks || 0);
      }, 0);
      
      // Update stats
      setUserStats({
        totalBadges: myBadges.length,
        totalCompletedTasks: completedTasks
      });
    } catch (err) {
      console.error('Error calculating user stats:', err);
    }
  }, [loadMyBadges]);

  const loadData = useCallback(async (isRefreshing = false) => {
    try {
      if (!isRefreshing) setLoading(true);
      await Promise.all([
        loadCurrentChapter(),
        loadRecentActivities(),
        calculateUserStats()
      ]);
      setError(null);
    } catch (err) {
      setError('Failed to load profile data');
      console.error('Error loading profile data:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [loadCurrentChapter, loadRecentActivities, calculateUserStats]);

  useEffect(() => {
    // Get user data from localStorage
    const user = localStorage.getItem('user');
    if (user) {
      setUserData(JSON.parse(user));
    }
    
    loadData();
  }, [loadData]);

  const handleRefresh = () => {
    setRefreshing(true);
    loadData(true);
  };

  const handleSettingsPress = () => {
    navigate('/settings');
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <span className="loading loading-spinner loading-lg text-primary"></span>
          <p className="mt-4">Loading your profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-base-100">
      <div className="container mx-auto max-w-6xl py-8 px-4">
        {error && (
          <div className="alert alert-error mb-6">
            <span>{error}</span>
            <button className="btn btn-sm" onClick={handleRefresh}>
              Retry
            </button>
          </div>
        )}

        <div className="flex flex-col md:flex-row gap-8">
          {/* Left Column - Profile Info */}
          <div className="md:w-1/3">
            <div className="card bg-base-100 shadow-xl">
              <div className="card-body items-center text-center">
                <div className="avatar mb-4">
                  <div className="w-32 rounded-full ring ring-primary ring-offset-base-100 ring-offset-2">
                    {userData?.avatarUrl ? (
                      <img 
                        src={getFullImageUrl(userData.avatarUrl) || ''} 
                        alt={userData.name} 
                      />
                    ) : (
                      <div className="bg-primary text-primary-content w-full h-full flex items-center justify-center text-3xl font-bold">
                        {userData?.name?.[0]?.toUpperCase() || 'U'}
                      </div>
                    )}
                  </div>
                </div>
                <h2 className="text-2xl font-bold">{userData?.name || 'User'}</h2>
                <p className="text-base-content/70">{userData?.email || 'user@example.com'}</p>
                
                {userData?.bio && (
                  <p className="text-sm italic mt-4 text-base-content/80">"{userData.bio}"</p>
                )}
                
                <div className="badge badge-outline mt-2">{userData?.role || 'USER'}</div>
                
                <div className="divider"></div>
                
                <div className="grid grid-cols-2 gap-4 w-full">
                  <div className="stat bg-base-200 rounded-box p-4">
                    <div className="stat-title">Badges</div>
                    <div className="stat-value text-primary">{userStats.totalBadges}</div>
                  </div>
                  
                  <div className="stat bg-base-200 rounded-box p-4">
                    <div className="stat-title">Tasks</div>
                    <div className="stat-value text-secondary">{userStats.totalCompletedTasks}</div>
                  </div>
                </div>
                
                <div className="card-actions mt-6 w-full">
                  <button 
                    className="btn btn-outline w-full"
                    onClick={handleSettingsPress}
                  >
                    <Settings className="w-4 h-4 mr-2" />
                    Settings
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          {/* Right Column - Activities and Current Chapter */}
          <div className="md:w-2/3">
            {/* Current Chapter */}
            {currentChapter && (
              <div className="card bg-base-100 shadow-xl mb-8">
                <div className="card-body">
                  <h2 className="card-title text-xl">Continue Learning</h2>
                  <div className="bg-base-200 rounded-xl p-4 mt-2">
                    <div className="flex items-center gap-4">
                      <div className="p-3 rounded-full bg-primary/20 text-primary">
                        <BookOpen className="w-6 h-6" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-bold">{currentChapter.title}</h3>
                        <div className="flex items-center mt-2">
                          <div className="w-full bg-base-300 rounded-full h-2">
                            <div 
                              className="bg-primary h-2 rounded-full" 
                              style={{ width: `${currentChapter.progressPercentage}%` }}
                            ></div>
                          </div>
                          <span className="ml-2 text-sm">
                            {formatPercentage(currentChapter.progressPercentage)}%
                          </span>
                        </div>
                      </div>
                      <button 
                        className="btn btn-primary btn-sm"
                        onClick={() => navigate(`/chapter/${currentChapter.id}`)}
                      >
                        Continue
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            {/* Recent Activities */}
            <div className="card bg-base-100 shadow-xl">
              <div className="card-body">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="card-title text-xl">Recent Activities</h2>
                  <button 
                    className="btn btn-ghost btn-sm"
                    onClick={handleRefresh}
                    disabled={refreshing}
                  >
                    {refreshing ? (
                      <span className="loading loading-spinner loading-sm"></span>
                    ) : (
                      <Clock className="w-4 h-4" />
                    )}
                    Refresh
                  </button>
                </div>
                
                {activities.length === 0 ? (
                  <div className="text-center py-8">
                    <BookOpen className="w-12 h-12 mx-auto text-base-content/30 mb-4" />
                    <p className="text-base-content/70">No activities yet. Start your learning journey!</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {activities.map((activity, index) => {
                      const colors = getActivityColor(activity.type);
                      return (
                        <div 
                          key={index}
                          className={`flex items-center gap-4 p-4 rounded-xl ${colors.bg}`}
                        >
                          <div className={`p-3 rounded-full bg-white ${colors.fg}`}>
                            {getActivityIcon(activity.type)}
                          </div>
                          <div className="flex-1">
                            <p className={`font-medium ${colors.fg}`}>
                              {getActivityDescription(activity)}
                            </p>
                            <p className="text-sm text-base-content/70">
                              {getRelativeTime(activity.date)}
                            </p>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
