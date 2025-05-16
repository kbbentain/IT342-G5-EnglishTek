import { useState, useRef, useEffect } from 'react';
import { Upload, X } from 'lucide-react';
import defaultBadgeIcon from '../../../assets/default_badge.png';
import { getFullImageUrl } from '../../../utils/urlUtils';

interface BadgeCreationStepProps {
  initialBadge?: {
    name: string;
    description: string;
    iconUrl?: string;
  };
  onNext: (badgeData: {
    name: string;
    description: string;
    icon: File | null;
  }) => void;
  onBack: () => void;
}

const BadgeCreationStep = ({ initialBadge, onNext, onBack }: BadgeCreationStepProps) => {
  const [name, setName] = useState(initialBadge?.name || '');
  const [description, setDescription] = useState(initialBadge?.description || '');
  const [icon, setIcon] = useState<File | null>(null);
  const [iconPreview, setIconPreview] = useState<string | null>(
    initialBadge?.iconUrl ? getFullImageUrl(initialBadge.iconUrl) : null
  );
  const [error, setError] = useState<string | null>(null);
  const [defaultIconFile, setDefaultIconFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Load default icon on mount
  useEffect(() => {
    const loadDefaultIcon = async () => {
      try {
        const response = await fetch(defaultBadgeIcon);
        const blob = await response.blob();
        const file = new File([blob], 'default_badge.png', { type: 'image/png' });
        setDefaultIconFile(file);
      } catch (err) {
        console.error('Failed to load default badge icon:', err);
      }
    };
    loadDefaultIcon();
  }, []);

  const handleIconChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.type.startsWith('image/')) {
        setIcon(file);
        const reader = new FileReader();
        reader.onloadend = () => {
          setIconPreview(reader.result as string);
        };
        reader.readAsDataURL(file);
        setError(null);
      } else {
        setError('Please upload an image file');
      }
    }
  };

  const handleRemoveIcon = () => {
    setIcon(null);
    setIconPreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      setError('Name is required');
      return;
    }

    if (!description.trim()) {
      setError('Description is required');
      return;
    }

    // Use default icon if no icon is uploaded and it's a new badge
    const submissionIcon = icon || (!initialBadge ? defaultIconFile : null);

    onNext({
      name: name.trim(),
      description: description.trim(),
      icon: submissionIcon,
    });
  };

  // Cleanup function for object URLs
  useEffect(() => {
    return () => {
      if (iconPreview && iconPreview.startsWith('blob:')) {
        URL.revokeObjectURL(iconPreview);
      }
    };
  }, [iconPreview]);

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Icon Upload */}
      <div className="form-control">
        <label className="label">
          <span className="label-text">Badge Icon</span>
        </label>
        <div className="flex items-center gap-4">
          <div
            className={`w-24 h-24 rounded-lg border-2 border-dashed flex items-center justify-center relative overflow-hidden ${
              iconPreview ? 'border-primary' : 'border-base-content/20'
            }`}
          >
            {iconPreview ? (
              <>
                <img
                  src={iconPreview}
                  alt="Badge icon preview"
                  className="w-full h-full object-cover"
                />
                <button
                  type="button"
                  onClick={handleRemoveIcon}
                  className="absolute top-1 right-1 btn btn-circle btn-ghost btn-xs bg-base-200/80 hover:bg-base-200"
                >
                  <X className="w-3 h-3" />
                </button>
              </>
            ) : (
              <label className="cursor-pointer p-2 text-center">
                <Upload className="w-8 h-8 mx-auto mb-1 opacity-50" />
                <span className="text-xs opacity-50">Upload Icon</span>
                <input
                  type="file"
                  className="hidden"
                  accept="image/*"
                  onChange={handleIconChange}
                  ref={fileInputRef}
                />
              </label>
            )}
          </div>
          <div className="flex-1">
            <p className="text-sm text-base-content/70">
              {initialBadge 
                ? "Upload a new icon to replace the current one, or keep the existing icon."
                : "Upload a square image for the badge icon."}
              <br />
              Recommended size: 128x128 pixels.
            </p>
          </div>
        </div>
      </div>

      {/* Name */}
      <div className="form-control">
        <label className="label">
          <span className="label-text">Badge Name</span>
        </label>
        <label className="input input-bordered flex items-center gap-2">
        <input
          type="text"
          className="grow"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g., Perfect Score"
        />
        </label>
      </div>

      {/* Description */}
      <div className="form-control">
        <label className="label">
          <span className="label-text">Description</span>
        </label>
        <textarea
          className="textarea textarea-bordered h-24"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="e.g., Awarded for achieving a perfect score on this quiz"
        />
      </div>

      {/* Error Message */}
      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
        </div>
      )}

      {/* Actions */}
      <div className="flex justify-between pt-4">
        <button
          type="button"
          className="btn btn-ghost"
          onClick={onBack}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="btn btn-primary"
        >
          Continue
        </button>
      </div>
    </form>
  );
};

export default BadgeCreationStep;
