import { useState, useEffect, useRef } from 'react';
import { User, userService } from '../services/userService';
import { getFullImageUrl } from '../utils/urlUtils';
import { Camera } from 'lucide-react';
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

interface EditUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUserUpdated: () => void;
  user: User | null;
}

const EditUserModal = ({ isOpen, onClose, onUserUpdated, user }: EditUserModalProps) => {
  const [formData, setFormData] = useState({
    username: '',
    name: '',
    email: '',
    bio: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [showPresets, setShowPresets] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username,
        name: user.name || '',
        email: user.email,
        bio: user.bio || '',
      });
      setAvatarPreview(user.avatarUrl ? getFullImageUrl(user.avatarUrl) : null);
    }
  }, [user]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatarFile(file);
      setAvatarPreview(URL.createObjectURL(file));
    }
  };

  const handlePresetSelect = async (imagePath: string) => {
    try {
      // Convert the imported image URL to a File object
      const response = await fetch(imagePath);
      if (!response.ok) throw new Error('Failed to load preset image');
      
      const blob = await response.blob();
      const file = new File([blob], 'preset-avatar.png', { type: 'image/png' });
      
      setAvatarFile(file);
      setAvatarPreview(imagePath);
      setShowPresets(false);
    } catch (err) {
      console.error('Failed to load preset:', err);
      setError('Failed to load preset avatar');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    try {
      setLoading(true);
      setError(null);
      await userService.updateUser(
        user.id, 
        { ...formData, passwordUpdateRequested: false },
        avatarFile || undefined
      );
      onUserUpdated();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update user');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen || !user) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-2xl">
        <h3 className="font-bold text-lg mb-6">Edit User</h3>
        
        {error && (
          <div className="alert alert-error mb-4">
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Avatar Upload Section */}
          <div className="form-control mb-4">
            <label className="label">
              <span className="label-text">Profile Picture</span>
            </label>
            <div className="flex flex-col items-center gap-4">
              {/* Avatar Preview */}
              <div className="avatar">
                <div className="w-24 h-24 rounded-full ring ring-primary ring-offset-base-100 ring-offset-2">
                  {avatarPreview ? (
                    <img src={avatarPreview} alt="Avatar preview" className="object-cover" />
                  ) : (
                    <div className="bg-base-300 w-full h-full flex items-center justify-center">
                      <Camera className="w-8 h-8 text-base-content/50" />
                    </div>
                  )}
                </div>
              </div>

              {/* Upload and Preset Buttons */}
              <div className="flex gap-2">
                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  onClick={() => fileInputRef.current?.click()}
                >
                  Upload Image
                </button>
                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  onClick={() => setShowPresets(!showPresets)}
                >
                  Choose Preset
                </button>
              </div>

              {/* Hidden File Input */}
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                accept="image/*"
                onChange={handleAvatarChange}
              />

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

          {/* Form Fields */}
          <div className="grid gap-4">
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

            <div className="form-control">
              <label className="label">
                <span className="label-text">Bio</span>
                <span className="label-text-alt">{formData.bio.length}/20</span>
              </label>
              <textarea
                name="bio"
                value={formData.bio}
                onChange={(e) => {
                  const value = e.target.value;
                  if (value.length <= 20) {
                    handleInputChange(e);
                  }
                }}
                maxLength={20}
                className="textarea textarea-bordered h-24"
                placeholder="Enter bio (optional)"
              />
            </div>
          </div>

          {/* Actions */}
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
                <>
                  <span className="loading loading-spinner loading-sm"></span>
                  Saving...
                </>
              ) : (
                'Save Changes'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditUserModal;
