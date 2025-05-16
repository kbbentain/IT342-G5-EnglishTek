import axiosInstance from './axiosConfig';
import { isAxiosError } from 'axios';

export interface User {
  id: number;
  username: string;
  email: string;
  name: string | null;
  role: string;
  bio: string | null;
  avatarUrl: string | null;
  createdAt: string;
  totalBadges: number;
  completedChapters: number;
  completedLessons: number;
  completedQuizzes: number;
}

export interface UpdateUserRequest {
  username: string;
  name: string;
  email: string;
  bio: string;
  passwordUpdateRequested: boolean;
}

export interface ChangePasswordRequest {
  existingPassword: string;
  newPassword: string;
}

export const userService = {
  // Get all users (admin only)
  getAllUsers: async (): Promise<User[]> => {
    const response = await axiosInstance.get('/api/v1/users/all');
    return response.data;
  },

  // Get user by ID (admin only)
  getUserById: async (id: number): Promise<User> => {
    const response = await axiosInstance.get(`/api/v1/users/${id}`);
    return response.data;
  },

  // Create new user with multipart/form-data
  async createUser(formData: FormData, endpoint: string): Promise<User> {
    try {
      const response = await axiosInstance.post(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      if (isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Failed to create user');
      }
      throw error;
    }
  },

  // Update user (admin only)
  async updateUser(id: number, data: UpdateUserRequest, avatar?: File): Promise<User> {
    try {
      const formData = new FormData();
      
      // Add all fields to formData
      formData.append('username', data.username);
      formData.append('name', data.name);
      formData.append('email', data.email);
      formData.append('bio', data.bio);
      formData.append('passwordUpdateRequested', 'false');
      
      // Add avatar if provided
      if (avatar) {
        formData.append('avatar', avatar);
      }

      const response = await axiosInstance.put(`/api/v1/users/${id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      if (isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Failed to update user');
      }
      throw error;
    }
  },

  // Change password (admin only)
  async changePassword(id: number, newPassword: string): Promise<void> {
    try {
      await axiosInstance.put(`/api/v1/admin/change-password/${id}`, {
        newPassword
      });
    } catch (error) {
      if (isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Failed to change password');
      }
      throw error;
    }
  },

  // Delete user (admin only)
  deleteUser: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/api/v1/users/${id}`);
  },

  // Get current user details
  getCurrentUser: async (): Promise<User> => {
    const response = await axiosInstance.get('/api/v1/users/me');
    return response.data;
  },
};
