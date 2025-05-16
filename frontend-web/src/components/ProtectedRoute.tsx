import { Navigate } from 'react-router-dom';
import { isAuthenticated, isAdmin } from '../services/authService';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: 'ADMIN' | 'USER';
}

const ProtectedRoute = ({ children, requiredRole }: ProtectedRouteProps) => {
  // Use the isAuthenticated function from authService
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  // Check role if required
  if (requiredRole) {
    const userIsAdmin = isAdmin();
    
    if (requiredRole === 'ADMIN' && !userIsAdmin) {
      // If admin route but user is not admin, redirect to user dashboard
      return <Navigate to="/dashboard" replace />;
    }
    
    if (requiredRole === 'USER' && userIsAdmin) {
      // If user route but user is admin, redirect to admin dashboard
      return <Navigate to="/admin/dashboard" replace />;
    }
  }

  return <>{children}</>;
};

export default ProtectedRoute;
