import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../../services/authService';
import logo from '../../assets/Logo.png';

const UserLogin = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login(username, password);
      
      // Check if user has USER role (not ADMIN)
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (user.role === 'ADMIN') {
        // If admin, redirect to admin dashboard
        navigate('/admin/dashboard');
      } else {
        // If regular user, redirect to user dashboard
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to login. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center p-4">
      <div className="bg-base-100 rounded-xl shadow-xl w-full max-w-md overflow-hidden">
        <div className="p-8 text-center">
          <div className="flex justify-center mb-6">
            <img src={logo} alt="EnglishTek Logo" className="h-24 w-auto" />
          </div>
          <h1 className="text-2xl font-bold mb-2">Welcome to EnglishTek</h1>
          <p className="text-base-content/70 mb-6">Sign in to continue your learning journey</p>
          
          {error && (
            <div className="alert alert-error mb-6">
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="w-4 h-4 opacity-70"><path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM12.735 14c.618 0 1.093-.561.872-1.139a6.002 6.002 0 0 0-11.215 0c-.22.578.254 1.139.872 1.139h9.47Z" /></svg>
                <input 
                  type="text" 
                  className="grow" 
                  placeholder="Username" 
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </label>
            </div>
            
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor" className="w-4 h-4 opacity-70"><path fillRule="evenodd" d="M14 6a4 4 0 0 1-4.899 3.899l-1.955 1.955a.5.5 0 0 1-.353.146H5v1.5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1-.5-.5v-2.293a.5.5 0 0 1 .146-.353l3.955-3.955A4 4 0 1 1 14 6Zm-4-2a.75.75 0 0 0 0 1.5.5.5 0 0 1 .5.5.75.75 0 0 0 1.5 0 2 2 0 0 0-2-2Z" clipRule="evenodd" /></svg>
                <input 
                  type="password" 
                  className="grow" 
                  placeholder="Password" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </label>
            </div>

            <button 
              type="submit" 
              className="btn btn-primary w-full"
              disabled={loading}
            >
              {loading ? (
                <span className="loading loading-spinner loading-sm"></span>
              ) : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-sm text-base-content/70">
            <p>Don't have an account? <Link to="/register" className="text-primary hover:underline">Register here</Link></p>
          </div>
        </div>

        <div className="bg-base-200 p-4 text-center text-sm text-base-content/70">
          <p>&#xa9; 2025 EnglishTek. All rights reserved.</p>
        </div>
      </div>
    </div>
  );
};

export default UserLogin;
