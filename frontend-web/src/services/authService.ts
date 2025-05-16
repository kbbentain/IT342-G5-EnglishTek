import axiosInstance from './axiosConfig';

export interface LoginResponse {
  username: string;
  email: string;
  name: string;
  role: string;
  token: string;
  avatarUrl?: string;
  bio?: string;
  totalBadges: number;
  totalCompletedTasks: number;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  name: string;
  avatar?: File;
}

export const login = async (username: string, password: string): Promise<LoginResponse> => {
  try {
    const response = await axiosInstance.post('/api/v1/auth/login', {
      username,
      password,
    });
    
    const data = response.data;
    
    // Store the JWT token
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    
    return data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data?.message || 'Login failed');
    }
    throw error;
  }
};

export const register = async (userData: RegisterRequest): Promise<LoginResponse> => {
  try {
    const formData = new FormData();
    formData.append('username', userData.username);
    formData.append('email', userData.email);
    formData.append('password', userData.password);
    formData.append('name', userData.name);
    
    if (userData.avatar) {
      formData.append('avatar', userData.avatar);
    }
    
    const response = await axiosInstance.post('/api/v1/auth/register', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    const data = response.data;
    
    // Store the JWT token
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    
    return data;
  } catch (error: any) {
    if (error.response) {
      throw new Error(error.response.data?.message || 'Registration failed');
    }
    throw error;
  }
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

export const getToken = () => {
  return localStorage.getItem('token');
};

export const isAuthenticated = () => {
  const token = getToken();
  const user = localStorage.getItem('user');
  if (!token || !user) return false;
  
  try {
    // Just check if we have a valid user object
    JSON.parse(user);
    return true;
  } catch {
    return false;
  }
};

export const getUserRole = (): string => {
  const user = localStorage.getItem('user');
  if (!user) return '';
  
  try {
    const userData = JSON.parse(user);
    return userData.role || '';
  } catch {
    return '';
  }
};

export const isAdmin = (): boolean => {
  return getUserRole() === 'ADMIN';
};
