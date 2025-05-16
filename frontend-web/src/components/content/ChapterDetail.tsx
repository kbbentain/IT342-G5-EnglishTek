import { useState, useEffect } from 'react';
import { Chapter, ChapterItem, lessonService, quizService, chapterService, badgeService } from '../../services/contentService';
import { DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { arrayMove, SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Trash2, Edit, BookOpen, FileText, HelpCircle, GripVertical, Eye } from 'lucide-react';
import LessonEditor from './LessonEditor';
import QuizEditor from './QuizEditor';
import PreviewLessonModal from './PreviewLessonModal';
import PreviewQuizModal from './PreviewQuizModal';
import { getFullImageUrl } from '../../utils/urlUtils';
import axiosInstance from '../../services/axiosConfig';

interface ChapterDetailProps {
  chapter: Chapter;
  onChapterUpdated: () => void;
}

const SortableItem = ({ item, index, items, isRearranging, onEdit, onDelete, onPreview }: any) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
  } = useSortable({ id: `${item.type}-${item.id}` });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`card bg-base-200 hover:bg-base-300 transition-colors ${
        isRearranging ? 'cursor-move' : ''
      }`}
    >
      <div className="card-body p-4">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className={`p-2 rounded-lg ${
              item.type === 'lesson' 
                ? 'bg-primary/10' 
                : 'bg-secondary/10'
            }`}>
              {item.type === 'lesson' ? (
                <FileText className="w-6 h-6 text-primary" />
              ) : (
                <HelpCircle className="w-6 h-6 text-secondary" />
              )}
            </div>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className={`badge badge-sm ${
                  item.type === 'lesson'
                    ? 'badge-primary'
                    : 'badge-secondary'
                }`}>
                  {item.type === 'lesson' ? 'Lesson' : 'Quiz'}
                </span>
                <span className="text-sm text-base-content/70">
                  #{index + 1}
                </span>
              </div>
              <h4 className="font-bold">{item.title}</h4>
              {item.description && (
                <p className="text-sm text-base-content/70 mt-1">
                  {item.description}
                </p>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            {!isRearranging ? (
              <div className="card-actions">
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => onPreview(item)}
                >
                  <Eye className="w-4 h-4 mr-2" />
                  Preview
                </button>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => onEdit(item)}
                >
                  <Edit className="w-4 h-4 mr-2" />
                  Edit
                </button>
                {index === items.length - 1 && (
                  <button
                    className="btn btn-ghost btn-sm text-error hover:bg-error hover:text-error-content"
                    onClick={() => onDelete(item)}
                  >
                    <Trash2 className="w-4 h-4 mr-2" />
                    Delete
                  </button>
                )}
              </div>
            ) : (
              <div
                {...attributes}
                {...listeners}
                className="p-2 hover:bg-base-300 rounded cursor-move"
              >
                <GripVertical className="w-4 h-4" />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

const ChapterDetail = ({ chapter, onChapterUpdated }: ChapterDetailProps) => {
  const [error, setError] = useState<string | null>(null);
  const [isLessonEditorOpen, setIsLessonEditorOpen] = useState(false);
  const [isQuizEditorOpen, setIsQuizEditorOpen] = useState(false);
  const [isPreviewLessonOpen, setIsPreviewLessonOpen] = useState(false);
  const [isPreviewQuizOpen, setIsPreviewQuizOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<number | undefined>(undefined);
  const [previewLesson, setPreviewLesson] = useState<any>(null);
  const [previewQuiz, setPreviewQuiz] = useState<any>(null);
  const [previewBadge, setPreviewBadge] = useState<any>(null);
  const [isRearranging, setIsRearranging] = useState(false);
  const [items, setItems] = useState<ChapterItem[]>([]);
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  useEffect(() => {
    setItems(chapter.items);
  }, [chapter]);

  const startRearranging = () => {
    setIsRearranging(true);
    setItems([...chapter.items]);
  };

  const cancelRearranging = () => {
    setIsRearranging(false);
    setItems([...chapter.items]);
  };

  const handleDragEnd = (event: any) => {
    const { active, over } = event;

    if (active.id !== over.id) {
      setItems((items) => {
        const oldIndex = items.findIndex((item) => `${item.type}-${item.id}` === active.id);
        const newIndex = items.findIndex((item) => `${item.type}-${item.id}` === over.id);
        return arrayMove(items, oldIndex, newIndex);
      });
    }
  };

  const handleApplyRearrange = async () => {
    try {
      const orderData = {
        order: items.map(item => ({
          id: item.id,
          type: item.type
        }))
      };

      await chapterService.rearrangeChapterItems(chapter.id, orderData);
      setIsRearranging(false);
      onChapterUpdated();
      setShowConfirmModal(false);
    } catch (err) {
      setError('Failed to rearrange items');
      console.error('Rearrange error:', err);
    }
  };

  const handleDeleteContent = async (item: ChapterItem) => {
    const isLastItem = chapter.items[chapter.items.length - 1].id === item.id;
    if (!isLastItem) {
      setError('You can only delete the most recent content');
      return;
    }

    if (!window.confirm('Are you sure you want to delete this content?')) {
      return;
    }

    try {
      if (item.type === 'lesson') {
        await lessonService.deleteLesson(item.id);
      } else {
        const quizData = await quizService.getQuiz(item.id);
        await quizService.deleteQuiz(item.id);
        
        if (quizData.badgeId) {
          try {
            await axiosInstance.delete(`/api/v1/badges/${quizData.badgeId}`);
          } catch (badgeErr) {
            console.error('Failed to delete badge:', badgeErr);
          }
        }
      }
      onChapterUpdated();
      setError(null);
    } catch (err) {
      setError('Failed to delete content');
      console.error(err);
    }
  };

  const handleEditContent = (item: ChapterItem) => {
    setSelectedItemId(item.id);
    if (item.type === 'lesson') {
      setIsLessonEditorOpen(true);
    } else {
      setIsQuizEditorOpen(true);
    }
  };

  const handlePreviewContent = async (item: ChapterItem) => {
    try {
      if (item.type === 'lesson') {
        const lesson = await lessonService.getLesson(item.id);
        setPreviewLesson(lesson);
        setIsPreviewLessonOpen(true);
      } else {
        const quiz = await quizService.getQuiz(item.id);
        setPreviewQuiz(quiz);
        
        // Fetch badge info if available
        if (quiz.badgeId) {
          try {
            const badge = await badgeService.getBadge(quiz.badgeId);
            setPreviewBadge(badge);
          } catch (err) {
            console.error('Failed to fetch badge:', err);
            setPreviewBadge(null);
          }
        } else {
          setPreviewBadge(null);
        }
        
        setIsPreviewQuizOpen(true);
      }
    } catch (err) {
      console.error('Failed to fetch content for preview:', err);
      setError('Failed to load content for preview');
    }
  };

  return (
    <div className="space-y-6">
      {/* Chapter Header Card */}
      <div className="card bg-base-100 shadow-xl">
        <div className="card-body">
          <div className="flex items-start gap-6">
            {chapter.iconUrl && (
              <img
                src={getFullImageUrl(chapter.iconUrl)}
                alt={`${chapter.title} icon`}
                className="w-24 h-24 object-cover rounded-lg shadow-md"
              />
            )}
            <div className="flex-1">
              <h2 className="card-title text-2xl">{chapter.title}</h2>
              <p className="text-base-content/70 mt-1">{chapter.description}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="alert alert-error shadow-lg">
          <span>{error}</span>
        </div>
      )}

      {/* Content Actions Card */}
      <div className="card bg-base-100 shadow-lg">
        <div className="card-body">
          <h3 className="card-title text-lg mb-4">Add Content</h3>
          <div className="flex gap-4">
            <button
              className="btn btn-primary flex-1"
              onClick={() => {
                setSelectedItemId(undefined);
                setIsLessonEditorOpen(true);
              }}
            >
              <FileText className="w-4 h-4 mr-2" />
              Add Lesson
            </button>
            <button
              className="btn btn-secondary flex-1"
              onClick={() => {
                setSelectedItemId(undefined);
                setIsQuizEditorOpen(true);
              }}
            >
              <HelpCircle className="w-4 h-4 mr-2" />
              Add Quiz
            </button>
          </div>
        </div>
      </div>

      {/* Content List */}
      <div className="card bg-base-100 shadow-lg">
        <div className="card-body">
          <div className="flex justify-between items-center mb-4">
            <h3 className="card-title text-lg">Chapter Content</h3>
            {!isRearranging ? (
              <button
                className="btn btn-outline btn-sm"
                onClick={startRearranging}
              >
                <GripVertical className="w-4 h-4 mr-2" />
                Rearrange
              </button>
            ) : (
              <div className="space-x-2">
                <button
                  className="btn btn-outline btn-error btn-sm"
                  onClick={cancelRearranging}
                >
                  Cancel
                </button>
                <button
                  className="btn btn-outline btn-success btn-sm"
                  onClick={() => setShowConfirmModal(true)}
                >
                  Apply
                </button>
              </div>
            )}
          </div>
          
          {items.length === 0 ? (
            <div className="text-center py-8 text-base-content/70 bg-base-200 rounded-lg">
              <BookOpen className="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>No content yet</p>
              <p className="text-sm">Start by adding a lesson or quiz above</p>
            </div>
          ) : (
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={handleDragEnd}
            >
              <SortableContext
                items={items.map(item => `${item.type}-${item.id}`)}
                strategy={verticalListSortingStrategy}
              >
                <div className="space-y-4">
                  {items.map((item, index) => (
                    <SortableItem
                      key={`${item.type}-${item.id}`}
                      item={item}
                      index={index}
                      items={items}
                      isRearranging={isRearranging}
                      onEdit={handleEditContent}
                      onDelete={handleDeleteContent}
                      onPreview={handlePreviewContent}
                    />
                  ))}
                </div>
              </SortableContext>
            </DndContext>
          )}
        </div>
      </div>

      {/* Confirmation Modal */}
      {showConfirmModal && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-lg mb-4">Confirm Rearrange</h3>
            <p>
              Rearranging items will delete all student progress for this chapter. 
              Are you sure you want to continue?
            </p>
            <div className="modal-action">
              <button
                className="btn btn-ghost"
                onClick={() => setShowConfirmModal(false)}
              >
                Cancel
              </button>
              <button
                className="btn btn-error"
                onClick={handleApplyRearrange}
              >
                Yes, Continue
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Editors */}
      {isLessonEditorOpen && (
        <LessonEditor
          chapterId={chapter.id}
          initialLesson={selectedItemId ? {
            id: selectedItemId,
            title: '',
            description: '',
            content: ['']
          } : undefined}
          onSave={() => {
            setIsLessonEditorOpen(false);
            onChapterUpdated();
          }}
          onClose={() => setIsLessonEditorOpen(false)}
        />
      )}

      {isQuizEditorOpen && (
        <QuizEditor
          chapterId={chapter.id}
          initialQuizId={selectedItemId}
          onSave={() => {
            setIsQuizEditorOpen(false);
            onChapterUpdated();
          }}
          onClose={() => setIsQuizEditorOpen(false)}
        />
      )}

      {/* Preview Modals */}
      {isPreviewLessonOpen && previewLesson && (
        <PreviewLessonModal
          isOpen={isPreviewLessonOpen}
          onClose={() => setIsPreviewLessonOpen(false)}
          lesson={previewLesson}
        />
      )}

      {isPreviewQuizOpen && previewQuiz && (
        <PreviewQuizModal
          isOpen={isPreviewQuizOpen}
          onClose={() => setIsPreviewQuizOpen(false)}
          quiz={previewQuiz}
          badge={previewBadge}
        />
      )}
    </div>
  );
};

export default ChapterDetail;
