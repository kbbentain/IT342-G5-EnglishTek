import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Login from './pages/Login';
import UserLogin from './pages/user/Login';
import Register from './pages/user/Register';
import Dashboard from './pages/Dashboard';
import UserManagement from './pages/UserManagement';
import ContentManagement from './pages/ContentManagement';
import Feedback from './pages/Feedback';
import ProtectedRoute from './components/ProtectedRoute';
import UserRoutes from './pages/user/UserRoutes';

function App() {
  return (
    <div data-theme="pastel" className="min-h-screen w-full">
      <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
      <Router>
        <Routes>
          {/* Admin Login */}
          <Route path="/admin/login" element={<Login />} />
          
          {/* User Authentication */}
          <Route path="/login" element={<UserLogin />} />
          <Route path="/register" element={<Register />} />
          
          {/* Admin Routes */}
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <UserManagement />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/content"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <ContentManagement />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/feedbacks"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <Feedback />
              </ProtectedRoute>
            }
          />
          
          {/* User Routes - All paths under /user/* will be handled by UserRoutes */}
          <Route
            path="/*"
            element={
              <ProtectedRoute requiredRole="USER">
                <UserRoutes />
              </ProtectedRoute>
            }
          />
          
          {/* Default Redirect */}
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/admin" element={<Navigate to="/admin/login" replace />} />
        </Routes>
      </Router>
    </div>
  );
}

export default App;
