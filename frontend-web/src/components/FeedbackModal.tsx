import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';

// Define the rating options with emojis, titles, and keywords
const RATINGS = {
  1: {
    emoji: '😞',
    title: 'Very Dissatisfied',
    words: [
      'Unacceptable', 'Frustrating', 'Poor', 'Lacking', 'Disappointing',
      'Inadequate', 'Insufficient', 'Horrible', 'Bad experience', 'Not helpful'
    ]
  },
  2: {
    emoji: '😕',
    title: 'Dissatisfied',
    words: [
      'Below expectations', 'Needs improvement', 'Subpar', 'Mediocre',
      'Could be better', 'Lacking quality', 'Unsatisfactory', 'Issues',
      'Slightly frustrating', 'Not as expected'
    ]
  },
  3: {
    emoji: '😐',
    title: 'Neutral',
    words: [
      'Average', 'Acceptable', 'Fair', 'Just okay', 'Neutral',
      'Nothing special', 'Meets basic needs', 'So-so',
      'Neither good nor bad', 'Room for improvement'
    ]
  },
  4: {
    emoji: '😊',
    title: 'Satisfied',
    words: [
      'Good', 'Pleasant', 'Worthwhile', 'Meets expectations', 'Positive',
      'Helpful', 'Convenient', 'Above average', 'Appreciated',
      'Better than expected'
    ]
  },
  5: {
    emoji: '🤩',
    title: 'Very Satisfied',
    words: [
      'Excellent', 'Outstanding', 'Amazing', 'Perfect', 'Wonderful',
      'Exceptional', 'Superb', 'Fantastic', 'Exceeds expectations',
      'Highly recommend'
    ]
  }
};

interface FeedbackModalProps {
  visible: boolean;
  onClose: () => void;
  onSubmit: (feedback: {
    rating: number;
    selectedWord: string;
    additionalFeedback: string;
  }) => void;
  error?: string | null;
}

const FeedbackModal: React.FC<FeedbackModalProps> = ({
  visible,
  onClose,
  onSubmit,
  error
}) => {
  const [rating, setRating] = useState<number | null>(null);
  const [selectedWord, setSelectedWord] = useState<string>('');
  const [additionalFeedback, setAdditionalFeedback] = useState('');
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    if (!visible) {
      // Reset states when modal is closed
      setRating(null);
      setSelectedWord('');
      setAdditionalFeedback('');
      setShowSuccess(false);
    }
  }, [visible]);

  const handleSubmit = () => {
    if (rating && selectedWord) {
      onSubmit({
        rating,
        selectedWord,
        additionalFeedback,
      });
      setShowSuccess(true);
    }
  };

  const canSubmit = rating !== null && selectedWord !== '';

  if (!visible) return null;

  if (showSuccess) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
        <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 overflow-hidden">
          <div className="p-6 text-center">
            <div className="text-5xl mb-4">🎉</div>
            <h3 className="text-2xl font-bold mb-4">Thank You!</h3>
            <p className="text-gray-600 mb-2">
              Your feedback helps us improve and create better learning experiences for everyone.
            </p>
            <p className="text-gray-600 mb-6">
              Keep up the great work with your learning journey!
            </p>
            <button 
              className="btn btn-primary w-full"
              onClick={onClose}
            >
              You're Welcome! 😊
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4 overflow-hidden">
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-xl font-bold">Chapter Feedback</h3>
            <button 
              className="btn btn-ghost btn-sm btn-circle"
              onClick={onClose}
            >
              <X size={20} />
            </button>
          </div>
          
          <p className="text-gray-600 mb-6">
            We'd love to hear your thoughts on this chapter. Your feedback helps us improve.
          </p>
          
          {error && (
            <div className="alert alert-error mb-4">
              <span>{error}</span>
            </div>
          )}
          
          {/* Rating Selection */}
          <div className="mb-6">
            <h4 className="font-medium mb-3">How would you rate this chapter?</h4>
            <div className="flex justify-between">
              {Object.entries(RATINGS).map(([ratingKey, ratingData]) => {
                const ratingNumber = parseInt(ratingKey);
                const isSelected = rating === ratingNumber;
                
                return (
                  <button
                    key={ratingKey}
                    className={`flex flex-col items-center p-2 rounded-lg transition-colors ${isSelected ? 'bg-primary/10 text-primary' : 'hover:bg-base-200'}`}
                    onClick={() => setRating(ratingNumber)}
                  >
                    <span className="text-2xl mb-1">{ratingData.emoji}</span>
                    <span className={`text-xs ${isSelected ? 'font-bold' : ''}`}>{ratingData.title}</span>
                  </button>
                );
              })}
            </div>
          </div>
          
          {/* Word Selection - Only show if rating is selected */}
          {rating && (
            <div className="mb-6">
              <h4 className="font-medium mb-3">Select a word that best describes your experience:</h4>
              <div className="flex flex-wrap gap-2">
                {RATINGS[rating as keyof typeof RATINGS].words.map((word) => (
                  <button
                    key={word}
                    className={`px-3 py-1.5 rounded-full border transition-colors ${selectedWord === word ? 'bg-primary text-white border-primary' : 'border-gray-200 hover:bg-base-200'}`}
                    onClick={() => setSelectedWord(word)}
                  >
                    {word}
                  </button>
                ))}
              </div>
            </div>
          )}
          
          {/* Additional Feedback */}
          <div className="mb-6">
            <h4 className="font-medium mb-3">Additional feedback (optional):</h4>
            <textarea
              className="textarea textarea-bordered w-full h-24"
              placeholder="Share more details about your experience..."
              value={additionalFeedback}
              onChange={(e) => setAdditionalFeedback(e.target.value)}
            />
          </div>
          
          {/* Submit Button */}
          <button
            className={`btn w-full ${canSubmit ? 'btn-primary' : 'btn-disabled'}`}
            onClick={handleSubmit}
            disabled={!canSubmit}
          >
            Submit Feedback
          </button>
        </div>
      </div>
    </div>
  );
};

export default FeedbackModal;
