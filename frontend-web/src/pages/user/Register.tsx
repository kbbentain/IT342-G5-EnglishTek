import { useState, useRef, ChangeEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register, RegisterRequest } from '../../services/authService';
import { ArrowLeft, ArrowRight, User, Mail, Lock, FileImage, Check } from 'lucide-react';
import logo from '../../assets/Logo.png';

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

const Register = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState<RegisterRequest>({
    username: '',
    email: '',
    password: '',
    name: '',
  });
  const [confirmPassword, setConfirmPassword] = useState('');
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [selectedPreset, setSelectedPreset] = useState<string | null>(null);

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setFormData(prev => ({ ...prev, avatar: file }));
      
      // Clear any selected preset
      setSelectedPreset(null);
      
      // Create preview URL
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
    
    // Clear any uploaded file
    setFormData(prev => {
      const newData = { ...prev };
      delete newData.avatar;
      return newData;
    });
  };

  const validateStep = (currentStep: number): boolean => {
    setError(null);
    
    if (currentStep === 1) {
      if (!formData.username) {
        setError('Username is required');
        return false;
      }
      if (formData.username.length < 3) {
        setError('Username must be at least 3 characters');
        return false;
      }
    }
    
    if (currentStep === 2) {
      if (!formData.email) {
        setError('Email is required');
        return false;
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        setError('Please enter a valid email address');
        return false;
      }
    }
    
    if (currentStep === 3) {
      if (!formData.password) {
        setError('Password is required');
        return false;
      }
      if (formData.password.length < 6) {
        setError('Password must be at least 6 characters');
        return false;
      }
      if (formData.password !== confirmPassword) {
        setError('Passwords do not match');
        return false;
      }
    }
    
    if (currentStep === 4) {
      if (!formData.name) {
        setError('Full name is required');
        return false;
      }
    }
    
    return true;
  };

  const nextStep = () => {
    if (validateStep(step)) {
      setStep(prev => prev + 1);
    }
  };

  const prevStep = () => {
    setStep(prev => prev - 1);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateStep(step)) return;
    
    setLoading(true);
    try {
      // Create a copy of the form data to work with
      let registrationData = { ...formData };
      
      // If a preset avatar was selected, fetch it and convert to a File object
      if (selectedPreset && !formData.avatar) {
        const presetSrc = presetAvatars.find(p => p.id === selectedPreset)?.src;
        if (presetSrc) {
          const response = await fetch(presetSrc);
          const blob = await response.blob();
          const file = new File([blob], `${selectedPreset}.png`, { type: 'image/png' });
          
          // Update the local copy directly instead of using setState
          registrationData = { ...registrationData, avatar: file };
        }
      }
      
      // Use the updated registration data with the avatar included
      await register(registrationData);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const renderStepIndicator = () => (
    <div className="flex justify-center mb-8">
      <div className="flex items-center">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="flex items-center">
            <div 
              className={`w-8 h-8 rounded-full flex items-center justify-center ${i === step ? 'bg-primary text-primary-content' : i < step ? 'bg-success text-success-content' : 'bg-base-300'}`}
            >
              {i < step ? <Check className="w-4 h-4" /> : i}
            </div>
            {i < 5 && (
              <div 
                className={`w-10 h-1 ${i < step ? 'bg-success' : 'bg-base-300'}`}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  );

  const renderStepContent = () => {
    switch (step) {
      case 1:
        return (
          <div className="space-y-4">
            <h2 className="text-xl font-bold">Create Your Username</h2>
            <p className="text-base-content/70 mb-4">Choose a unique username for your account</p>
            
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <User className="w-4 h-4 opacity-70" />
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleInputChange}
                  className="grow"
                  placeholder="Username"
                  required
                />
              </label>
            </div>
          </div>
        );
      
      case 2:
        return (
          <div className="space-y-4">
            <h2 className="text-xl font-bold">What's Your Email?</h2>
            <p className="text-base-content/70 mb-4">We'll use this for account recovery</p>
            
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <Mail className="w-4 h-4 opacity-70" />
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="grow"
                  placeholder="Email address"
                  required
                />
              </label>
            </div>
          </div>
        );
      
      case 3:
        return (
          <div className="space-y-4">
            <h2 className="text-xl font-bold">Create a Password</h2>
            <p className="text-base-content/70 mb-4">Make it strong and secure</p>
            
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <Lock className="w-4 h-4 opacity-70" />
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="grow"
                  placeholder="Password"
                  required
                />
              </label>
            </div>
            
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <Lock className="w-4 h-4 opacity-70" />
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="grow"
                  placeholder="Confirm password"
                  required
                />
              </label>
            </div>
          </div>
        );
      
      case 4:
        return (
          <div className="space-y-4">
            <h2 className="text-xl font-bold">What's Your Name?</h2>
            <p className="text-base-content/70 mb-4">Let us know what to call you</p>
            
            <div className="form-control">
              <label className="input input-bordered flex items-center gap-2">
                <User className="w-4 h-4 opacity-70" />
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="grow"
                  placeholder="Full name"
                  required
                />
              </label>
            </div>
          </div>
        );
      
      case 5:
        return (
          <div className="space-y-4">
            <h2 className="text-xl font-bold">Profile Picture</h2>
            <p className="text-base-content/70 mb-4">Add a profile picture (optional)</p>
            
            <div className="flex flex-col items-center gap-4">
              <div className="avatar">
                <div className="w-24 rounded-full ring ring-primary ring-offset-base-100 ring-offset-2">
                  {avatarPreview ? (
                    <img src={avatarPreview} alt="Profile preview" className="w-full h-full object-cover" />
                  ) : (
                    <div className="bg-base-300 w-full h-full flex items-center justify-center">
                      <User className="w-12 h-12 opacity-50" />
                    </div>
                  )}
                </div>
              </div>
              
              <label className="btn btn-outline" htmlFor="avatar-upload">
                <FileImage className="w-4 h-4 mr-2" />
                Choose Image
                <input
                  id="avatar-upload"
                  type="file"
                  accept="image/*"
                  onChange={handleFileChange}
                  ref={fileInputRef}
                  className="hidden"
                />
              </label>
              
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
            </div>
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center p-4">
      <div className="bg-base-100 rounded-xl shadow-xl w-full max-w-md overflow-hidden">
        <div className="p-8">
          <div className="flex justify-center mb-6">
            <img src={logo} alt="EnglishTek Logo" className="h-16 w-auto" />
          </div>
          
          {renderStepIndicator()}
          
          {error && (
            <div className="alert alert-error mb-6">
              <span>{error}</span>
            </div>
          )}
          
          <form onSubmit={(e) => e.preventDefault()}>
            {renderStepContent()}
            
            <div className="mt-8 flex justify-between">
              {step > 1 ? (
                <button 
                  type="button" 
                  onClick={prevStep}
                  className="btn btn-outline"
                >
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Back
                </button>
              ) : (
                <Link to="/login" className="btn btn-ghost">
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  Login
                </Link>
              )}
              
              {step < 5 ? (
                <button 
                  type="button" 
                  onClick={nextStep}
                  className="btn btn-primary"
                >
                  Next
                  <ArrowRight className="w-4 h-4 ml-2" />
                </button>
              ) : (
                <button 
                  type="button" 
                  className="btn btn-primary"
                  disabled={loading}
                  onClick={handleSubmit}
                >
                  {loading ? (
                    <span className="loading loading-spinner loading-sm"></span>
                  ) : 'Complete Registration'}
                </button>
              )}
            </div>
          </form>
        </div>
        
        <div className="bg-base-200 p-4 text-center text-sm text-base-content/70">
          <p>&#xa9; 2025 EnglishTek. All rights reserved.</p>
        </div>
      </div>
    </div>
  );
};

export default Register;
