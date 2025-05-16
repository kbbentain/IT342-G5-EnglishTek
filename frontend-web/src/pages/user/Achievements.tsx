import { useEffect, useState } from 'react';
import { Calendar, Lock, Star, RefreshCw } from 'lucide-react';
import { getAllBadges, getMyBadges } from '../../services/contentService';
import type { Badge } from '../../types/api';
import { getFullImageUrl } from '../../utils/urlUtils';

const Achievements = () => {
  const [badges, setBadges] = useState<Badge[]>([]);
  const [unlockedBadges, setUnlockedBadges] = useState<Badge[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBadges();
  }, []);

  const loadBadges = async (isRefreshing = false) => {
    try {
      if (!isRefreshing) setLoading(true);
      const [allBadges, myBadges] = await Promise.all([
        getAllBadges(),
        getMyBadges()
      ]);
      setBadges(allBadges);
      setUnlockedBadges(myBadges);
      setError(null);
    } catch (err) {
      setError('Failed to load badges. Please try again later!');
      console.error('Error loading badges:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadBadges(true);
  };

  const isUnlocked = (badgeId: number) => {
    return unlockedBadges.some(badge => badge.id === badgeId);
  };

  const getBadgeDate = (badgeId: number) => {
    const badge = unlockedBadges.find(b => b.id === badgeId);
    return badge?.dateObtained ? new Date(badge.dateObtained).toLocaleDateString() : null;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <span className="loading loading-spinner loading-lg text-primary"></span>
          <p className="mt-4">Loading your achievements...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center max-w-md">
          <div className="text-error mb-4 text-5xl">😕</div>
          <h2 className="text-2xl font-bold mb-4">Oops!</h2>
          <p className="mb-6">{error}</p>
          <button 
            className="btn btn-primary"
            onClick={() => loadBadges()}
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-base-100">
      <div className="bg-gradient-to-r from-warning/20 to-primary/20 py-12 px-8">
        <div className="container mx-auto max-w-6xl">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl md:text-4xl font-bold mb-2">My Achievements</h1>
              <p className="text-base-content/70">Collect them all! 🎖️</p>
            </div>
            <button 
              className="btn btn-outline"
              onClick={handleRefresh}
              disabled={refreshing}
            >
              {refreshing ? (
                <span className="loading loading-spinner loading-sm"></span>
              ) : (
                <RefreshCw className="w-4 h-4 mr-2" />
              )}
              Refresh
            </button>
          </div>
        </div>
      </div>

      <div className="container mx-auto max-w-6xl py-8 px-4">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {badges.map(badge => {
            const unlocked = isUnlocked(badge.id);
            const dateObtained = getBadgeDate(badge.id);
            
            return (
              <div 
                key={badge.id} 
                className={`card bg-base-100 shadow-xl overflow-hidden ${!unlocked ? 'opacity-75' : ''}`}
              >
                <figure className="relative">
                  <div className="w-full h-48 bg-gradient-to-br from-warning/30 to-primary/30 flex items-center justify-center">
                    <div className={`w-32 h-32 rounded-full flex items-center justify-center ${unlocked ? 'bg-white' : 'bg-base-300'}`}>
                      <img 
                        src={getFullImageUrl(badge.iconUrl) || ''}
                        alt={badge.name}
                        className={`w-24 h-24 object-contain ${!unlocked ? 'opacity-50 grayscale' : ''}`}
                      />
                    </div>
                    {!unlocked && (
                      <div className="absolute inset-0 bg-base-300/50 backdrop-blur-sm flex items-center justify-center">
                        <div className="bg-base-100 rounded-full p-4">
                          <Lock className="w-8 h-8 text-base-content/70" />
                        </div>
                      </div>
                    )}
                    {unlocked && (
                      <div className="absolute top-4 right-4 bg-warning text-warning-content rounded-full p-2">
                        <Star className="w-6 h-6" />
                      </div>
                    )}
                  </div>
                </figure>
                <div className="card-body">
                  <h2 className="card-title">{badge.name}</h2>
                  <p className={`${!unlocked ? 'text-base-content/50' : ''}`}>{badge.description}</p>
                  
                  {unlocked && dateObtained && (
                    <div className="flex items-center mt-2 text-success">
                      <Calendar className="w-4 h-4 mr-2" />
                      <span className="text-sm">Earned on {dateObtained}</span>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {badges.length === 0 && (
          <div className="text-center py-16">
            <div className="text-5xl mb-4">🏆</div>
            <h3 className="text-2xl font-bold mb-2">No Badges Available Yet</h3>
            <p className="text-base-content/70">Complete quizzes to earn badges!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Achievements;
