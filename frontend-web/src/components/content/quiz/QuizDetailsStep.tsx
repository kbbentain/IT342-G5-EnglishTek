import { useState } from 'react';

interface QuizDetailsStepProps {
  initialDetails: {
    title: string;
    description: string;
    difficulty: number;
    isRandom?: boolean;
  };
  onSubmit: (details: {
    title: string;
    description: string;
    difficulty: number;
    isRandom: boolean;
  }) => void;
  onBack: () => void;
}

const QuizDetailsStep = ({
  initialDetails,
  onSubmit,
  onBack,
}: QuizDetailsStepProps) => {
  const [title, setTitle] = useState(initialDetails.title);
  const [description, setDescription] = useState(initialDetails.description);
  const [difficulty, setDifficulty] = useState(initialDetails.difficulty);
  const [isRandom, setIsRandom] = useState(initialDetails.isRandom || false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      setError('Title is required');
      return;
    }

    if (!description.trim()) {
      setError('Description is required');
      return;
    }

    if (difficulty < 1 || difficulty > 3) {
      setError('Difficulty must be between 1 and 3');
      return;
    }

    onSubmit({
      title,
      description,
      difficulty,
      isRandom,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Title */}
      <div className="form-control">
        <label className="label">
          <span className="label-text">Quiz Title</span>
        </label>
        <label className="input input-bordered flex items-center gap-2">
        <input
          type="text"
          className="grow"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="e.g., English Grammar Quiz"
        />
        </label>
      </div>

      {/* Description */}
      <div className="form-control">
        <label className="label">
          <span className="label-text">Description</span>
        </label>
        <textarea
          className="textarea textarea-bordered h-24"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="e.g., Test your knowledge of basic English grammar"
        />
      </div>

      {/* Difficulty */}
      <div className="form-control">
        <label className="label">
          <span className="label-text">Difficulty Level (1-3)</span>
        </label>
        <div className="flex items-center gap-4">
          <input
            type="range"
            min={1}
            max={3}
            value={difficulty}
            onChange={(e) => setDifficulty(Number(e.target.value))}
            className="range range-primary flex-1"
            step={1}
          />
          <div className="w-12 text-center font-bold">{difficulty}</div>
        </div>
        <div className="flex justify-between px-2 mt-2">
          <div className="flex flex-col items-center">
            <span className="text-xs text-base-content/70">Easy</span>
            <span className="font-bold">1</span>
          </div>
          <div className="flex flex-col items-center">
            <span className="text-xs text-base-content/70">Medium</span>
            <span className="font-bold">2</span>
          </div>
          <div className="flex flex-col items-center">
            <span className="text-xs text-base-content/70">Hard</span>
            <span className="font-bold">3</span>
          </div>
        </div>
      </div>

      {/* Randomize Questions */}
      <div className="form-control">
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            className="checkbox checkbox-primary"
            checked={isRandom}
            onChange={(e) => setIsRandom(e.target.checked)}
          />
          <span className="label-text">Randomize Questions</span>
        </div>
        <label className="label">
          <span className="label-text-alt text-base-content/70">
            When enabled, questions will be presented in random order for each student
          </span>
        </label>
      </div>

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
        <button type="submit" className="btn btn-primary">
          Continue
        </button>
      </div>
    </form>
  );
};

export default QuizDetailsStep;
