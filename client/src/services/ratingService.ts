import axiosInstance from '../libs/axios';

import { API } from '../config/constants';
import type { Rating, CreateRatingRequest, UpdateRatingRequest } from '../types/rating';

// Backend trả về Page<T> từ Spring Data
interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}

export const ratingService = {
  // Get all ratings with pagination
  getAllRatings: async (page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
    const response = await axiosInstance.get(API.GET_ALL_RATINGS, {
      params: { page, size }
    });

    return response.data;
  },

  // Get ratings by product ID
  getRatingsByProduct: async (productId: string, page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
    const response = await axiosInstance.get(API.GET_RATINGS_BY_PRODUCT(productId), {
      params: { page, size }
    });

    return response.data;
  },

  // Get average rating by product ID
  getAverageRatingByProduct: async (productId: string): Promise<number> => {
    const response = await axiosInstance.get(API.GET_AVERAGE_RATING_BY_PRODUCT(productId));

    return response.data;
  },

  // Get ratings by account ID
  getRatingsByAccount: async (accountId: string, page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
    const response = await axiosInstance.get(API.GET_RATINGS_BY_ACCOUNT(accountId), {
      params: { page, size }
    });

    return response.data;
  },

  // Get rating by account and product
  getRatingByAccountAndProduct: async (accountId: string, productId: string): Promise<Rating[]> => {
    const response = await axiosInstance.get(API.GET_RATING_BY_ACCOUNT_AND_PRODUCT(accountId, productId));

    return response.data;
  },

  // Create a new rating
  createRating: async (data: CreateRatingRequest): Promise<Rating> => {
    const response = await axiosInstance.post(API.CREATE_RATING, data);

    return response.data;
  },

  // Update a rating
  updateRating: async (ratingId: string, data: UpdateRatingRequest): Promise<Rating> => {
    const response = await axiosInstance.put(API.UPDATE_RATING(ratingId), data);

    return response.data;
  },

  // Delete a rating (soft delete by changing status)
  deleteRating: async (ratingId: string): Promise<Rating> => {
    const response = await axiosInstance.delete(API.DELETE_RATING(ratingId));

    return response.data;
  },

  // Change rating status (toggle between active/inactive)
  changeRatingStatus: async (ratingId: string): Promise<Rating> => {
    const response = await axiosInstance.patch(API.CHANGE_RATING_STATUS(ratingId));
    return response.data;
  }
};
