import { useState } from 'react';
import { QuizQuestion } from '../../../services/contentService';
import { Plus, Trash2, GripVertical, FileText, HelpCircle } from 'lucide-react';

interface QuizQuestionsStepProps {
  initialQuestions: QuizQuestion[];
  onSubmit: (questions: QuizQuestion[]) => void;
  onBack: () => void;
  loading?: boolean;
}

const QuizQuestionsStep = ({
  initialQuestions,
  onSubmit,
  onBack,
  loading = false,
}: QuizQuestionsStepProps) => {
  const [questions, setQuestions] = useState<QuizQuestion[]>(
    initialQuestions.length > 0
      ? initialQuestions
      : [
          {
            page: 1,
            type: 'identification',
            title: '',
            choices: [''],
            correct_answer: '',
          },
        ]
  );
  const [error, setError] = useState<string | null>(null);

  const addQuestion = () => {
    setQuestions([
      ...questions,
      {
        page: questions.length + 1,
        type: 'identification',
        title: '',
        choices: [''],
        correct_answer: '',
      },
    ]);
  };

  const removeQuestion = (index: number) => {
    if (questions.length === 1) {
      setError('Quiz must have at least one question');
      return;
    }

    const newQuestions = questions.filter((_, i) => i !== index);
    // Update page numbers
    newQuestions.forEach((q, i) => {
      q.page = i + 1;
    });
    setQuestions(newQuestions);
  };

  const updateQuestion = (index: number, updates: Partial<QuizQuestion>) => {
    const newQuestions = [...questions];
    newQuestions[index] = {
      ...newQuestions[index],
      ...updates,
    };

    // Reset choices and correct answer when type changes
    if (updates.type) {
      newQuestions[index].choices = [''];
      newQuestions[index].correct_answer = updates.type === 'identification' ? '' : [];
    }

    setQuestions(newQuestions);
  };

  const addChoice = (questionIndex: number) => {
    const newQuestions = [...questions];
    newQuestions[questionIndex].choices.push('');
    setQuestions(newQuestions);
  };

  const updateChoice = (questionIndex: number, choiceIndex: number, value: string) => {
    const newQuestions = [...questions];
    newQuestions[questionIndex].choices[choiceIndex] = value;
    setQuestions(newQuestions);
  };

  const removeChoice = (questionIndex: number, choiceIndex: number) => {
    const newQuestions = [...questions];
    newQuestions[questionIndex].choices.splice(choiceIndex, 1);
    setQuestions(newQuestions);
  };

  const toggleCorrectAnswer = (questionIndex: number, choice: string) => {
    const question = questions[questionIndex];
    if (question.type === 'multiple_choice') {
      const currentAnswers = Array.isArray(question.correct_answer)
        ? question.correct_answer
        : [];
      const newAnswers = currentAnswers.includes(choice)
        ? currentAnswers.filter((a) => a !== choice)
        : [...currentAnswers, choice];
      updateQuestion(questionIndex, { correct_answer: newAnswers });
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validate questions
    for (const [index, question] of questions.entries()) {
      if (!question.title.trim()) {
        setError(`Question ${index + 1} must have a title`);
        return;
      }

      if (question.choices.some((c) => !c.trim())) {
        setError(`All choices in question ${index + 1} must have content`);
        return;
      }

      if (question.type === 'identification') {
        if (!question.correct_answer) {
          setError(`Question ${index + 1} must have a correct answer`);
          return;
        }
      } else {
        const answers = Array.isArray(question.correct_answer)
          ? question.correct_answer
          : [];
        if (answers.length === 0) {
          setError(`Question ${index + 1} must have at least one correct answer`);
          return;
        }
      }
    }

    onSubmit(questions);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-8">
      {/* Questions */}
      <div className="space-y-6">
        {questions.map((question, questionIndex) => (
          <div
            key={questionIndex}
            className="card bg-base-200 shadow-sm"
          >
            <div className="card-body">
              {/* Question Header */}
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-4">
                  <span className="badge badge-lg">Page {question.page}</span>
                  <select
                    className="select select-bordered select-sm"
                    value={question.type}
                    onChange={(e) =>
                      updateQuestion(questionIndex, {
                        type: e.target.value as 'identification' | 'multiple_choice',
                      })
                    }
                  >
                    <option value="identification">Identification</option>
                    <option value="multiple_choice">Multiple Choice</option>
                  </select>
                </div>
                <button
                  type="button"
                  className="btn btn-ghost btn-sm btn-square text-error"
                  onClick={() => removeQuestion(questionIndex)}
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              {/* Question Title */}
              <div className="form-control">
                <input
                  type="text"
                  className="input input-bordered"
                  value={question.title}
                  onChange={(e) =>
                    updateQuestion(questionIndex, { title: e.target.value })
                  }
                  placeholder="Enter your question"
                />
              </div>

              {/* Choices */}
              <div className="space-y-2 mt-4">
                <label className="label">
                  <span className="label-text font-medium">Choices</span>
                </label>
                {question.choices.map((choice, choiceIndex) => (
                  <div key={choiceIndex} className="flex items-center gap-2">
                    {question.type === 'multiple_choice' ? (
                      <input
                        type="checkbox"
                        className="checkbox checkbox-primary"
                        checked={Array.isArray(question.correct_answer) && question.correct_answer.includes(choice)}
                        onChange={() => toggleCorrectAnswer(questionIndex, choice)}
                      />
                    ) : (
                      <input
                        type="radio"
                        className="radio radio-primary"
                        name={`correct_${questionIndex}`}
                        checked={question.correct_answer === choice}
                        onChange={() =>
                          updateQuestion(questionIndex, { correct_answer: choice })
                        }
                      />
                    )}
                    <input
                      type="text"
                      className="input input-bordered flex-1"
                      value={choice}
                      onChange={(e) =>
                        updateChoice(questionIndex, choiceIndex, e.target.value)
                      }
                      placeholder={`Choice ${choiceIndex + 1}`}
                    />
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm btn-square"
                      onClick={() => removeChoice(questionIndex, choiceIndex)}
                      disabled={question.choices.length <= 1}
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                ))}
                <button
                  type="button"
                  className="btn btn-ghost btn-sm mt-2"
                  onClick={() => addChoice(questionIndex)}
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Add Choice
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Add Question Button */}
      <button
        type="button"
        className="btn btn-outline w-full"
        onClick={addQuestion}
      >
        <Plus className="w-4 h-4 mr-2" />
        Add Question
      </button>

      {/* Error Message */}
      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
        </div>
      )}

      {/* Actions */}
      <div className="flex justify-between pt-4">
        <button type="button" className="btn btn-ghost" onClick={onBack}>
          Back
        </button>
        <button
          type="submit"
          className={`btn btn-primary ${loading ? 'loading' : ''}`}
          disabled={loading}
        >
          {loading ? 'Saving...' : 'Save Quiz'}
        </button>
      </div>
    </form>
  );
};

export default QuizQuestionsStep;
