import { useState, useEffect } from 'react';
import { Chapter, chapterService } from '../services/contentService';
import DashboardLayout from '../components/DashboardLayout';
import ChapterList from '../components/content/ChapterList';
import ChapterDetail from '../components/content/ChapterDetail';
import CreateChapterModal from '../components/content/CreateChapterModal';
import EditChapterModal from '../components/content/EditChapterModal';
import ConfirmationModal from '../components/modals/ConfirmationModal';
import Toast from '../components/ui/Toast';
import { Plus } from 'lucide-react';

const ContentManagement = () => {
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [selectedChapter, setSelectedChapter] = useState<Chapter | null>(null);
  const [loading, setLoading] = useState(true);
  const [chapterLoading, setChapterLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [chapterToDelete, setChapterToDelete] = useState<number | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  useEffect(() => {
    fetchChapters();
  }, []);

  const fetchChapters = async () => {
    try {
      setLoading(true);
      const data = await chapterService.getAllChapters();
      setChapters(data);
      
      // If we have a selected chapter, refresh it
      if (selectedChapter) {
        const updatedChapter = await chapterService.getChapter(selectedChapter.id);
        setSelectedChapter(updatedChapter);
      }
      setError(null);
    } catch (err) {
      setError('Failed to fetch chapters');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChapterSelect = async (chapterId: number) => {
    try {
      setChapterLoading(true);
      const chapter = await chapterService.getChapter(chapterId);
      setSelectedChapter(chapter);
    } catch (err) {
      setError('Failed to fetch chapter details');
      console.error(err);
    } finally {
      setChapterLoading(false);
    }
  };

  const handleChapterCreated = () => {
    fetchChapters();
    setIsCreateModalOpen(false);
  };

  const handleChapterUpdated = () => {
    fetchChapters();
    setIsEditModalOpen(false);
  };

  const handleChapterDeleted = async (chapterId: number) => {
    // Check if this is the last chapter
    const isLastChapter = chapters[chapters.length - 1].id === chapterId;
    if (!isLastChapter) {
      setError('You can only delete the most recent chapter');
      return;
    }

    setChapterToDelete(chapterId);
  };

  const handleConfirmDelete = async () => {
    if (!chapterToDelete) return;

    try {
      const chapterName = chapters.find(c => c.id === chapterToDelete)?.title || 'Chapter';
      await chapterService.deleteChapter(chapterToDelete);
      setChapters(chapters.filter(chapter => chapter.id !== chapterToDelete));
      if (selectedChapter?.id === chapterToDelete) {
        setSelectedChapter(null);
      }
      setError(null);
      setToast({
        message: `Successfully deleted "${chapterName}"`,
        type: 'success'
      });
    } catch (err) {
      setError('Failed to delete chapter');
      setToast({
        message: 'Failed to delete chapter. Please try again.',
        type: 'error'
      });
      console.error(err);
    } finally {
      setChapterToDelete(null);
    }
  };

  const handleContentUpdated = async () => {
    if (selectedChapter) {
      try {
        setChapterLoading(true);
        const updatedChapter = await chapterService.getChapter(selectedChapter.id);
        setSelectedChapter(updatedChapter);
        await fetchChapters(); // Refresh the chapter list in the background
      } catch (err) {
        setError('Failed to refresh chapter content');
        console.error(err);
      } finally {
        setChapterLoading(false);
      }
    }
  };

  return (
    <DashboardLayout>
      <div className="h-full flex">
        {/* Sidebar */}
        <div className="w-64 border-r border-base-300 flex flex-col h-full">
          <div className="p-4">
            <button
              className="btn btn-primary w-full mb-4"
              onClick={() => setIsCreateModalOpen(true)}
            >
              <Plus className="w-4 h-4 mr-2" />
              Create Chapter
            </button>
          </div>

          <div className="flex-1 overflow-y-auto p-4 pt-0">
            <ChapterList
              chapters={chapters}
              selectedChapterId={selectedChapter?.id}
              onSelect={handleChapterSelect}
              onDelete={handleChapterDeleted}
              onEdit={() => selectedChapter && setIsEditModalOpen(true)}
              loading={loading}
            />
          </div>
        </div>

        {/* Main Content */}
        <div className="flex-1 flex flex-col h-full overflow-hidden">
          {error && (
            <div className="p-6 pb-0">
              <div className="alert alert-error">
                <span>{error}</span>
              </div>
            </div>
          )}

          <div className="flex-1 overflow-y-auto p-6">
            {chapterLoading ? (
              <div className="flex flex-col items-center justify-center h-full">
                <span className="loading loading-spinner loading-lg"></span>
                <p className="mt-4 text-base-content/70">Loading chapter content...</p>
              </div>
            ) : selectedChapter ? (
              <ChapterDetail
                chapter={selectedChapter}
                onChapterUpdated={handleContentUpdated}
              />
            ) : (
              <div className="flex flex-col items-center justify-center h-full text-base-content/70">
                <p>Select a chapter to view its content</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <CreateChapterModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onChapterCreated={handleChapterCreated}
      />

      {selectedChapter && (
        <EditChapterModal
          isOpen={isEditModalOpen}
          onClose={() => setIsEditModalOpen(false)}
          onChapterUpdated={handleChapterUpdated}
          chapter={selectedChapter}
        />
      )}

      <ConfirmationModal
        isOpen={chapterToDelete !== null}
        title="Delete Chapter"
        message="Are you sure you want to delete this chapter and all its content? This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleConfirmDelete}
        onCancel={() => setChapterToDelete(null)}
        isDestructive={true}
      />

      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </DashboardLayout>
  );
};

export default ContentManagement;
