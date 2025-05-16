import { useState, useEffect } from 'react';
import { Chapter, chapterService } from '../../services/contentService';
import { Image, X, Upload } from 'lucide-react';
import { getFullImageUrl } from '../../utils/urlUtils';

interface EditChapterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onChapterUpdated: () => void;
  chapter: Chapter;
}

const EditChapterModal = ({
  isOpen,
  onClose,
  onChapterUpdated,
  chapter,
}: EditChapterModalProps) => {
  const [title, setTitle] = useState(chapter.title);
  const [description, setDescription] = useState(chapter.description);
  const [icon, setIcon] = useState<File | null>(null);
  const [iconPreview, setIconPreview] = useState<string | null>(getFullImageUrl(chapter.iconUrl));
  const [removeIcon, setRemoveIcon] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Update form when chapter changes
    setTitle(chapter.title);
    setDescription(chapter.description);
    setIconPreview(getFullImageUrl(chapter.iconUrl));
    setRemoveIcon(false);
  }, [chapter]);

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
      setRemoveIcon(false);
      setError(null);

      // Create preview URL
      const reader = new FileReader();
      reader.onloadend = () => {
        setIconPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRemoveIcon = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIcon(null);
    setIconPreview(null);
    setRemoveIcon(true);
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
      await chapterService.updateChapter(chapter.id, {
        title: title.trim(),
        description: description.trim(),
        image: removeIcon ? null : (icon || undefined),
        removeIcon: removeIcon,
      });
      onChapterUpdated();
      handleClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update chapter');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setTitle(chapter.title);
    setDescription(chapter.description);
    setIcon(null);
    setIconPreview(getFullImageUrl(chapter.iconUrl));
    setRemoveIcon(false);
    setError(null);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-xl">
        <div className="flex justify-between items-start mb-4">
          <h3 className="font-bold text-lg">Edit Chapter</h3>
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
              className="input input-bordered"
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
                {iconPreview && !removeIcon ? (
                  <div className="relative w-32 h-32 mb-2">
                    <img
                      src={iconPreview}
                      alt="Chapter icon preview"
                      className="w-full h-full object-cover rounded-lg"
                    />
                    <button
                      type="button"
                      className="btn btn-circle btn-error btn-xs absolute -top-2 -right-2"
                      onClick={handleRemoveIcon}
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
                'Save Changes'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditChapterModal;
