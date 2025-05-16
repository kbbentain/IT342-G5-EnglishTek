import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Eye, EyeOff, Check, Shield } from 'lucide-react';
import { toast } from 'react-hot-toast';

// Define the changePassword function
const changePassword = async (existingPassword: string, newPassword: string) => {
  try {
    const formData = new FormData();
    formData.append('existingPassword', existingPassword);
    formData.append('newPassword', newPassword);
    formData.append('passwordUpdateRequested', 'true');
    // Required fields for the endpoint but not needed for password change
    formData.append('name', '');
    formData.append('email', '');
    formData.append('bio', '');

    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Not authenticated');
    }

    const response = await fetch(`${import.meta.env.VITE_API_URL}/api/v1/users/me`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Failed to change password');
    }

    const userData = await response.json();
    
    // Update user data in localStorage if needed
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    localStorage.setItem('user', JSON.stringify({ ...currentUser, ...userData }));
    
    return userData;
  } catch (error) {
    console.error('Password change error:', error);
    throw error;
  }
};

const ChangePassword = () => {
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (loading) return;
    
    if (!isFormValid()) {
      toast.error('Please check your password requirements');
      return;
    }
    
    try {
      setLoading(true);
      await changePassword(currentPassword, newPassword);
      toast.success('Password updated successfully!');
      navigate(-1);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Failed to update password');
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = () => {
    return (
      currentPassword.length >= 6 &&
      newPassword.length >= 6 &&
      confirmPassword === newPassword
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-base-200 to-base-100 py-12">
      <div className="container mx-auto max-w-4xl px-4">
        {/* Header */}
        <div className="flex items-center mb-8">
          <button 
            className="btn btn-circle btn-ghost mr-4" 
            onClick={() => navigate(-1)}
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="text-3xl font-bold">Change Password</h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
          {/* Left Column - Info Card */}
          <div className="lg:col-span-2">
            <div className="card bg-base-100 shadow-xl h-full">
              <div className="card-body flex flex-col items-center justify-center text-center">
                <div className="w-24 h-24 rounded-full bg-primary/10 flex items-center justify-center mb-6">
                  <Shield className="w-12 h-12 text-primary" />
                </div>
                <h2 className="card-title text-2xl mb-4">Secure Your Account</h2>
                <p className="text-base-content/70 mb-6">
                  A strong password helps protect your account from unauthorized access.
                </p>
                
                <div className="divider"></div>
                
                <h3 className="font-semibold mb-3">Password Tips:</h3>
                <ul className="text-left text-base-content/70 space-y-2">
                  <li className="flex items-start gap-2">
                    <Check className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                    <span>Use at least 8 characters</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <Check className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                    <span>Include numbers and special characters</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <Check className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                    <span>Avoid using personal information</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <Check className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                    <span>Don't reuse passwords from other sites</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          {/* Right Column - Form */}
          <div className="lg:col-span-3">
            <div className="card bg-base-100 shadow-xl">
              <div className="card-body">
                <h2 className="card-title text-xl mb-6">Update Your Password</h2>

                <div className="form-control w-full mb-4">
                  <label className="label">
                    <span className="label-text font-medium">Current Password</span>
                  </label>
                  <div className="relative">
                    <input
                      type={showCurrentPassword ? "text" : "password"}
                      placeholder="Enter current password"
                      className="input input-bordered w-full pr-10"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                    />
                    <button 
                      className="absolute inset-y-0 right-0 px-3 flex items-center"
                      onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                      type="button"
                    >
                      {showCurrentPassword ? <EyeOff className="w-5 h-5 text-gray-500" /> : <Eye className="w-5 h-5 text-gray-500" />}
                    </button>
                  </div>
                </div>

                <div className="form-control w-full mb-4">
                  <label className="label">
                    <span className="label-text font-medium">New Password</span>
                  </label>
                  <div className="relative">
                    <input
                      type={showNewPassword ? "text" : "password"}
                      placeholder="Enter new password"
                      className="input input-bordered w-full pr-10"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                    />
                    <button 
                      className="absolute inset-y-0 right-0 px-3 flex items-center"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      type="button"
                    >
                      {showNewPassword ? <EyeOff className="w-5 h-5 text-gray-500" /> : <Eye className="w-5 h-5 text-gray-500" />}
                    </button>
                  </div>
                </div>

                <div className="form-control w-full mb-6">
                  <label className="label">
                    <span className="label-text font-medium">Confirm New Password</span>
                  </label>
                  <div className="relative">
                    <input
                      type={showConfirmPassword ? "text" : "password"}
                      placeholder="Confirm new password"
                      className="input input-bordered w-full pr-10"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                    <button 
                      className="absolute inset-y-0 right-0 px-3 flex items-center"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      type="button"
                    >
                      {showConfirmPassword ? <EyeOff className="w-5 h-5 text-gray-500" /> : <Eye className="w-5 h-5 text-gray-500" />}
                    </button>
                  </div>
                </div>

                {/* Password Requirements */}
                <div className="bg-base-200 p-4 rounded-lg mb-6">
                  <h3 className="font-semibold mb-2">Password Requirements:</h3>
                  <div className="flex items-center gap-2 mb-2">
                    <div className={`w-5 h-5 rounded-full flex items-center justify-center ${newPassword.length >= 6 ? 'bg-primary text-white' : 'bg-base-300'}`}>
                      {newPassword.length >= 6 && <Check className="w-3 h-3" />}
                    </div>
                    <span className={newPassword.length >= 6 ? 'text-primary font-medium' : ''}>At least 6 characters long</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className={`w-5 h-5 rounded-full flex items-center justify-center ${confirmPassword === newPassword && confirmPassword !== '' ? 'bg-primary text-white' : 'bg-base-300'}`}>
                      {confirmPassword === newPassword && confirmPassword !== '' && <Check className="w-3 h-3" />}
                    </div>
                    <span className={confirmPassword === newPassword && confirmPassword !== '' ? 'text-primary font-medium' : ''}>Passwords match</span>
                  </div>
                </div>

                <button
                  className={`btn btn-primary w-full ${!isFormValid() ? 'btn-disabled opacity-70' : ''}`}
                  onClick={handleSubmit}
                  disabled={!isFormValid() || loading}
                >
                  {loading ? (
                    <span className="loading loading-spinner loading-sm"></span>
                  ) : (
                    <>
                      Update Password
                      <Check className="w-5 h-5 ml-2" />
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChangePassword;
