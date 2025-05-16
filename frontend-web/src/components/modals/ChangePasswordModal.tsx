import { useState } from 'react';
import { User, userService } from '../../services/userService';
import { Lock } from 'lucide-react';

interface ChangePasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: User | null;
  onPasswordChanged: () => void;
}

const ChangePasswordModal = ({ isOpen, onClose, user, onPasswordChanged }: ChangePasswordModalProps) => {
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!user) return;
    
    if (formData.newPassword !== formData.confirmPassword) {
      setError("Passwords don't match");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      await userService.changePassword(user.id, formData.newPassword);

      onPasswordChanged();
      onClose();
      setFormData({
        newPassword: '',
        confirmPassword: '',
      });
    } catch (err: any) {
      setError(err.message || 'Failed to change password');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box relative">
        <button
          onClick={onClose}
          className="btn btn-sm btn-circle absolute right-2 top-2"
        >
          ✕
        </button>
        
        <h3 className="font-bold text-lg mb-4 flex items-center gap-2">
          <Lock className="w-5 h-5" />
          Change User Password
        </h3>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="alert alert-error">
              <span>{error}</span>
            </div>
          )}

          <div className="form-control">
            <label className="label">
              <span className="label-text">New Password</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
            <input
              type="password"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleInputChange}
              className="grow"
              required
              placeholder="Enter new password"
            />
            </label>
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Confirm New Password</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              className="grow"
              required
              placeholder="Confirm new password"
            />
            </label>
          </div>

          <div className="modal-action">
            <button
              type="button"
              className="btn"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? (
                <span className="loading loading-spinner"></span>
              ) : (
                'Change Password'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordModal;
