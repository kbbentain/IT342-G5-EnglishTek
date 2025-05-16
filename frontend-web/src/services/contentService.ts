import axiosInstance from './axiosConfig';

// Types
export interface Chapter {
  id: number;
  title: string;
  description: string;
  iconUrl?: string;
  items: ChapterItem[];
  hasCompletedFeedback?: boolean;
  totalTasks?: number;
  completedTasks?: number;
  progressPercentage?: number;
  status?: string;
}

export interface ChapterItem {
  id: number;
  type: 'lesson' | 'quiz';
  title: string;
  description?: string;
  order: number;
  content?: string[];
  difficulty?: number;
  maxScore?: number;
  badgeId?: number;
  questions?: QuizQuestion[];
}

export interface Lesson {
  id: number;
  chapterId: number;
  title: string;
  description: string;
  content: string[];
  completed?: boolean;
}

export interface ChapterRequest {
  title: string;
  description: string;
  image?: File;
}

export interface LessonRequest {
  chapterId: number;
  title: string;
  description: string;
  content: string[];
}

export interface Badge {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
}

export interface BadgeRequest {
  name: string;
  description: string;
  icon: File;
}

export interface QuizQuestion {
  id?: number;
  page: number;
  type: 'identification' | 'multiple_choice';
  title: string;
  choices: string[];
  correct_answer: string | string[];
}

export interface Quiz {
  id: number;
  chapterId: number;
  title: string;
  description: string;
  difficulty: number;
  maxScore: number;
  numberOfItems: number;
  badgeId: number;
  questions: QuizQuestion[];
  completed?: boolean;
}

export interface QuizRequest {
  chapterId: number;
  title: string;
  description: string;
  difficulty: number;
  maxScore: number;
  numberOfItems: number;
  badgeId: number;
  questions: QuizQuestion[];
  isRandom?: boolean;
}

export interface ChapterRearrangeRequest {
  order: Array<{
    id: number;
    type: 'lesson' | 'quiz';
  }>;
}

