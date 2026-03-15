import axios from "axios";
import type { AxiosResponse, AxiosError, InternalAxiosRequestConfig } from "axios";
import { store } from "../store";
import { logout } from "../store/slices/authSlice";
import { adminLogout } from "../store/slices/adminAuthSlice";
import { CONFIG, STORAGE_KEYS } from "../config/constants";

interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const axiosInstance = axios.create({
  baseURL: CONFIG.API_GATEWAY,
  timeout: 120000, // 2 phút = 120.000 ms
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error: AxiosError) => {
    
    const originalRequest = error.config as CustomAxiosRequestConfig;

    // Kiểm tra nếu là request login thì không xử lý 401 đặc biệt
    const isLoginRequest = originalRequest?.url?.includes('/login') || 
                           originalRequest?.url?.includes('/auth');

    // If the error is 401 and we haven't tried to refresh the token yet
    // KHÔNG xử lý logout/redirect cho login request
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isLoginRequest) {
      originalRequest._retry = true;

      try {
        const isAdminSession =
          localStorage.getItem('admin_isAuthenticated') === 'true' ||
          Boolean(localStorage.getItem('admin_user'));

        if (isAdminSession) {
          store.dispatch(adminLogout());
          window.location.href = '/admin/login';
          return Promise.reject(error);
        }

        // Dispatch customer logout action to clear auth state
        store.dispatch(logout());
        localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER_PROFILE);
        window.location.href = '/';
        return Promise.reject(error);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
