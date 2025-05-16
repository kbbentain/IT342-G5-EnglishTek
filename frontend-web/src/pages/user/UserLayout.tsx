import { useState, useEffect } from 'react';
import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { User, Award, Menu, X, LogOut } from 'lucide-react';
import logo from '../../assets/Logo.png';
import { userService } from '../../services/userService';
import { getFullImageUrl } from '../../utils/urlUtils';

const UserLayout = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [userData, setUserData] = useState<any>(null);
  // Loading state for profile data fetching
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    // Clear user data from localStorage
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    // Navigate to login page
    navigate('/login');
  };

  useEffect(() => {
    // Get basic user data from localStorage first for quick display
    const user = localStorage.getItem('user');
    if (user) {
      setUserData(JSON.parse(user));
    }
    
    // Then fetch complete profile data from API
    const fetchUserProfile = async () => {
      try {
        setIsLoading(true);
        const profileData = await userService.getCurrentUser();
        setUserData((prevData: any) => ({
          ...prevData,
          ...profileData
        }));
        // Update localStorage with the latest data
        localStorage.setItem('user', JSON.stringify({
          ...JSON.parse(localStorage.getItem('user') || '{}'),
          ...profileData
        }));
      } catch (error) {
        console.error('Error fetching user profile:', error);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchUserProfile();
  }, []);

  // Close mobile menu when route changes
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  const navItems = [
    { path: '/home', label: 'Home', icon: <img src={logo} alt="EnglishTek Logo" className="w-5 h-5 object-contain" /> },
    { path: '/achievements', label: 'Badges', icon: <Award className="w-5 h-5" /> },
    { path: '/profile', label: 'Profile', icon: <User className="w-5 h-5" /> },
  ];

  return (
    <div className="drawer lg:drawer-open">
      <input 
        id="user-drawer" 
        type="checkbox" 
        className="drawer-toggle" 
        checked={isMobileMenuOpen}
        onChange={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
      />
      
      <div className="drawer-content flex flex-col">
        {/* Navbar */}
        <div className="navbar bg-base-100 lg:hidden sticky top-0 z-10 shadow-sm">
          <div className="flex-none">
            <label htmlFor="user-drawer" className="btn btn-square btn-ghost">
              <Menu className="w-6 h-6" />
            </label>
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <img src={logo} alt="EnglishTek Logo" className="w-6 h-6 object-contain" />
              <span className="text-xl font-bold">EnglishTek</span>
            </div>
          </div>
          <div className="flex-none">
            <div className="avatar placeholder">
              <div className="bg-primary text-primary-content rounded-full w-10" onClick={() => navigate('/profile')}>
                <span>{userData?.name?.[0]?.toUpperCase() || 'U'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Page content */}
        <main className="flex-1">
          <Outlet />
        </main>
      </div>
      
      <div className="drawer-side z-20">
        <label htmlFor="user-drawer" aria-label="close sidebar" className="drawer-overlay"></label>
        <div className="menu p-4 w-80 min-h-full bg-base-200 text-base-content">
          {/* Sidebar content */}
          <div className="flex justify-between items-center mb-6 lg:mb-10">
            <div className="flex items-center gap-2">
              <img src={logo} alt="EnglishTek Logo" className="w-8 h-8 object-contain" />
              <span className="text-2xl font-bold">EnglishTek</span>
            </div>
            <button 
              className="btn btn-ghost btn-circle lg:hidden"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              <X className="w-6 h-6" />
            </button>
          </div>
          
          {/* User info */}
          <div className="flex items-center gap-4 mb-8 p-4 bg-base-100 rounded-box">
            <div className="avatar">
              {userData?.avatarUrl ? (
                <div className="rounded-full w-12 h-12 overflow-hidden">
                  <img 
                    src={getFullImageUrl(userData.avatarUrl) || ''} 
                    alt={userData?.name || 'User'} 
                    className="w-full h-full object-cover"
                  />
                </div>
              ) : (
                <div className="bg-primary text-primary-content rounded-full w-12 h-12 flex items-center justify-center">
                  <span className="text-xl">{userData?.name?.[0]?.toUpperCase() || 'U'}</span>
                </div>
              )}
            </div>
            <div>
              <h3 className="font-bold">{userData?.name || 'User'}</h3>
              <p className="text-sm opacity-70">{userData?.email || 'user@example.com'}</p>
            </div>
          </div>
          
          {/* Navigation */}
          <ul className="space-y-2">
            {navItems.map((item) => (
              <li key={item.path}>
                <NavLink 
                  to={item.path}
                  className={({ isActive }) => 
                    `flex items-center p-3 ${isActive ? 'bg-primary text-primary-content' : 'hover:bg-base-300'} rounded-lg transition-colors`
                  }
                >
                  {item.icon}
                  <span className="ml-3">{item.label}</span>
                </NavLink>
              </li>
            ))}
          </ul>
          
          {/* Logout button */}
          <div className="mt-auto">
            <button 
              onClick={handleLogout}
              className="flex items-center w-full p-3 hover:bg-base-300 rounded-lg transition-colors text-left"
            >
              <LogOut className="w-5 h-5" />
              <span className="ml-3">Logout</span>
            </button>
            
            <div className="pt-6 text-center text-sm opacity-70">
              <p>&#169; 2025 EnglishTek</p>
              <p>All rights reserved</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserLayout;
