import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Home, Users, BookOpen, MessageSquare, LogOut } from 'lucide-react';
import { logout } from '../services/authService';
import { config } from '../config/env';
import logo from '../assets/Logo.png';

const Sidebar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const getFullImageUrl = (url: string) => {
    if (!url) return '';
    return url.startsWith('http') ? url : `${config.apiUrl}${url}`;
  };

  const menuItems = [
    { path: '/admin/dashboard', label: 'Home', icon: <Home className="w-5 h-5" /> },
    { path: '/admin/users', label: 'User Management', icon: <Users className="w-5 h-5" /> },
    { path: '/admin/content', label: 'Content Management', icon: <BookOpen className="w-5 h-5" /> },
    { path: '/admin/feedbacks', label: 'Feedbacks', icon: <MessageSquare className="w-5 h-5" /> },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex flex-col h-full bg-base-100 w-64 shadow-lg">
      {/* Brand Section */}
      <div className="p-4 border-b border-base-300">
        <div className="w-32 h-32 mx-auto mb-2">
          <img 
            src={logo} 
            alt="EnglishTek Logo" 
            className="w-full h-full object-contain"
          />
        </div>
        <h1 className="text-xl font-bold text-center">EnglishTek Admin</h1>
      </div>

      {/* User Info */}
      <div className="p-4 border-b border-base-300">
        <div className="flex items-center space-x-3">
          <div className="avatar">
            <div className="w-10 h-10 rounded-full bg-primary/10">
              {user.avatarUrl ? (
                <img src={getFullImageUrl(user.avatarUrl)} alt={user.name} className="rounded-full" />
              ) : (
                <span className="text-primary text-xl flex items-center justify-center h-full">
                  {user.name?.[0]?.toUpperCase()}
                </span>
              )}
            </div>
          </div>
          <div>
            <p className="font-semibold">{user.name}</p>
            <p className="text-sm text-primary">Administrator</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4">
        <ul className="space-y-2">
          {menuItems.map((item) => (
            <li key={item.path}>
              <Link
                to={item.path}
                className={'btn btn-ghost justify-start w-full ' + 
                  (location.pathname === item.path ? 'btn-active' : '')}
              >
                {item.icon}
                <span className="ml-2">{item.label}</span>
              </Link>
            </li>
          ))}
        </ul>
      </nav>

      {/* Logout Button */}
      <div className="p-4 border-t border-base-300">
        <button
          onClick={handleLogout}
          className="btn btn-error btn-outline w-full justify-start"
        >
          <LogOut className="w-5 h-5" />
          <span className="ml-2">Logout</span>
        </button>
      </div>
    </div>
  );
};

export default Sidebar;
