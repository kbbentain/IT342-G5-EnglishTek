import { useState } from 'react';
import { X, ChevronLeft, ChevronRight, BookOpen, FileText, Clock, Bookmark } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface PreviewLessonModalProps {
  isOpen: boolean;
  onClose: () => void;
  lesson: {
    id: number;
    title: string;
    description: string;
    content: string[];
  };
}

const PreviewLessonModal = ({ isOpen, onClose, lesson }: PreviewLessonModalProps) => {
  const [currentPage, setCurrentPage] = useState(0);
  const [readingMode, setReadingMode] = useState(false);

  if (!isOpen) return null;

  // Estimate reading time (rough calculation: 200 words per minute)
  const wordCount = lesson.content[currentPage].split(/\s+/).length;
  const readingTimeMinutes = Math.max(1, Math.ceil(wordCount / 200));

  const handleNextPage = () => {
    if (currentPage < lesson.content.length - 1) {
      setCurrentPage(prev => prev + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(prev => prev - 1);
    }
  };

  const toggleReadingMode = () => {
    setReadingMode(!readingMode);
  };

  return (
    <div className="fixed inset-0 bg-base-300/80 backdrop-blur-sm z-50 flex items-center justify-center overflow-y-auto py-8">
      <div 
        className={`bg-base-100 w-full ${readingMode ? 'max-w-3xl' : 'max-w-4xl'} rounded-xl shadow-xl flex flex-col max-h-[90vh] transition-all duration-300 ease-in-out`}
      >
        {/* Header */}
        <div className="p-6 border-b border-base-300 flex items-center justify-between bg-gradient-to-r from-primary/10 to-secondary/10">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-primary/20 rounded-lg">
              <BookOpen className="w-6 h-6 text-primary" />
            </div>
            <div>
              <h2 className="text-xl font-bold">{lesson.title}</h2>
              <p className="text-base-content/70">{lesson.description}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="btn btn-ghost btn-sm btn-circle"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Reading stats */}
        <div className="bg-base-200 p-3 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 text-sm">
              <Clock className="w-4 h-4 text-primary" />
              <span>{readingTimeMinutes} min read</span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <FileText className="w-4 h-4 text-primary" />
              <span>Page {currentPage + 1} of {lesson.content.length}</span>
            </div>
          </div>
          <div>
            <button 
              className="btn btn-sm btn-ghost"
              onClick={toggleReadingMode}
            >
              {readingMode ? (
                <>
                  <Bookmark className="w-4 h-4 mr-2" />
                  Exit Reading Mode
                </>
              ) : (
                <>
                  <Bookmark className="w-4 h-4 mr-2" />
                  Reading Mode
                </>
              )}
            </button>
          </div>
        </div>

        {/* Progress bar */}
        <div className="w-full bg-base-300 h-1">
          <div 
            className="bg-primary h-1 transition-all duration-300"
            style={{ width: `${((currentPage + 1) / lesson.content.length) * 100}%` }}
          />
        </div>

        {/* Content */}
        <div className={`flex-1 overflow-y-auto p-6 ${readingMode ? 'bg-base-200' : ''}`}>
          <div
            key={currentPage}
            className={`prose max-w-none transition-opacity duration-300 ${readingMode ? 'mx-auto bg-base-100 p-8 rounded-lg shadow-lg' : ''}`}
          >
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {lesson.content[currentPage]}
            </ReactMarkdown>
          </div>
        </div>

        {/* Page navigation */}
        <div className="p-4 border-t border-base-300 flex items-center justify-between bg-base-100">
          <div className="flex items-center gap-2">
            {Array.from({ length: lesson.content.length }).map((_, index) => (
              <button 
                key={index}
                onClick={() => setCurrentPage(index)}
                className={`w-2 h-2 rounded-full transition-all ${currentPage === index ? 'bg-primary w-4' : 'bg-base-300'}`}
                aria-label={`Go to page ${index + 1}`}
              />
            ))}
          </div>

          <div className="flex items-center gap-2">
            <button
              className="btn btn-sm btn-ghost"
              onClick={handlePrevPage}
              disabled={currentPage === 0}
            >
              <ChevronLeft className="w-4 h-4 mr-1" />
              Previous
            </button>
            <button
              className="btn btn-sm btn-primary"
              onClick={handleNextPage}
              disabled={currentPage === lesson.content.length - 1}
            >
              Next
              <ChevronRight className="w-4 h-4 ml-1" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PreviewLessonModal;
