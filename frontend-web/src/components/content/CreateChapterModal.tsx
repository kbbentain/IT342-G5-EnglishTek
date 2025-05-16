import { useState, useEffect } from 'react';
import { chapterService } from '../../services/contentService';
import { Image, X, Upload } from 'lucide-react';
import defaultChapterIcon from '../../assets/default_chapter.png';

interface CreateChapterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onChapterCreated: () => void;
}

const CreateChapterModal = ({
  isOpen,
  onClose,
  onChapterCreated,
}: CreateChapterModalProps) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [icon, setIcon] = useState<File | null>(null);
  const [iconPreview, setIconPreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [defaultIconFile, setDefaultIconFile] = useState<File | null>(null);

  // Load and convert default icon to File object
  useEffect(() => {
    const loadDefaultIcon = async () => {
      try {
        const response = await fetch(defaultChapterIcon);
        const blob = await response.blob();
        const file = new File([blob], 'default_chapter.png', { type: 'image/png' });
        setDefaultIconFile(file);
      } catch (err) {
        console.error('Failed to load default chapter icon:', err);
      }
    };
    loadDefaultIcon();
  }, []);

  const handleIconChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        setError('Please upload an image file');
        return;
      }

      // Validate file size (e.g., 2MB limit)
      if (file.size > 2 * 1024 * 1024) {
        setError('Image size should be less than 2MB');
        return;
      }

      setIcon(file);
      setError(null);

      // Create preview URL
      const reader = new FileReader();
      reader.onloadend = () => {
        setIconPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!title.trim() || !description.trim()) {
      setError('Title and description are required');
      return;
    }

    try {
      setLoading(true);
      await chapterService.createChapter({
        title: title.trim(),
        description: description.trim(),
        image: icon || defaultIconFile || undefined,
      });
      onChapterCreated();
      handleClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create chapter');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setTitle('');
    setDescription('');
    setIcon(null);
    setIconPreview(null);
    setError(null);
    onClose();
  };

  // Cleanup function for object URLs
  useEffect(() => {
    return () => {
      if (iconPreview && iconPreview.startsWith('blob:')) {
        URL.revokeObjectURL(iconPreview);
      }
    };
  }, [iconPreview]);

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-xl">
        <div className="flex justify-between items-start mb-4">
          <h3 className="font-bold text-lg">Create New Chapter</h3>
          <button className="btn btn-ghost btn-sm" onClick={handleClose}>
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="alert alert-error">
              <span>{error}</span>
            </div>
          )}

          <div className="form-control">
            <label className="label">
              <span className="label-text">Title</span>
            </label>
            <label className="input input-bordered flex items-center gap-2">
            <input
              type="text"
              className="grow"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Enter chapter title"
              required
            />
            </label>
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Description</span>
            </label>
            <textarea
              className="textarea textarea-bordered h-24"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Enter chapter description"
              required
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Chapter Icon</span>
            </label>
            <div className="flex flex-col items-center p-4 border-2 border-dashed rounded-lg bg-base-200 hover:bg-base-300 transition-colors cursor-pointer">
              <input
                type="file"
                accept="image/*"
                onChange={handleIconChange}
                className="hidden"
                id="icon-upload"
              />
              <label
                htmlFor="icon-upload"
                className="flex flex-col items-center cursor-pointer"
              >
                {iconPreview ? (
                  <div className="relative w-32 h-32 mb-2">
                    <img
                      src={iconPreview}
                      alt="Chapter icon preview"
                      className="w-full h-full object-cover rounded-lg"
                    />
                    <button
                      type="button"
                      className="btn btn-circle btn-error btn-xs absolute -top-2 -right-2"
                      onClick={(e) => {
                        e.preventDefault();
                        setIcon(null);
                        setIconPreview(null);
                      }}
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </div>
                ) : (
                  <>
                    <Upload className="w-12 h-12 text-base-content/50 mb-2" />
                    <div className="text-sm text-base-content/70">
                      Click to upload chapter icon
                    </div>
                    <div className="text-xs text-base-content/50 mt-1">
                      PNG, JPG up to 2MB
                    </div>
                  </>
                )}
              </label>
            </div>
          </div>

          <div className="modal-action">
            <button
              type="button"
              className="btn"
              onClick={handleClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || !title.trim() || !description.trim()}
            >
              {loading ? (
                <span className="loading loading-spinner"></span>
              ) : (
                'Create Chapter'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateChapterModal;
