import axiosInstance from './axiosConfig';

export interface FeedbackResponse {
  id: number;
  userId: number;
  username: string;
  chapterId: number;
  chapterTitle: string;
  rating: number;
  feedbackText: string;
  feedbackKeyword: string;
  createdAt: string;
  updatedAt: string;
}

// Feedback Service
export const feedbackService = {
  getFeedback: async (id: number): Promise<FeedbackResponse> => {
    const response = await axiosInstance.get(`/api/v1/feedbacks/${id}`);
    return response.data;
  },

  getFeedbacksByChapter: async (chapterId: number): Promise<FeedbackResponse[]> => {
    const response = await axiosInstance.get(`/api/v1/feedbacks/get-feedbacks/${chapterId}`);
    return response.data;
  },

  deleteFeedback: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/api/v1/feedbacks/${id}`);
  },
};
