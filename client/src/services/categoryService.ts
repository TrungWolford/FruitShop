import axiosInstance from '../libs/axios';
import type { AxiosResponse } from 'axios';
import { API } from '../config/constants';

export interface Category {
  categoryId: string;
  categoryName: string;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryResponse {
  content: Category[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Category Service
export const categoryService = {
  // Get all categories with pagination
  getAllCategories: async (page: number = 0, size: number = 100): Promise<CategoryResponse> => {
    try {
      const response: AxiosResponse<CategoryResponse> = await axiosInstance.get(
        `${API.GET_ALL_CATEGORIES}?page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Get category by ID
  getCategoryById: async (categoryId: string): Promise<Category> => {
    try {
      const response: AxiosResponse<Category> = await axiosInstance.get(API.GET_CATEGORY_BY_ID(categoryId));
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Create new category
  createCategory: async (categoryData: Omit<Category, 'categoryId'>): Promise<Category> => {
    try {
      const response: AxiosResponse<Category> = await axiosInstance.post(API.CREATE_CATEGORY, categoryData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Update category
  updateCategory: async (categoryId: string, categoryData: Partial<Category>): Promise<Category> => {
    try {
      const response: AxiosResponse<Category> = await axiosInstance.put(API.UPDATE_CATEGORY(categoryId), categoryData);
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  // Delete category
  deleteCategory: async (categoryId: string): Promise<void> => {
    try {
      await axiosInstance.delete(API.DELETE_CATEGORY(categoryId));
    } catch (error) {
      throw error;
    }
  },

  // Search categories
  searchCategories: async (keyword: string, page: number = 0, size: number = 10): Promise<CategoryResponse> => {
    try {
      const response: AxiosResponse<CategoryResponse> = await axiosInstance.get(
        `${API.SEARCH_CATEGORIES}?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      throw error;
    }
  },
};
