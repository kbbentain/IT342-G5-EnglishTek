import { useState } from 'react';
import { QuizRequest, QuizQuestion, quizService } from '../../services/contentService';
import { X, Plus, Trash2 } from 'lucide-react';

interface CreateQuizModalProps {
  isOpen: boolean;
  onClose: () => void;
  onQuizCreated: () => void;
  chapterId: number;
}

const CreateQuizModal = ({
  isOpen,
  onClose,
  onQuizCreated,
  chapterId,
}: CreateQuizModalProps) => {
  const [formData, setFormData] = useState<QuizRequest>({
    chapterId,
    title: '',
    description: '',
    timeLimit: 30,
    questions: [],
    order: 0,
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleAddQuestion = () => {
    setFormData(prev => ({
      ...prev,
      questions: [
        ...prev.questions,
        {
          question: '',
          options: ['', '', '', ''],
          correctAnswer: 0,
        },
      ],
    }));
  };

  const handleQuestionChange = (index: number, field: keyof QuizQuestion, value: any) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) =>
        i === index ? { ...q, [field]: value } : q
      ),
    }));
  };

  const handleOptionChange = (questionIndex: number, optionIndex: number, value: string) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) =>
        i === questionIndex
          ? {
              ...q,
              options: q.options.map((opt, j) =>
                j === optionIndex ? value : opt
              ),
            }
          : q
      ),
    }));
  };

  const handleRemoveQuestion = (index: number) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.filter((_, i) => i !== index),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await quizService.createQuiz(formData);
      onQuizCreated();
      setFormData({
        chapterId,
        title: '',
        description: '',
        timeLimit: 30,
        questions: [],
        order: 0,
      });
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create quiz');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-3xl">
        <button
          onClick={onClose}
          className="btn btn-sm btn-circle absolute right-2 top-2"
        >
          <X className="w-4 h-4" />
        </button>

        <h3 className="font-bold text-lg mb-4">Create New Quiz</h3>

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
              <span className="label-text">Description</span>
            </label>
            <textarea
              className="textarea textarea-bordered"
              value={formData.description}
              onChange={(e) =>
                setFormData({ ...formData, description: e.target.value })
              }
              required
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Time Limit (minutes)</span>
            </label>
            <input
              type="number"
              className="input input-bordered"
              value={formData.timeLimit}
              onChange={(e) =>
                setFormData({ ...formData, timeLimit: parseInt(e.target.value) })
              }
              min="1"
              required
            />
          </div>

          <div className="divider">Questions</div>

          <div className="space-y-6">
            {formData.questions.map((question, questionIndex) => (
              <div
                key={questionIndex}
                className="card bg-base-200 shadow-sm p-4"
              >
                <div className="flex justify-between items-start mb-4">
                  <h4 className="font-medium">Question {questionIndex + 1}</h4>
                  <button
                    type="button"
                    onClick={() => handleRemoveQuestion(questionIndex)}
                    className="btn btn-ghost btn-sm btn-square text-error"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="space-y-4">
                  <textarea
                    className="textarea textarea-bordered w-full"
                    value={question.question}
                    onChange={(e) =>
                      handleQuestionChange(
                        questionIndex,
                        'question',
                        e.target.value
                      )
                    }
                    placeholder="Enter question text"
                    required
                  />

                  <div className="grid grid-cols-1 gap-2">
                    {question.options.map((option, optionIndex) => (
                      <div key={optionIndex} className="flex items-center gap-2">
                        <input
                          type="radio"
                          name={`correct-${questionIndex}`}
                          className="radio"
                          checked={question.correctAnswer === optionIndex}
                          onChange={() =>
                            handleQuestionChange(
                              questionIndex,
                              'correctAnswer',
                              optionIndex
                            )
                          }
                          required
                        />
                        <input
                          type="text"
                          className="input input-bordered flex-1"
                          value={option}
                          onChange={(e) =>
                            handleOptionChange(
                              questionIndex,
                              optionIndex,
                              e.target.value
                            )
                          }
                          placeholder={`Option ${optionIndex + 1}`}
                          required
                        />
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ))}

            <button
              type="button"
              className="btn btn-outline w-full"
              onClick={handleAddQuestion}
            >
              <Plus className="w-4 h-4 mr-2" />
              Add Question
            </button>
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
              disabled={loading || formData.questions.length === 0}
            >
              {loading ? (
                <span className="loading loading-spinner"></span>
              ) : (
                'Create Quiz'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateQuizModal;
