export type ChapterStatus = 'AVAILABLE' | 'LOCKED' | 'IN_PROGRESS' | 'COMPLETED';

export interface ChapterItem {
  id: number;
  type: 'lesson' | 'quiz';
  title: string;
  order: number;
  completed: boolean;
}

export interface Chapter {
  id: number;
  title: string;
  icon?: string;
  totalTasks: number;
  completedTasks: number;
  progressPercentage: number;
  status: ChapterStatus;
  items: ChapterItem[];
  hasCompletedFeedback: boolean;
  description?: string;
}

export interface ChapterSummary {
  id: number;
  title: string;
  icon?: string;
  status: ChapterStatus;
  progressPercentage: number;
  totalTasks: number;
  completedTasks: number;
}

export interface Lesson {
  id: number;
  chapterId: number;
  title: string;
  description: string;
  content: string[];
  completed: boolean;
}

export type QuestionType = 'identification' | 'multiple_choice';

interface BaseQuizQuestion {
  id: number;
  page: number;
  title: string;
  type: QuestionType;
}

interface IdentificationQuestion extends BaseQuizQuestion {
  type: 'identification';
  choices: string[];
  correct_answer: string;
}

interface MultipleChoiceQuestion extends BaseQuizQuestion {
  type: 'multiple_choice';
  choices: string[];
  correct_answer: string[];
}

export type QuizQuestion = IdentificationQuestion | MultipleChoiceQuestion;

export interface QuizDetails {
  id: number;
  chapterId: number;
  title: string;
  description: string;
  difficulty: number;
  maxScore: number;
  numberOfItems: number;
  badgeId: number;
  questions: QuizQuestion[];
  completed: boolean;
}

export interface Badge {
  id: number;
  name: string;
  description?: string;
  iconUrl?: string;
  dateObtained?: string;
}

export interface QuizSubmitResponse {
  score: number;
  maxScore: number;
  isEligibleForRetake: boolean;
  isEligibleForBadge: boolean;
  badge: Badge;
  badgeAwarded: boolean;
}

export type ActivityType = 'quiz' | 'lesson' | 'badge';

export interface ActivityLog {
  type: ActivityType;
  name: string;
  date: string;
}
