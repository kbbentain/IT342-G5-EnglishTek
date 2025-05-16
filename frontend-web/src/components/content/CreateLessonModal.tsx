import { useState } from 'react';
import { LessonRequest, lessonService } from '../../services/contentService';
import { X } from 'lucide-react';

interface CreateLessonModalProps {
  isOpen: boolean;
  onClose: () => void;
  onLessonCreated: () => void;
  chapterId: number;
}

const CreateLessonModal = ({
  isOpen,
  onClose,
  onLessonCreated,
  chapterId,
}: CreateLessonModalProps) => {
  const [formData, setFormData] = useState<LessonRequest>({
    chapterId,
    title: '',
    content: '',
    order: 0,
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await lessonService.createLesson(formData);
      onLessonCreated();
      setFormData({
        chapterId,
        title: '',
        content: '',
        order: 0,
      });
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create lesson');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box">
        <button
          onClick={onClose}
          className="btn btn-sm btn-circle absolute right-2 top-2"
        >
          <X className="w-4 h-4" />
        </button>

        <h3 className="font-bold text-lg mb-4">Create New Lesson</h3>

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
            <input
              type="text"
              className="input input-bordered"
              value={formData.title}
              onChange={(e) =>
                setFormData({ ...formData, title: e.target.value })
              }
              required
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Content</span>
            </label>
            <textarea
              className="textarea textarea-bordered min-h-[200px]"
              value={formData.content}
              onChange={(e) =>
                setFormData({ ...formData, content: e.target.value })
              }
              required
              placeholder="Enter lesson content here..."
            />
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
                'Create Lesson'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateLessonModal;
