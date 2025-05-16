import { useEffect, useState } from 'react';
import DashboardLayout from '../components/DashboardLayout';
import { BarChart3, Users, BookOpen, Award, BookOpenCheck, Trophy, Medal } from 'lucide-react';
import { dashboardService, DashboardData } from '../services/dashboardService';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getFullImageUrl } from '../utils/urlUtils';
import { parseISO, addHours } from 'date-fns';

const Dashboard = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const data = await dashboardService.getDashboardData();
        setDashboardData(data);
      } catch (err) {
        setError('Failed to load dashboard data');
        console.error('Dashboard error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  // Transform activity data for the chart
  const activityData = dashboardData ? Object.entries(dashboardData.activityByDay).map(([date, count]) => ({
    date: addHours(parseISO(date), 8).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    count
  })) : [];

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center min-h-screen">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout>
        <div className="alert alert-error">
          <span>{error}</span>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      {/* Welcome Section with Total Users */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <div className="lg:col-span-2 card bg-base-100 shadow-xl">
          <div className="card-body">
            <h2 className="card-title text-2xl">
              Welcome back, {user.name}! 👋
            </h2>
            <p className="opacity-70">
              Here's what's happening with EnglishTek✨ today.
            </p>
          </div>
        </div>

        <div className="card shadow-xl relative overflow-hidden bg-gradient-to-br from-primary via-purple-600 to-blue-600 text-primary-content">
          <div className="absolute inset-0 bg-white/10 animate-pulse-slow"></div>
          <div className="absolute -inset-1 bg-gradient-to-r from-primary/20 to-blue-500/20 blur-xl"></div>
          <div className="card-body relative">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg">Total Users</h3>
                <p className="text-4xl font-bold mb-1">{dashboardData?.totalUsers || 0}</p>
                <p className="text-sm opacity-80">Active Community Members</p>
              </div>
              <div className="relative">
                <div className="absolute -inset-2 bg-white/20 rounded-full blur animate-pulse"></div>
                <Users className="w-14 h-14 opacity-90 relative" />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card bg-secondary text-secondary-content shadow-xl">
          <div className="card-body">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg">Chapters</h3>
                <p className="text-3xl font-bold">{dashboardData?.totalChapters || 0}</p>
              </div>
              <BookOpen className="w-12 h-12 opacity-80" />
            </div>
          </div>
        </div>

        <div className="card bg-accent text-accent-content shadow-xl">
          <div className="card-body">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg">Lessons</h3>
                <p className="text-3xl font-bold">{dashboardData?.totalLessons || 0}</p>
              </div>
              <BookOpenCheck className="w-12 h-12 opacity-80" />
            </div>
          </div>
        </div>

        <div className="card bg-info text-info-content shadow-xl">
          <div className="card-body">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg">Quizzes</h3>
                <p className="text-3xl font-bold">{dashboardData?.totalQuizzes || 0}</p>
              </div>
              <Award className="w-12 h-12 opacity-80" />
            </div>
          </div>
        </div>

        <div className="card bg-warning text-warning-content shadow-xl">
          <div className="card-body">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-lg">Badges Given</h3>
                <p className="text-3xl font-bold">{dashboardData?.totalBadgesGiven || 0}</p>
              </div>
              <Medal className="w-12 h-12 opacity-80" />
            </div>
          </div>
        </div>
      </div>

      {/* Analytics Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Activity Chart */}
        <div className="lg:col-span-2 card bg-base-100 shadow-xl">
          <div className="card-body">
            <h3 className="card-title">User Activity</h3>
            <div className="h-[400px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart
                  data={activityData}
                  margin={{
                    top: 10,
                    right: 30,
                    left: 0,
                    bottom: 30,
                  }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    angle={-45}
                    textAnchor="end"
                    height={70}
                  />
                  <YAxis />
                  <Tooltip />
                  <Area
                    type="monotone"
                    dataKey="count"
                    stroke="#8884d8"
                    fill="#8884d8"
                    fillOpacity={0.3}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Top Scorers */}
        <div className="card bg-base-100 shadow-xl">
          <div className="card-body">
            <h3 className="card-title">Top Scorers</h3>
            <div className="space-y-4 mt-4">
              {dashboardData?.topScorers.slice(0, 5).map((scorer, index) => (
                <div key={scorer.userId} className="flex items-center gap-4">
                  <div className="avatar">
                    <div className="w-12 h-12 rounded-full bg-base-300">
                      {scorer.avatarUrl ? (
                        <img
                          src={getFullImageUrl(scorer.avatarUrl)}
                          alt={scorer.username}
                          className="rounded-full"
                        />
                      ) : (
                        <div className="flex items-center justify-center w-full h-full">
                          <Users className="w-6 h-6 opacity-50" />
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="flex-1 flex items-center gap-2">
                    <div>
                      <div className="font-semibold flex items-center gap-2">
                        {scorer.username}
                        {index === 0 && (
                          <Trophy className="w-5 h-5 text-yellow-400" fill="currentColor" />
                        )}
                        {index === 1 && (
                          <Trophy className="w-5 h-5 text-gray-400" fill="currentColor" />
                        )}
                        {index === 2 && (
                          <Trophy className="w-5 h-5 text-amber-600" fill="currentColor" />
                        )}
                      </div>
                      <div className="text-sm opacity-70">Score: {scorer.totalScore}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Dashboard;
