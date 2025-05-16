import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Lock } from 'lucide-react';
import { login } from '../services/authService';
import logo from '../assets/Logo.png';

const Login = () => {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Clear any existing tokens when login page loads
  useEffect(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }, []);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    const formData = new FormData(e.currentTarget);
    const username = formData.get('username') as string;
    const password = formData.get('password') as string;
    
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-full flex">
      {/* Left Section with Memphis Pattern */}
      <div className="hidden lg:flex lg:w-1/2 bg-base-200 items-center justify-center relative overflow-hidden">
        <div className="absolute inset-0" style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg width='52' height='26' viewBox='0 0 52 26' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.1'%3E%3Cpath d='M10 10c0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6h2c0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4v2c-3.314 0-6-2.686-6-6 0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6zm25.464-1.95l8.486 8.486-1.414 1.414-8.486-8.486 1.414-1.414z' /%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
          opacity: 0.5
        }}></div>
        
        <div className="relative z-10 text-center p-8 max-w-md">
          <div className="w-48 h-48 mx-auto mb-8">
            <img 
              src={logo} 
              alt="EnglishTek Logo" 
              className="w-full h-full object-contain"
            />
          </div>
          <h1 className="text-4xl font-bold mb-4">EnglishTek</h1>
          <p className="text-base-content/70">Empowering English Language Learning</p>
        </div>
      </div>

      {/* Right Section */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-base-100">
        <div className="w-full max-w-md space-y-8">
          <div className="text-center">
            <h2 className="text-3xl font-bold mb-2">Login</h2>
            <p className="text-base-content/70">Welcome back! Please enter your credentials.</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="form-control">
            <label className="input input-bordered flex items-center gap-2">
              <input
                type="text"
                name="username"
                placeholder="Username"
                className="grow"
                required
              />
              </label>
            </div>

            <div className="form-control">
            <label className="input input-bordered flex items-center gap-2">
              <input
                type="password"
                name="password"
                placeholder="Password"
                className="grow"
                required
              />
              </label>
            </div>

            {error && (
              <div className="alert alert-error">
                <span>{error}</span>
              </div>
            )}

            <button 
              type="submit" 
              className={`btn btn-primary w-full h-12 ${isLoading ? 'loading' : ''}`}
              disabled={isLoading}
            >
              {isLoading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          <p className="text-center text-sm text-base-content/70">
            All rights reserved &copy; {new Date().getFullYear()}
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;