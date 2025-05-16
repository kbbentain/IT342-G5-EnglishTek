import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Edit, Lock, FileText, HelpCircle, ArrowLeft } from 'lucide-react';
import { getFullImageUrl } from '../../utils/urlUtils';

const Settings = () => {
  const navigate = useNavigate();
  const [userData, setUserData] = useState<any>(null);
  const [isDownloading, setIsDownloading] = useState(false);

  useEffect(() => {
    // Get user data from localStorage
    const user = localStorage.getItem('user');
    if (user) {
      setUserData(JSON.parse(user));
    }
  }, []);

  // Logout functionality moved to sidebar
  // Sound toggle functionality removed - not currently used in UI

  const handleDownloadReport = async () => {
    try {
      setIsDownloading(true);
      // Implement report download functionality
      setTimeout(() => {
        setIsDownloading(false);
        // Show success toast/notification
      }, 2000);
    } catch (error) {
      console.error('Error downloading report:', error);
      setIsDownloading(false);
    }
  };

  return (
    <div className="min-h-screen bg-base-100">
      <div className="container mx-auto max-w-4xl py-8 px-4">
        {/* Header */}
        <div className="flex items-center mb-6">
          <button 
            className="btn btn-circle btn-ghost mr-4" 
            onClick={() => navigate(-1)}
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="text-3xl font-bold">Settings</h1>
        </div>

        {/* Settings Card */}
        <div className="card bg-base-100 shadow-xl mb-8">
          <div className="card-body">
            <div className="p-4 bg-base-200 rounded-lg mb-6">
              <div className="flex items-center gap-4">
                <div className="avatar">
                  <div className="w-16 h-16 rounded-full">
                    {userData?.avatarUrl ? (
                      <img src={getFullImageUrl(userData.avatarUrl) || ''} alt={userData?.name} />
                    ) : (
                      <div className="bg-primary text-primary-content w-full h-full flex items-center justify-center text-2xl font-bold">
                        {userData?.name?.[0]?.toUpperCase() || 'U'}
                      </div>
                    )}
                  </div>
                </div>
                <div>
                  <h2 className="text-xl font-bold">{userData?.name || 'User'}</h2>
                  <p className="text-base-content/70">{userData?.email || 'user@example.com'}</p>
                </div>
              </div>
            </div>

            {/* Account Settings */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold mb-3">Account Settings</h3>
              <div className="divide-y divide-base-300">
                <button 
                  className="flex items-center justify-between w-full py-4 px-2 hover:bg-base-200 rounded-lg transition-colors"
                  onClick={() => navigate('/profile/edit')}
                >
                  <div className="flex items-center">
                    <Edit className="w-5 h-5 mr-3 text-primary" />
                    <span>Edit Profile</span>
                  </div>
                  <div className="text-base-content/50">
                    <ArrowLeft className="w-5 h-5 rotate-180" />
                  </div>
                </button>

                <button 
                  className="flex items-center justify-between w-full py-4 px-2 hover:bg-base-200 rounded-lg transition-colors"
                  onClick={() => navigate('/settings/change-password')}
                >
                  <div className="flex items-center">
                    <Lock className="w-5 h-5 mr-3 text-primary" />
                    <span>Change Password</span>
                  </div>
                  <div className="text-base-content/50">
                    <ArrowLeft className="w-5 h-5 rotate-180" />
                  </div>
                </button>

                <button 
                  className="flex items-center justify-between w-full py-4 px-2 hover:bg-base-200 rounded-lg transition-colors"
                  onClick={handleDownloadReport}
                >
                  <div className="flex items-center">
                    <FileText className="w-5 h-5 mr-3 text-primary" />
                    <span>Download User Report</span>
                  </div>
                  {isDownloading ? (
                    <span className="loading loading-spinner loading-sm text-primary"></span>
                  ) : (
                    <div className="text-base-content/50">
                      <ArrowLeft className="w-5 h-5 rotate-180" />
                    </div>
                  )}
                </button>
              </div>
            </div>

            {/* Extra Stuff */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold mb-3">Extra Stuff</h3>
              <div className="divide-y divide-base-300">
                <button 
                  className="flex items-center justify-between w-full py-4 px-2 hover:bg-base-200 rounded-lg transition-colors"
                  onClick={() => navigate('/about')}
                >
                  <div className="flex items-center">
                    <HelpCircle className="w-5 h-5 mr-3 text-primary" />
                    <span>About EnglishTek</span>
                  </div>
                  <div className="text-base-content/50">
                    <ArrowLeft className="w-5 h-5 rotate-180" />
                  </div>
                </button>
              </div>
            </div>

            {/* Logout button removed - now available in sidebar */}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Settings;
