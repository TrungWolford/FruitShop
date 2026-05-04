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
    const response: AxiosResponse<CategoryResponse | Category[]> = await axiosInstance.get(
      `${API.GET_ALL_CATEGORIES}?page=${page}&size=${size}`
    );
    const data = response.data;
    if (Array.isArray(data)) {
      const total = data.length;
      return {
        content: data,
        totalElements: total,
        totalPages: total > 0 ? Math.ceil(total / size) : 1,
        size,
        number: page,
        first: page === 0,
        last: page >= Math.max(Math.ceil(total / size) - 1, 0)
      };
    }
    if (data && Array.isArray(data.content)) {
      return data;
    }
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size,
      number: page,
      first: true,
      last: true
    };
  },

  // Get category by ID
  getCategoryById: async (categoryId: string): Promise<Category> => {
    const response: AxiosResponse<Category> = await axiosInstance.get(API.GET_CATEGORY_BY_ID(categoryId));
    return response.data;
  },

  // Create new category
  createCategory: async (categoryData: Omit<Category, 'categoryId'>): Promise<Category> => {
    const response: AxiosResponse<Category> = await axiosInstance.post(API.CREATE_CATEGORY, categoryData);
    return response.data;
  },

  // Update category
  updateCategory: async (categoryId: string, categoryData: Partial<Category>): Promise<Category> => {
    const response: AxiosResponse<Category> = await axiosInstance.put(API.UPDATE_CATEGORY(categoryId), categoryData);
    return response.data;
  },

  // Delete category
  deleteCategory: async (categoryId: string): Promise<void> => {
    await axiosInstance.delete(API.DELETE_CATEGORY(categoryId));
  },

  // Search categories
  searchCategories: async (keyword: string, page: number = 0, size: number = 10): Promise<CategoryResponse> => {
    const response: AxiosResponse<CategoryResponse | Category[]> = await axiosInstance.get(
      `${API.SEARCH_CATEGORIES}?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`
    );
    const data = response.data;
    if (Array.isArray(data)) {
      const total = data.length;
      return {
        content: data,
        totalElements: total,
        totalPages: total > 0 ? Math.ceil(total / size) : 1,
        size,
        number: page,
        first: page === 0,
        last: page >= Math.max(Math.ceil(total / size) - 1, 0)
      };
    }
    if (data && Array.isArray(data.content)) {
      return data;
    }
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size,
      number: page,
      first: true,
      last: true
    };
  },
};
