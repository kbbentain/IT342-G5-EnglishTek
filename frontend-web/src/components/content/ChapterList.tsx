import { Chapter } from '../../services/contentService';
import { Trash2, Edit, BookOpen } from 'lucide-react';
import { getFullImageUrl } from '../../utils/urlUtils';

interface ChapterListProps {
  chapters: Chapter[];
  selectedChapterId?: number;
  onSelect: (chapterId: number) => void;
  onDelete: (chapterId: number) => void;
  onEdit: () => void;
  loading?: boolean;
}

const ChapterList = ({
  chapters,
  selectedChapterId,
  onSelect,
  onDelete,
  onEdit,
  loading = false,
}: ChapterListProps) => {
  if (loading) {
    return (
      <div className="flex justify-center items-center py-8">
        <span className="loading loading-spinner loading-md"></span>
      </div>
    );
  }

  if (chapters.length === 0) {
    return (
      <div className="text-center py-8 text-base-content/70">
        <p>No chapters yet</p>
      </div>
    );
  }

  return (
    <div className="space-y-1">
      {chapters.map((chapter, index) => (
        <button
          key={chapter.id}
          className={`w-full px-4 py-3 text-left hover:bg-base-200 flex items-center gap-3 transition-colors relative group
            ${selectedChapterId === chapter.id 
              ? 'bg-primary/10 text-primary border-l-4 border-primary' 
              : 'border-l-4 border-transparent'}`}
          onClick={() => onSelect(chapter.id)}
        >
          {/* Icon */}
          {chapter.iconUrl ? (
            <img
              src={getFullImageUrl(chapter.iconUrl)}
              alt={`${chapter.title} icon`}
              className="w-6 h-6 object-cover rounded flex-shrink-0"
            />
          ) : (
            <BookOpen className={`w-5 h-5 ${
              selectedChapterId === chapter.id 
                ? 'text-primary' 
                : 'text-base-content/70'
            }`} />
          )}

          {/* Content */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between gap-2">
              <h3 className={`font-medium truncate ${
                selectedChapterId === chapter.id ? '' : 'text-base-content/90'
              }`}>
                {index + 1}. {chapter.title}
              </h3>

              {/* Actions */}
              <div className={`flex items-center gap-1 ${
                selectedChapterId === chapter.id ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'
              } transition-opacity`}>
                {/* Only show edit button for selected chapter */}
                {selectedChapterId === chapter.id && (
                  <button
                    className="btn btn-ghost btn-xs btn-square text-base-content/70 hover:text-primary"
                    onClick={(e) => {
                      e.stopPropagation();
                      onEdit();
                    }}
                  >
                    <Edit className="w-3.5 h-3.5" />
                  </button>
                )}
                {index === chapters.length - 1 && (
                  <button
                    className="btn btn-ghost btn-xs btn-square text-base-content/70 hover:text-error"
                    onClick={(e) => {
                      e.stopPropagation();
                      onDelete(chapter.id);
                    }}
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            </div>

            {/* Description (if any) */}
            {chapter.description && (
              <p className="text-sm text-base-content/60 truncate mt-0.5">
                {chapter.description}
              </p>
            )}
          </div>
        </button>
      ))}
    </div>
  );
};

export default ChapterList;
