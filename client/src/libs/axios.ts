import axios from "axios";
import type { AxiosResponse, AxiosError, InternalAxiosRequestConfig } from "axios";
import { store } from "../store";
import { logout } from "../store/slices/authSlice";
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
    
    // Debug API calls nếu được bật
    if (import.meta.env.VITE_DEBUG_API_CALLS === 'true') {
      console.log('🔄 API Request:', config.method?.toUpperCase(), config.url, config.data || config.params);
    }
    
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    // Debug API responses nếu được bật
    if (import.meta.env.VITE_DEBUG_API_CALLS === 'true') {
      console.log('✅ API Response:', response.status, response.config?.url, response.data);
    }
    return response;
  },
  async (error: AxiosError) => {
    // Debug API errors nếu được bật
    if (import.meta.env.VITE_DEBUG_API_CALLS === 'true') {
      console.error('❌ API Error:', error.response?.status, error.config?.url, error.response?.data);
    }
    
    const originalRequest = error.config as CustomAxiosRequestConfig;

    // If the error is 401 and we haven't tried to refresh the token yet
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Dispatch logout action to clear auth state
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