// Chapter Service
export const chapterService = {
  getAllChapters: async (): Promise<Chapter[]> => {
    try {
      const response = await axiosInstance.get('/api/v1/chapters');
      // Transform the response to match the Chapter type
      return response.data.map((chapter: any) => ({
        id: chapter.id,
        title: chapter.title,
        description: chapter.description || '',
        iconUrl: chapter.iconUrl || '',
        totalTasks: chapter.totalTasks || chapter.items?.length || 0,
        completedTasks: chapter.completedTasks || 0,
        progressPercentage: chapter.progressPercentage || 0,
        status: chapter.status || 'AVAILABLE',
        items: chapter.items || [],
        hasCompletedFeedback: chapter.hasCompletedFeedback || false
      }));
    } catch (error) {
      console.error('Error fetching chapters:', error);
      throw error;
    }
  },

  getChapter: async (id: number): Promise<Chapter> => {
    try {
      const response = await axiosInstance.get(`/api/v1/chapters/${id}`);
      const chapter = response.data;
      // Transform the response to match the Chapter type
      return {
        id: chapter.id,
        title: chapter.title,
        description: chapter.description || '',
        iconUrl: chapter.iconUrl || '',
        totalTasks: chapter.totalTasks || chapter.items?.length || 0,
        completedTasks: chapter.completedTasks || 0,
        progressPercentage: chapter.progressPercentage || 0,
        status: chapter.status || 'AVAILABLE',
        items: chapter.items || [],
        hasCompletedFeedback: chapter.hasCompletedFeedback || false
      };
    } catch (error) {
      console.error(`Error fetching chapter ${id}:`, error);
      throw error;
    }
  },

  createChapter: async (data: ChapterRequest): Promise<Chapter> => {
    const formData = new FormData();
    formData.append('title', data.title);
    formData.append('description', data.description);
    if (data.image) {
      formData.append('icon', data.image);
    }

    const response = await axiosInstance.post('/api/v1/chapters', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  updateChapter: async (id: number, data: ChapterRequest): Promise<Chapter> => {
    const formData = new FormData();
    formData.append('title', data.title);
    formData.append('description', data.description);
    if (data.image) {
      formData.append('icon', data.image);
    }

    const response = await axiosInstance.put(`/api/v1/chapters/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  deleteChapter: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/api/v1/chapters/${id}`);
  },

  rearrangeChapterItems: async (chapterId: number, data: ChapterRearrangeRequest): Promise<Chapter> => {
    const response = await axiosInstance.put(`/api/v1/chapters/${chapterId}/rearrange`, data);
    return response.data;
  },
};

// Lesson Service
export const lessonService = {
  getLesson: async (id: number): Promise<Lesson> => {
    const response = await axiosInstance.get(`/api/v1/lessons/${id}`);
    return response.data;
  },

  createLesson: async (data: LessonRequest): Promise<ChapterItem> => {
    const response = await axiosInstance.post('/api/v1/lessons', data);
    return response.data;
  },

  updateLesson: async (id: number, data: LessonRequest): Promise<ChapterItem> => {
    const response = await axiosInstance.put(`/api/v1/lessons/${id}`, data);
    return response.data;
  },

  deleteLesson: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/api/v1/lessons/${id}`);
  },
};

// Badge Service
export const badgeService = {
  getBadge: async (id: number): Promise<Badge> => {
    const response = await axiosInstance.get(`/api/v1/badges/${id}`);
    return response.data;
  },

  createBadge: async (data: BadgeRequest): Promise<Badge> => {
    const formData = new FormData();
    formData.append('name', data.name);
    formData.append('description', data.description);
    formData.append('icon', data.icon);

    const response = await axiosInstance.post('/api/v1/badges', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  updateBadge: async (id: number, data: BadgeRequest): Promise<Badge> => {
    const formData = new FormData();
    formData.append('name', data.name);
    formData.append('description', data.description);
    formData.append('icon', data.icon);

    const response = await axiosInstance.put(`/api/v1/badges/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

// Quiz Service
export const quizService = {
  getQuiz: async (id: number): Promise<Quiz> => {
    const response = await axiosInstance.get(`/api/v1/quizzes/${id}`);
    return response.data;
  },

  createQuiz: async (data: QuizRequest): Promise<ChapterItem> => {
    const response = await axiosInstance.post('/api/v1/quizzes', data);
    return response.data;
  },

  updateQuiz: async (id: number, data: QuizRequest): Promise<ChapterItem> => {
    const response = await axiosInstance.put(`/api/v1/quizzes/${id}`, data);
    return response.data;
  },

  deleteQuiz: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/api/v1/quizzes/${id}`);
  },
};

// User-specific endpoints for the user side
export const getChapters = async (): Promise<Chapter[]> => {
  try {
    const response = await axiosInstance.get('/api/v1/chapters');
    // Transform the response to match the Chapter type
    return response.data.map((chapter: any) => ({
      id: chapter.id,
      title: chapter.title,
      description: chapter.description || '',
      iconUrl: chapter.iconUrl || '',
      totalTasks: chapter.totalTasks || chapter.items?.length || 0,
      completedTasks: chapter.completedTasks || 0,
      progressPercentage: chapter.progressPercentage || 0,
      status: chapter.status || 'AVAILABLE',
      items: chapter.items || [],
      hasCompletedFeedback: chapter.hasCompletedFeedback || false
    }));
  } catch (error) {
    console.error('Error fetching chapters:', error);
    throw error;
  }
};

export const getChapterDetails = async (chapterId: number): Promise<Chapter> => {
  try {
    const response = await axiosInstance.get(`/api/v1/chapters/${chapterId}`);
    const chapter = response.data;
    // Transform the response to match the Chapter type
    return {
      id: chapter.id,
      title: chapter.title,
      description: chapter.description || '',
      iconUrl: chapter.iconUrl || '',
      totalTasks: chapter.totalTasks || chapter.items?.length || 0,
      completedTasks: chapter.completedTasks || 0,
      progressPercentage: chapter.progressPercentage || 0,
      status: chapter.status || 'AVAILABLE',
      items: chapter.items || [],
      hasCompletedFeedback: chapter.hasCompletedFeedback || false
    };
  } catch (error) {
    console.error(`Error fetching chapter ${chapterId}:`, error);
    throw error;
  }
};

export const getRecentActivities = async (): Promise<any[]> => {
  try {
    const response = await axiosInstance.get('/api/v1/activity-log/short');
    return response.data;
  } catch (error) {
    console.error('Error fetching recent activities:', error);
    throw error;
  }
};

export const getAllBadges = async (): Promise<Badge[]> => {
  try {
    const response = await axiosInstance.get('/api/v1/badges');
    return response.data;
  } catch (error) {
    console.error('Error fetching all badges:', error);
    throw error;
  }
};

export const getMyBadges = async (): Promise<Badge[]> => {
  try {
    const response = await axiosInstance.get('/api/v1/badges/my');
    return response.data;
  } catch (error) {
    console.error('Error fetching my badges:', error);
    throw error;
  }
};

// Lesson-related API endpoints for user side
export const getLesson = async (id: number): Promise<Lesson> => {
  try {
    const response = await axiosInstance.get(`/api/v1/lessons/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching lesson ${id}:`, error);
    throw error;
  }
};

export const startLesson = async (id: number): Promise<any> => {
  try {
    const response = await axiosInstance.post(`/api/v1/lessons/${id}/start`);
    return response.data;
  } catch (error) {
    console.error(`Error starting lesson ${id}:`, error);
    throw error;
  }
};

export const finishLesson = async (id: number): Promise<any> => {
  try {
    const response = await axiosInstance.post(`/api/v1/lessons/${id}/finish`);
    return response.data;
  } catch (error) {
    console.error(`Error completing lesson ${id}:`, error);
    throw error;
  }
};

export const downloadLessonPDF = async (id: number): Promise<any> => {
  try {
    const response = await axiosInstance.get(`/api/v1/lessons/${id}/pdf`, {
      responseType: 'blob'
    });
    return response;
  } catch (error) {
    console.error(`Error downloading lesson PDF ${id}:`, error);
    throw error;
  }
};

// Quiz-related API endpoints for user side
export const getQuizDetails = async (id: number): Promise<any> => {
  try {
    // This should be a GET request to fetch quiz details without starting the quiz
    const response = await axiosInstance.get(`/api/v1/quizzes/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching quiz ${id}:`, error);
    throw error;
  }
};

export const startQuiz = async (id: number): Promise<any> => {
  try {
    // This is a POST request to start the quiz
    const response = await axiosInstance.post(`/api/v1/quizzes/${id}/start`);
    return response.data;
  } catch (error) {
    console.error(`Error starting quiz ${id}:`, error);
    throw error;
  }
};

export const submitQuiz = async (id: number, score: number): Promise<any> => {
  try {
    const response = await axiosInstance.post(`/api/v1/quizzes/${id}/submit`, { score });
    return response.data;
  } catch (error) {
    console.error(`Error submitting quiz ${id}:`, error);
    throw error;
  }
};

// Feedback functions
export const submitFeedback = async (data: {
  chapterId: number;
  rating: number;
  feedbackText: string;
  feedbackKeyword: string;
}): Promise<any> => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('Not authenticated');
    }

    const response = await axiosInstance.post('/api/v1/feedbacks/submit', data);
    return response.data;
  } catch (error: any) {
    if (error?.response?.data?.message === 'You have already submitted feedback for this chapter') {
      throw new Error('You have already submitted feedback for this chapter');
    }
    console.error('Error submitting feedback:', error);
    throw error;
  }
};

// Export all functions
export const contentService = {
  getChapters,
  getChapterDetails,
  getLesson,
  startLesson,
  finishLesson,
  startQuiz,
  submitQuiz,
  getMyBadges,
  submitFeedback
};

export default contentService;
