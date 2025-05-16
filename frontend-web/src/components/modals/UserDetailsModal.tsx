import { User } from '../../services/userService';
import { Medal, BookOpen, GraduationCap, Brain, Clock, Download } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { getFullImageUrl, BACKEND_URL } from '../../utils/urlUtils';

interface UserDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: User | null;
}

const UserDetailsModal = ({ isOpen, onClose, user }: UserDetailsModalProps) => {
  if (!isOpen || !user) return null;

  const handleDownloadReport = async () => {
    try {
      // Get auth token from localStorage
      const token = localStorage.getItem('token');
      if (!token) throw new Error('No authentication token found');

      const response = await fetch(`${BACKEND_URL}/api/v1/report/${user.id}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/pdf'
        },
      });

      if (!response.ok) throw new Error('Failed to download report');
      
      const blob = await response.blob();
      // Verify the content type
      if (!blob.type.includes('pdf')) {
        throw new Error('Invalid file format received');
      }
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `user-report-${user.username}-${new Date().toISOString().split('T')[0]}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Error downloading report:', error);
      // You might want to show an error message to the user
      alert('Failed to download report. Please try again.');
    }
  };

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-2xl">
        {/* Header */}
        <div className="flex items-start gap-4 mb-6">
          <div className="avatar">
            <div className="w-16 h-16 rounded-full bg-primary/10">
              {user.avatarUrl ? (
                <img src={getFullImageUrl(user.avatarUrl)} alt={user.name || user.username} />
              ) : (
                <span className="text-primary text-2xl flex items-center justify-center h-full">
                  {(user.name || user.username)[0]?.toUpperCase()}
                </span>
              )}
            </div>
          </div>
          <div className="flex-1">
            <h3 className="text-xl font-bold">{user.name || user.username}</h3>
            <div className="flex flex-col gap-1 text-base-content/70">
              <p>ID: {user.id}</p>
              <p>Username: {user.username}</p>
              <p>{user.email}</p>
            </div>
            <div className="flex items-center gap-2 mt-2">
              <span className={`badge ${user.role === 'ADMIN' ? 'badge-primary' : 'badge-secondary'}`}>
                {user.role}
              </span>
              <div className="flex items-center gap-1 text-base-content/60 text-sm">
                <Clock className="w-4 h-4" />
                <span>Joined {formatDistanceToNow(new Date(user.createdAt))} ago</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bio */}
        {user.bio && (
          <div className="mb-6">
            <h4 className="font-medium mb-2">Bio</h4>
            <p className="text-base-content/70">{user.bio}</p>
          </div>
        )}

        {/* Progress Stats */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="stat bg-base-200 rounded-box">
            <div className="stat-figure text-warning">
              <Medal className="w-8 h-8 opacity-80" />
            </div>
            <div className="stat-title">Total Badges</div>
            <div className="stat-value">{user.totalBadges}</div>
          </div>
          <div className="stat bg-base-200 rounded-box">
            <div className="stat-figure text-primary">
              <BookOpen className="w-8 h-8 opacity-80" />
            </div>
            <div className="stat-title">Completed Chapters</div>
            <div className="stat-value">{user.completedChapters}</div>
          </div>
          <div className="stat bg-base-200 rounded-box">
            <div className="stat-figure text-success">
              <GraduationCap className="w-8 h-8 opacity-80" />
            </div>
            <div className="stat-title">Completed Lessons</div>
            <div className="stat-value">{user.completedLessons}</div>
          </div>
          <div className="stat bg-base-200 rounded-box">
            <div className="stat-figure text-info">
              <Brain className="w-8 h-8 opacity-80" />
            </div>
            <div className="stat-title">Completed Quizzes</div>
            <div className="stat-value">{user.completedQuizzes}</div>
          </div>
        </div>

        {/* Actions */}
        <div className="modal-action">
          <button 
            className="btn btn-primary gap-2" 
            onClick={handleDownloadReport}
          >
            <Download className="w-4 h-4" />
            Download Report
          </button>
          <button className="btn" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserDetailsModal;
