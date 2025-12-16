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
      // Parse error message from backend
      let errorMessage = 'Số điện thoại hoặc mật khẩu không đúng';
      
      // Ưu tiên lấy message từ backend response
      if (typeof error.response?.data === 'string' && error.response.data.trim() !== '') {
        errorMessage = error.response.data;
      } 
      // Nếu backend trả về object với message property
      else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }
      // Chỉ dùng default message nếu không có message từ backend
      else if (error.response?.status === 401) {
        errorMessage = 'Số điện thoại hoặc mật khẩu không đúng';
      } else if (error.response?.status === 400) {
        errorMessage = 'Số điện thoại hoặc mật khẩu không đúng';
      } else if (error.response?.status === 403) {
        errorMessage = 'Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.';
      } else if (error.response?.status >= 500) {
        errorMessage = 'Lỗi hệ thống. Vui lòng thử lại sau';
      }
      
      // Throw error để component có thể catch và xử lý
      throw {
        response: error.response,
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
