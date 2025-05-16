import axios from 'axios';
import { config } from '../config/env';

export interface DashboardData {
  totalUsers: number;
  totalChapters: number;
  totalLessons: number;
  totalQuizzes: number;
  totalBadgesGiven: number;
  topScorers: {
    userId: number;
    username: string;
    avatarUrl: string | null;
    totalScore: number;
  }[];
  activityByDay: Record<string, number>;
}

export const dashboardService = {
  getDashboardData: async (): Promise<DashboardData> => {
    const response = await axios.get<DashboardData>(`${config.apiUrl}/api/v1/admin/dashboard`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
    });
    return response.data;
  },
};
