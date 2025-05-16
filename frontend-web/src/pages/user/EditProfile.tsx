import { useState, useEffect, useRef, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, User, Mail, FileText, Upload, Check, Camera } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { getFullImageUrl } from '../../utils/urlUtils';

// Import preset avatars
import character1 from '../../assets/preset_profiles/character1.png';
import character2 from '../../assets/preset_profiles/character2.png';
import character3 from '../../assets/preset_profiles/character3.png';
import character4 from '../../assets/preset_profiles/character4.png';

// Define preset avatars
const presetAvatars = [
  { id: 'character1', src: character1 },
  { id: 'character2', src: character2 },
  { id: 'character3', src: character3 },
  { id: 'character4', src: character4 },
];

// Define the updateProfile function
const updateProfile = async (formData: FormData) => {
  try {
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
      throw new Error(errorData.message || 'Failed to update profile');
    }

    const userData = await response.json();
    
    // Update user data in localStorage
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    localStorage.setItem('user', JSON.stringify({ ...currentUser, ...userData }));
    
    return userData;
  } catch (error) {
    console.error('Profile update error:', error);
    throw error;
  }
};

const EditProfile = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [bio, setBio] = useState('');
  const [avatar, setAvatar] = useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [selectedPreset, setSelectedPreset] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);
  
  // Load user data
  useEffect(() => {
    const userData = JSON.parse(localStorage.getItem('user') || '{}');
    setName(userData.name || '');
    setEmail(userData.email || '');
    setBio(userData.bio || '');
    
    if (userData.avatarUrl) {
      setAvatarPreview(getFullImageUrl(userData.avatarUrl));
    }
  }, []);
  
  // Track changes
  useEffect(() => {
    const userData = JSON.parse(localStorage.getItem('user') || '{}');
    const hasNameChanged = name !== (userData.name || '');
    const hasEmailChanged = email !== (userData.email || '');
    const hasBioChanged = bio !== (userData.bio || '');
    const hasAvatarChanged = avatar !== null || selectedPreset !== null;
    
    setHasChanges(hasNameChanged || hasEmailChanged || hasBioChanged || hasAvatarChanged);
  }, [name, email, bio, avatar, selectedPreset]);
  
  const handleAvatarChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatar(file);
      setSelectedPreset(null); // Clear any selected preset
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatarPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };
  
  const handlePresetSelect = (presetId: string, presetSrc: string) => {
    setSelectedPreset(presetId);
    setAvatarPreview(presetSrc);
    setAvatar(null); // Clear any uploaded file
  };
  
  const handleSubmit = async () => {
    if (loading) return;
    
    try {
      setLoading(true);
      
      const formData = new FormData();
      formData.append('name', name);
      formData.append('email', email);
      formData.append('bio', bio);
      
      if (avatar) {
        formData.append('avatar', avatar);
      } else if (selectedPreset) {
        // Convert the preset image to a file
        const response = await fetch(presetAvatars.find(p => p.id === selectedPreset)?.src || '');
        const blob = await response.blob();
        const file = new File([blob], `${selectedPreset}.png`, { type: 'image/png' });
        formData.append('avatar', file);
      }
      
      await updateProfile(formData);
      toast.success('Profile updated successfully!');
      navigate(-1);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };
  
  const handleBack = () => {
    if (hasChanges) {
      if (window.confirm('You have unsaved changes. Are you sure you want to leave?')) {
        navigate(-1);
      }
    } else {
      navigate(-1);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-base-200 to-base-100 py-12">
      <div className="container mx-auto max-w-4xl px-4">
        {/* Header */}
        <div className="flex items-center mb-8">
          <button 
            className="btn btn-circle btn-ghost mr-4" 
            onClick={handleBack}
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="text-3xl font-bold">Edit Profile</h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
          {/* Left Column - Avatar */}
          <div className="lg:col-span-2">
            <div className="card bg-base-100 shadow-xl">
              <div className="card-body flex flex-col items-center justify-center">
                <h2 className="card-title text-xl mb-6">Profile Picture</h2>
                
                <div className="relative group mb-6">
                  <div className="avatar">
                    <div className="w-48 h-48 rounded-full ring ring-primary ring-offset-base-100 ring-offset-2 overflow-hidden bg-base-300 flex items-center justify-center">
                      {avatarPreview ? (
                        <img src={avatarPreview} alt="Profile" className="w-full h-full object-cover" />
                      ) : (
                        <User className="w-24 h-24 text-base-content/30" />
                      )}
                    </div>
                  </div>
                  
                  <div 
                    className="absolute inset-0 bg-black/40 rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-center justify-center cursor-pointer"
                    onClick={() => fileInputRef.current?.click()}
                  >
                    <div className="flex flex-col items-center text-white">
                      <Camera className="w-8 h-8 mb-2" />
                      <span className="text-sm font-medium">Change Photo</span>
                    </div>
                  </div>
                </div>
                
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleAvatarChange}
                  accept="image/*"
                  className="hidden"
                />
                
                <button 
                  className="btn btn-outline btn-primary btn-sm mb-6"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <Upload className="w-4 h-4 mr-2" />
                  Upload New Picture
                </button>
                
                <div className="divider my-2">OR</div>
                
                <h3 className="font-medium text-base mb-3">Choose a preset avatar</h3>
                
                <div className="grid grid-cols-2 gap-3 w-full">
                  {presetAvatars.map((preset) => (
                    <div 
                      key={preset.id}
                      className={`avatar cursor-pointer transition-all duration-200 ${selectedPreset === preset.id ? 'ring ring-primary ring-offset-2' : 'hover:opacity-80'}`}
                      onClick={() => handlePresetSelect(preset.id, preset.src)}
                    >
                      <div className="w-full aspect-square rounded-xl overflow-hidden">
                        <img src={preset.src} alt={`Avatar ${preset.id}`} className="w-full h-full object-cover" />
                      </div>
                    </div>
                  ))}
                </div>
                
                <div className="divider my-6"></div>
                
                <div className="text-center text-base-content/70">
                  <p className="mb-2">Recommended image size:</p>
                  <p className="font-medium">500 x 500 pixels</p>
                  <p className="mt-4 text-sm">Supported formats: JPG, PNG, GIF</p>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column - Form */}
          <div className="lg:col-span-3">
            <div className="card bg-base-100 shadow-xl">
              <div className="card-body">
                <h2 className="card-title text-xl mb-6">Personal Information</h2>

                <div className="form-control w-full mb-4">
                  <label className="label">
                    <span className="label-text font-medium">Full Name</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                      <User className="w-5 h-5 text-gray-500" />
                    </div>
                    <input
                      type="text"
                      placeholder="Enter your name"
                      className="input input-bordered w-full pl-10"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-control w-full mb-4">
                  <label className="label">
                    <span className="label-text font-medium">Email Address</span>
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                      <Mail className="w-5 h-5 text-gray-500" />
                    </div>
                    <input
                      type="email"
                      placeholder="Enter your email"
                      className="input input-bordered w-full pl-10"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-control w-full mb-6">
                  <label className="label">
                    <span className="label-text font-medium">Bio</span>
                    <span className="label-text-alt">{bio.length}/20</span>
                  </label>
                  <div className="relative">
                    <div className="absolute top-3 left-0 flex items-start pl-3 pointer-events-none">
                      <FileText className="w-5 h-5 text-gray-500" />
                    </div>
                    <textarea
                      placeholder="Tell us about yourself"
                      className="textarea textarea-bordered w-full pl-10 min-h-[80px]"
                      value={bio}
                      onChange={(e) => setBio(e.target.value.slice(0, 20))}
                      maxLength={20}
                    />
                  </div>
                </div>

                <div className="flex justify-end gap-4">
                  <button
                    className="btn btn-outline"
                    onClick={handleBack}
                  >
                    Cancel
                  </button>
                  <button
                    className={`btn btn-primary ${!hasChanges ? 'btn-disabled opacity-70' : ''}`}
                    onClick={handleSubmit}
                    disabled={!hasChanges || loading}
                  >
                    {loading ? (
                      <span className="loading loading-spinner loading-sm"></span>
                    ) : (
                      <>
                        Save Changes
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
    </div>
  );
};

export default EditProfile;
