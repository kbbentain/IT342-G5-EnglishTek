import { useState, useEffect } from 'react';
import { LessonRequest, lessonService } from '../../services/contentService';
import { X, Plus, ChevronLeft, ChevronRight, Save, Trash2, BookOpen, FileText } from 'lucide-react';
import MDEditor from '@uiw/react-md-editor';

interface LessonEditorProps {
  chapterId: number;
  initialLesson?: {
    id: number;
    title: string;
    description: string;
    content: string[];
  };
  onSave: () => void;
  onClose: () => void;
}

const LessonEditor = ({
  chapterId,
  initialLesson,
  onSave,
  onClose,
}: LessonEditorProps) => {
  const [formData, setFormData] = useState<LessonRequest>({
    chapterId,
    title: '',
    description: '',
    content: [''],
  });
  const [currentPage, setCurrentPage] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(false);

  useEffect(() => {
    const fetchLessonDetails = async () => {
      if (!initialLesson?.id) return;

      setFetchLoading(true);
      try {
        const lessonDetails = await lessonService.getLesson(initialLesson.id);
        setFormData({
          chapterId,
          title: lessonDetails.title,
          description: lessonDetails.description,
          content: lessonDetails.content,
        });
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to fetch lesson details');
      } finally {
        setFetchLoading(false);
      }
    };

    fetchLessonDetails();
  }, [initialLesson?.id, chapterId]);

  const handleAddPage = () => {
    setFormData(prev => ({
      ...prev,
      content: [...prev.content, ''],
    }));
    setCurrentPage(formData.content.length);
  };

  const handleDeletePage = (pageIndex: number) => {
    if (formData.content.length <= 1) {
      setError("Can't delete the last page");
      return;
    }

    setFormData(prev => ({
      ...prev,
      content: prev.content.filter((_, index) => index !== pageIndex),
    }));

    if (currentPage >= pageIndex) {
      setCurrentPage(Math.max(0, currentPage - 1));
    }
  };

  const handlePageContentChange = (content: string | undefined) => {
    if (content === undefined) return;
    setFormData(prev => ({
      ...prev,
      content: prev.content.map((p, index) =>
        index === currentPage ? content : p
      ),
    }));
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);

    try {
      const requestData: LessonRequest = {
        chapterId,
        title: formData.title,
        description: formData.description,
        content: formData.content.filter(content => content.trim() !== ''),
      };

      if (initialLesson) {
        await lessonService.updateLesson(initialLesson.id, requestData);
      } else {
        await lessonService.createLesson(requestData);
      }
      onSave();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save lesson');
    } finally {
      setLoading(false);
    }
  };

  if (fetchLoading) {
    return (
      <div className="fixed inset-0 bg-base-300/80 backdrop-blur-sm z-50 flex items-center justify-center">
        <div className="text-center bg-base-100 p-8 rounded-xl shadow-xl">
          <span className="loading loading-spinner loading-lg text-primary"></span>
          <p className="mt-4 font-medium">Loading lesson content...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-base-300/80 backdrop-blur-sm z-50">
      <div className="h-full flex flex-col bg-base-100 shadow-2xl max-w-7xl mx-auto my-4 rounded-xl overflow-hidden">
        {/* Header */}
        <div className="bg-base-100 border-b p-6">
          <div className="flex justify-between items-start gap-4">
            <div className="flex-1 space-y-4">
              <div className="flex items-center gap-3">
                <BookOpen className="w-6 h-6 text-primary" />
                <input
                  type="text"
                  placeholder="Enter an engaging lesson title..."
                  className="input input-lg w-full max-w-2xl font-bold text-xl focus:outline-none"
                  value={formData.title}
                  onChange={(e) =>
                    setFormData({ ...formData, title: e.target.value })
                  }
                  required
                />
              </div>
              <div className="flex items-center gap-3">
                <FileText className="w-5 h-5 text-base-content/70" />
                <input
                  type="text"
                  placeholder="Add a brief description of what students will learn..."
                  className="input input-md w-full max-w-2xl focus:outline-none"
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                  required
                />
              </div>
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={handleSubmit}
                disabled={loading || !formData.title || !formData.description}
                className="btn btn-primary"
              >
                {loading ? (
                  <span className="loading loading-spinner"></span>
                ) : (
                  <>
                    <Save className="w-4 h-4" />
                    Save Lesson
                  </>
                )}
              </button>
              <button onClick={onClose} className="btn btn-ghost">
                <X className="w-4 h-4" />
                Close
              </button>
            </div>
          </div>

          {error && (
            <div className="alert alert-error mt-4">
              <span>{error}</span>
            </div>
          )}
        </div>

        {/* Editor Area */}
        <div className="flex-1 overflow-hidden flex flex-col bg-base-200/50">
          {/* Page Navigation */}
          <div className="bg-base-100 border-b px-6 py-3 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="flex items-center bg-base-200 rounded-lg p-1">
                <button
                  onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                  disabled={currentPage === 0}
                  className="btn btn-sm btn-ghost btn-circle"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <span className="px-3 font-medium">
                  Page {currentPage + 1} of {formData.content.length}
                </span>
                <button
                  onClick={() => setCurrentPage(prev => Math.min(formData.content.length - 1, prev + 1))}
                  disabled={currentPage === formData.content.length - 1}
                  className="btn btn-sm btn-ghost btn-circle"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
              <button
                onClick={handleAddPage}
                className="btn btn-sm btn-ghost"
              >
                <Plus className="w-4 h-4" />
                Add Page
              </button>
              {formData.content.length > 1 && (
                <button
                  onClick={() => handleDeletePage(currentPage)}
                  className="btn btn-sm btn-ghost btn-error"
                >
                  <Trash2 className="w-4 h-4" />
                  Delete Page
                </button>
              )}
            </div>
          </div>

          {/* Markdown Editor */}
          <div className="flex-1 overflow-y-auto p-6">
            <div data-color-mode="light">
              <MDEditor
                value={formData.content[currentPage]}
                onChange={handlePageContentChange}
                height={500}
                preview="edit"
                className="w-full"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LessonEditor;
