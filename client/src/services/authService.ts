import axiosInstance from '../libs/axios';
import type { AxiosResponse } from 'axios';
import { API } from '../config/constants';
import type { Account } from '../types/account';

export interface LoginCredentials {
  email: string
  password: string
}

export interface LoginResponse {
  success: boolean
  user?: Account
  message?: string
}

// Real API login
export const authService = {
  login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
    try {
      const response: AxiosResponse<Account> = await axiosInstance.post(API.ACCOUNT_LOGIN, {
        accountPhone: credentials.email, // Using email field as phone
        password: credentials.password
      });
      
      // Save user data to localStorage
      if (response.data) {
        localStorage.setItem('user', JSON.stringify(response.data))
        localStorage.setItem('isAuthenticated', 'true')
      }
      
      return {
        success: true,
        user: response.data
      };
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Đăng nhập thất bại';
      
      return {
        success: false,
        message: errorMessage
      };
    }
  },
  
  // Load user from localStorage
  loadUserFromStorage: (): Account | null => {
    try {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        return JSON.parse(userStr) as Account
      }
    } catch (error) {
      // Silent error handling
    }
    return null
  },
  
  // Logout and clear localStorage
  logout: (): void => {
    localStorage.removeItem('user')
    localStorage.removeItem('isAuthenticated')
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
  },
  
  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    return localStorage.getItem('isAuthenticated') === 'true'
  }
}
