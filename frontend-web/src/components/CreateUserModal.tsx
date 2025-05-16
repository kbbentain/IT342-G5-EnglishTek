import { useState, useRef } from 'react';
import { userService } from '../services/userService';
import { User, Camera } from 'lucide-react';
import character1 from '../assets/preset_profiles/character1.png';
import character2 from '../assets/preset_profiles/character2.png';
import character3 from '../assets/preset_profiles/character3.png';
import character4 from '../assets/preset_profiles/character4.png';

// Preset avatars with imported images
const PRESET_AVATARS = [
  {
    path: character1,
    label: 'Character 1'
  },
  {
    path: character2,
    label: 'Character 2'
  },
  {
    path: character3,
    label: 'Character 3'
  },
  {
    path: character4,
    label: 'Character 4'
  }
];

interface CreateUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUserCreated: () => void;
}

const CreateUserModal = ({ isOpen, onClose, onUserCreated }: CreateUserModalProps) => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    name: '',
    isAdmin: false,
  });
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [showPresets, setShowPresets] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatarFile(file);
      setAvatarPreview(URL.createObjectURL(file));
      setShowPresets(false);
    }
  };

  const handlePresetSelect = async (presetUrl: string) => {
    try {
      const response = await fetch(presetUrl);
      if (!response.ok) throw new Error('Failed to load preset image');
      
      const blob = await response.blob();
      const file = new File([blob], 'preset-avatar.png', { type: 'image/png' });
      
      setAvatarFile(file);
      setAvatarPreview(presetUrl);
      setShowPresets(false);
    } catch (err) {
      console.error('Failed to load preset:', err);
      setError('Failed to load preset avatar');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const formDataToSend = new FormData();
      formDataToSend.append('username', formData.username);
      formDataToSend.append('password', formData.password);
      formDataToSend.append('email', formData.email);
      formDataToSend.append('name', formData.name);
      if (avatarFile) {
        formDataToSend.append('avatar', avatarFile);
      }

      // Choose endpoint based on isAdmin flag
      const endpoint = formData.isAdmin ? '/api/v1/auth/register-admin' : '/api/v1/auth/register';
      await userService.createUser(formDataToSend, endpoint);

      onUserCreated();
      onClose();
      // Reset form
      setFormData({
        username: '',
        password: '',
        email: '',
        name: '',
        isAdmin: false,
      });
      setAvatarFile(null);
      setAvatarPreview(null);
      setShowPresets(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create user');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-md">
        <h3 className="font-bold text-lg mb-4">Create New User</h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Avatar Upload */}
          <div className="form-control">
            <label className="label">
              <span className="label-text">Avatar</span>
            </label>
            <div className="flex flex-col gap-4">
              <div className="flex justify-center">
                <div className="relative w-24 h-24">
                  <div className="absolute inset-0 rounded-full bg-base-300 flex items-center justify-center group">
                    {avatarPreview ? (
                      <img src={avatarPreview} alt="Avatar preview" className="rounded-full w-full h-full object-cover" />
                    ) : (
                      <User className="w-8 h-8 opacity-70" strokeWidth={1.5} />
                    )}
                    <label 
                      className="absolute inset-0 bg-black/40 rounded-full hidden group-hover:flex items-center justify-center cursor-pointer"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      <Camera className="w-6 h-6 text-white" />
                    </label>
                  </div>
                </div>
              </div>
              
              <div className="flex gap-2 justify-center">
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handleAvatarChange}
                  className="hidden"
                />
                <button
                  type="button"
                  className="btn btn-sm"
                  onClick={() => fileInputRef.current?.click()}
                >
                  Upload Image
                </button>
                <button
                  type="button"
                  className="btn btn-sm"
                  onClick={() => setShowPresets(prev => !prev)}
                >
                  Choose Preset
                </button>
              </div>

              {/* Preset Avatars Grid */}
              {showPresets && (
                <div className="grid grid-cols-2 gap-3 mt-2 p-3 border rounded-lg bg-base-200">
                  {PRESET_AVATARS.map((preset, index) => (
                    <button
                      key={index}
                      type="button"
                      className="flex flex-col items-center gap-1 p-2 hover:bg-base-300 rounded-lg transition-all"
                      onClick={() => handlePresetSelect(preset.path)}
                    >
                      <div className="avatar">
                        <div className="w-20 h-20 rounded-full bg-base-100">
                          <img src={preset.path} alt={preset.label} className="object-cover" />
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Username */}
          <div className="form-control">
            <label className="label">
              <span className="label-text">Username</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                className="grow"
                placeholder="Enter username"
                required
              />
            </label>
          </div>

          {/* Name */}
          <div className="form-control">
            <label className="label">
              <span className="label-text">Name</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                className="grow"
                placeholder="Enter name"
                required
              />
            </label>
          </div>

          {/* Email */}
          <div className="form-control">
            <label className="label">
              <span className="label-text">Email</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                className="grow"
                placeholder="Enter email"
                required
              />
            </label>
          </div>

          {/* Password */}
          <div className="form-control">
            <label className="label">
              <span className="label-text">Password</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                className="grow"
                placeholder="Enter password"
                required
              />
            </label>
          </div>

          {/* Admin Toggle */}
          <div className="form-control">
            <label className="label cursor-pointer justify-start gap-4">
              <span className="label-text">Create as Admin Account</span>
              <input
                type="checkbox"
                name="isAdmin"
                checked={formData.isAdmin}
                onChange={handleInputChange}
                className="toggle toggle-primary"
              />
            </label>
          </div>

          {error && (
            <div className="alert alert-error">
              <span>{error}</span>
            </div>
          )}

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
              {loading ? 'Creating...' : 'Create User'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateUserModal;
