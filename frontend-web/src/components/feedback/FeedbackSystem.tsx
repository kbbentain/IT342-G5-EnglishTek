import { useState, useEffect } from 'react';
import { Chapter, chapterService } from '../../services/contentService';
import { FeedbackResponse, feedbackService } from '../../services/feedbackService';
import { MessageSquare, Trash2, ChevronDown, ChevronUp, Search, ArrowUpDown } from 'lucide-react';

type SortField = 'date' | 'username' | 'rating';
type SortOrder = 'asc' | 'desc';

const FeedbackSystem = () => {
  const [chapters, setChapters] = useState<Chapter[]>([]);
  const [selectedChapter, setSelectedChapter] = useState<number | null>(null);
  const [feedbacks, setFeedbacks] = useState<FeedbackResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedFeedbacks, setExpandedFeedbacks] = useState<Set<number>>(new Set());
  const [searchTerm, setSearchTerm] = useState('');
  const [sortField, setSortField] = useState<SortField>('date');
  const [sortOrder, setSortOrder] = useState<SortOrder>('desc');

  // Get rating emoji and color scheme
  const getRatingScheme = (rating: number) => {
    switch (rating) {
      case 1:
        return {
          bg: 'bg-error/10',
          border: 'border-error/20',
          text: 'text-error',
          icon: 'text-error fill-error',
          emoji: '😡'
        };
      case 2:
        return {
          bg: 'bg-error/5',
          border: 'border-error/10',
          text: 'text-error/80',
          icon: 'text-error/80 fill-error/80',
          emoji: '😕'
        };
      case 3:
        return {
          bg: 'bg-warning/10',
          border: 'border-warning/20',
          text: 'text-warning',
          icon: 'text-warning fill-warning',
          emoji: '😐'
        };
      case 4:
        return {
          bg: 'bg-success/5',
          border: 'border-success/10',
          text: 'text-success/80',
          icon: 'text-success/80 fill-success/80',
          emoji: '😊'
        };
      case 5:
        return {
          bg: 'bg-success/10',
          border: 'border-success/20',
          text: 'text-success',
          icon: 'text-success fill-success',
          emoji: '😍'
        };
      default:
        return {
          bg: 'bg-base-200',
          border: 'border-base-300',
          text: 'text-base-content',
          icon: 'text-base-content',
          emoji: '😐'
        };
    }
  };

  // Fetch chapters on mount
  useEffect(() => {
    const fetchChapters = async () => {
      try {
        const response = await chapterService.getAllChapters();
        setChapters(response);
        if (response.length > 0) {
          setSelectedChapter(response[0].id);
        }
      } catch (err) {
        setError('Failed to load chapters');
        console.error(err);
      }
    };

    fetchChapters();
  }, []);

  // Fetch feedbacks when chapter is selected
  useEffect(() => {
    const fetchFeedbacks = async () => {
      if (!selectedChapter) return;

      try {
        setLoading(true);
        setError(null);
        const response = await feedbackService.getFeedbacksByChapter(selectedChapter);
        setFeedbacks(response);
      } catch (err) {
        setError('Failed to load feedbacks');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchFeedbacks();
  }, [selectedChapter]);

  const handleDeleteFeedback = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this feedback?')) {
      return;
    }

    try {
      await feedbackService.deleteFeedback(id);
      setFeedbacks(feedbacks.filter(f => f.id !== id));
    } catch (err) {
      setError('Failed to delete feedback');
      console.error(err);
    }
  };

  const toggleFeedbackExpand = (id: number) => {
    const newExpanded = new Set(expandedFeedbacks);
    if (newExpanded.has(id)) {
      newExpanded.delete(id);
    } else {
      newExpanded.add(id);
    }
    setExpandedFeedbacks(newExpanded);
  };

  const renderRating = (rating: number) => {
    const scheme = getRatingScheme(rating);
    return (
      <div className="flex items-center gap-1">
        <span className="text-xl">{scheme.emoji}</span>
        <span className={`text-sm ${scheme.text}`}>
          {rating}/5
        </span>
      </div>
    );
  };

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      // Toggle order if same field
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      // Set new field and default to descending
      setSortField(field);
      setSortOrder('desc');
    }
  };

  // Filter and sort feedbacks
  const sortedAndFilteredFeedbacks = feedbacks
    .filter(feedback =>
      feedback.feedbackText?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      feedback.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      feedback.feedbackKeyword?.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => {
      const order = sortOrder === 'asc' ? 1 : -1;
      switch (sortField) {
        case 'date':
          return (new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()) * order;
        case 'username':
          return a.username.localeCompare(b.username) * order;
        case 'rating':
          return (a.rating - b.rating) * order;
        default:
          return 0;
      }
    });

  const selectedChapterTitle = chapters.find(c => c.id === selectedChapter)?.title;

  // Render sort button
  const SortButton = ({ field, label }: { field: SortField; label: string }) => (
    <button
      className={`btn btn-ghost btn-sm normal-case ${sortField === field ? 'text-primary' : ''}`}
      onClick={() => handleSort(field)}
    >
      {label}
      {sortField === field && (
        <ArrowUpDown className={`w-4 h-4 ${sortOrder === 'asc' ? 'rotate-180' : ''}`} />
      )}
    </button>
  );

  return (
    <div className="flex h-[calc(100vh-4rem)] bg-base-200">
      {/* Left Sidebar - Chapter List */}
      <div className="w-80 bg-base-100 border-r border-base-300 flex flex-col">
        <div className="p-4 border-b border-base-300">
          <h1 className="text-xl font-bold">Feedback</h1>
        </div>
        <div className="overflow-y-auto flex-1">
          {loading ? (
            <div className="flex justify-center items-center py-8">
              <span className="loading loading-spinner loading-md"></span>
            </div>
          ) : chapters.map((chapter) => (
            <button
              key={chapter.id}
              onClick={() => setSelectedChapter(chapter.id)}
              className={`w-full px-4 py-3 text-left hover:bg-base-200 flex items-center gap-3 transition-colors
                ${selectedChapter === chapter.id ? 'bg-primary/10 border-l-4 border-primary' : 'border-l-4 border-transparent'}`}
            >
              <MessageSquare className={`w-5 h-5 ${selectedChapter === chapter.id ? 'text-primary' : 'text-base-content/70'}`} />
              <div className="flex-1 truncate">
                <span className={selectedChapter === chapter.id ? 'font-medium' : ''}>
                  {chapter.title}
                </span>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <div className="bg-base-100 border-b border-base-300 p-4">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">
              {selectedChapterTitle}
              {selectedChapter && (
                <span className="ml-2 text-sm text-base-content/70">
                  {sortedAndFilteredFeedbacks.length} responses
                </span>
              )}
            </h2>
          </div>
          <div className="flex items-center gap-4">
            <div className="relative flex-1">
              <input
                type="text"
                placeholder="Search feedback..."
                className="input input-bordered w-full pl-10"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-base-content/50" />
            </div>
            <div className="flex items-center gap-2 border-l border-base-300 pl-4">
              <span className="text-sm text-base-content/70">Sort by:</span>
              <SortButton field="date" label="Date" />
              <SortButton field="username" label="Name" />
              <SortButton field="rating" label="Rating" />
            </div>
          </div>
        </div>

        {/* Feedback List */}
        <div className="flex-1 overflow-y-auto p-4">
          {error && (
            <div className="alert alert-error mb-4">
              <span>{error}</span>
            </div>
          )}

          {loading ? (
            <div className="flex justify-center py-8">
              <div className="loading loading-spinner loading-lg"></div>
            </div>
          ) : sortedAndFilteredFeedbacks.length === 0 ? (
            <div className="text-center py-12">
              <MessageSquare className="w-16 h-16 mx-auto mb-4 text-base-content/30" />
              <h3 className="text-lg font-medium mb-2">No feedback found</h3>
              <p className="text-base-content/70">
                {searchTerm
                  ? "No feedback matches your search"
                  : "No feedback has been submitted for this chapter yet"}
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {sortedAndFilteredFeedbacks.map((feedback) => {
                const scheme = getRatingScheme(feedback.rating);
                return (
                  <div
                    key={feedback.id}
                    className={`border rounded-lg ${scheme.bg} ${scheme.border} transition-all`}
                  >
                    <div className="p-4">
                      {/* Header */}
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center gap-3">
                          <div className="avatar placeholder">
                            <div className="bg-neutral text-neutral-content rounded-full w-8">
                              <span className="text-xs">{feedback.username.substring(0, 2).toUpperCase()}</span>
                            </div>
                          </div>
                          <div>
                            <h3 className="font-medium">{feedback.username}</h3>
                            <div className="flex items-center gap-2 mt-1">
                              {renderRating(feedback.rating)}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm text-base-content/50">
                            {new Date(feedback.createdAt).toLocaleDateString()}
                          </span>
                          <button
                            className="btn btn-ghost btn-sm btn-square text-error"
                            onClick={() => handleDeleteFeedback(feedback.id)}
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>

                      {/* Content */}
                      <div className="space-y-3">
                        {feedback.feedbackKeyword && (
                          <div className="flex items-center gap-2">
                            <span className={`px-2 py-1 rounded text-sm ${scheme.bg} ${scheme.text}`}>
                              {feedback.feedbackKeyword}
                            </span>
                          </div>
                        )}

                        {feedback.feedbackText && (
                          <div>
                            <button
                              className="btn btn-ghost btn-sm px-0 gap-2"
                              onClick={() => toggleFeedbackExpand(feedback.id)}
                            >
                              <span>Feedback</span>
                              {expandedFeedbacks.has(feedback.id) ? (
                                <ChevronUp className="w-4 h-4" />
                              ) : (
                                <ChevronDown className="w-4 h-4" />
                              )}
                            </button>
                            {expandedFeedbacks.has(feedback.id) && (
                              <div className="mt-2 p-3 bg-base-100 rounded-lg border border-base-300">
                                <p className="whitespace-pre-wrap text-base-content/80">
                                  {feedback.feedbackText}
                                </p>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default FeedbackSystem;
