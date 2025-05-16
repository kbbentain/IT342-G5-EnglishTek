import { Routes, Route, Navigate } from 'react-router-dom';
import Home from './Home';
import Profile from './Profile';
import Achievements from './Achievements';
import ChapterDetails from './ChapterDetails';
import LessonView from './LessonView';
import QuizView from './QuizView';
import UserLayout from './UserLayout';
import Settings from './Settings';
import ChangePassword from './ChangePassword';
import EditProfile from './EditProfile';
import About from './About';

const UserRoutes = () => {
  return (
    <Routes>
      <Route element={<UserLayout />}>
        {/* Home page - welcome, overview, and learning content */}
        <Route path="/home" element={<Home />} />
        
        {/* User profile and activity */}
        <Route path="/profile" element={<Profile />} />
        <Route path="/profile/edit" element={<EditProfile />} />
        
        {/* Settings pages */}
        <Route path="/settings" element={<Settings />} />
        <Route path="/settings/change-password" element={<ChangePassword />} />
        
        {/* About page */}
        <Route path="/about" element={<About />} />
        
        {/* Badges/achievements */}
        <Route path="/achievements" element={<Achievements />} />
        
        {/* Chapter details page */}
        <Route path="/chapter/:id" element={<ChapterDetails />} />
        
        {/* Lesson view page */}
        <Route path="/lesson/:id" element={<LessonView />} />
        
        {/* Quiz view page */}
        <Route path="/quiz/:id" element={<QuizView />} />
        
        {/* Redirect dashboard to home */}
        <Route path="/dashboard" element={<Navigate to="/home" replace />} />
        
        {/* Default route redirects to home */}
        <Route path="/" element={<Navigate to="/home" replace />} />
      </Route>
      
      {/* Catch-all redirects to home */}
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  );
};

export default UserRoutes;
