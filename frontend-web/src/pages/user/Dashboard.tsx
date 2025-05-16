import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookOpen, Award, BookCheck, BarChart, User, LogOut } from 'lucide-react';
import { chapterService } from '../../services/contentService';
import { logout } from '../../services/authService';

const UserDashboard = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [chapters, setChapters] = useState<any[]>([]);
  const [userProgress, setUserProgress] = useState({
    completedChapters: 0,
    completedLessons: 0,
    completedQuizzes: 0,
    earnedBadges: 0,
    totalScore: 0
  });
  
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        // Fetch chapters
        const chaptersData = await chapterService.getAllChapters();
        setChapters(chaptersData);
        
        // In a real implementation, we would fetch user progress here
        // For now, we'll use placeholder data
        setUserProgress({
          completedChapters: 2,
          completedLessons: 8,
          completedQuizzes: 5,
          earnedBadges: 3,
          totalScore: 420
        });
        
        setError(null);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

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

  return (
    <div className="min-h-screen bg-base-100 flex flex-col">
      {/* Header */}
      <header className="bg-base-200 shadow-md">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <BookOpen className="w-8 h-8 text-primary" />
            <h1 className="text-xl font-bold">EnglishTek</h1>
          </div>
          
          <div className="flex items-center gap-4">
            <div className="dropdown dropdown-end">
              <div tabIndex={0} role="button" className="btn btn-ghost btn-circle avatar">
                <div className="w-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <span className="text-primary text-lg">{user.name?.[0]?.toUpperCase() || 'U'}</span>
                </div>
              </div>
              <ul tabIndex={0} className="mt-3 z-[1] p-2 shadow menu menu-sm dropdown-content bg-base-100 rounded-box w-52">
                <li><a><User className="w-4 h-4" /> Profile</a></li>
                <li><a onClick={handleLogout}><LogOut className="w-4 h-4" /> Logout</a></li>
              </ul>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 container mx-auto px-4 py-8">
        {error && (
          <div className="alert alert-error mb-6">
            <span>{error}</span>
          </div>
        )}

        {/* Welcome Section */}
        <section className="mb-8">
          <div className="card bg-base-100 shadow-xl">
            <div className="card-body">
              <h2 className="card-title text-2xl">Welcome back, {user.name || 'Learner'}! 👋</h2>
              <p className="opacity-70">Continue your English language learning journey.</p>
            </div>
          </div>
        </section>

        {/* Progress Stats */}
        <section className="mb-8">
          <h2 className="text-xl font-bold mb-4">Your Progress</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
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
                    <h3 className="card-title">Lessons</h3>
                    <p className="text-3xl font-bold">{userProgress.completedLessons}</p>
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
                    <h3 className="card-title">Quizzes</h3>
                    <p className="text-3xl font-bold">{userProgress.completedQuizzes}</p>
                    <p className="text-sm opacity-70">completed</p>
                  </div>
                  <BarChart className="w-12 h-12 text-accent opacity-70" />
                </div>
              </div>
            </div>

            <div className="card bg-warning/10 shadow-md">
              <div className="card-body">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="card-title">Badges</h3>
                    <p className="text-3xl font-bold">{userProgress.earnedBadges}</p>
                    <p className="text-sm opacity-70">earned</p>
                  </div>
                  <Award className="w-12 h-12 text-warning opacity-70" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Chapters Section */}
        <section className="mb-8">
          <h2 className="text-xl font-bold mb-4">Learning Path</h2>
          <div className="space-y-4">
            {chapters.map((chapter, index) => (
              <div key={chapter.id} className="card bg-base-100 shadow-md hover:shadow-lg transition-shadow cursor-pointer">
                <div className="card-body">
                  <div className="flex items-center gap-4">
                    <div className="bg-base-200 rounded-full w-10 h-10 flex items-center justify-center font-bold text-primary">
                      {index + 1}
                    </div>
                    <div>
                      <h3 className="card-title">{chapter.title}</h3>
                      <p className="text-sm opacity-70">{chapter.description}</p>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-base-200 py-6">
        <div className="container mx-auto px-4 text-center">
          <p className="text-sm opacity-70">&copy; 2025 EnglishTek. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default UserDashboard;
