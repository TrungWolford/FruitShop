import axios from 'axios';
import { CONFIG, API } from '../config/constants';
import type { Rating, CreateRatingRequest, UpdateRatingRequest } from '../types/rating';

interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

interface PaginatedResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

export const ratingService = {
    // Get all ratings with pagination
    getAllRatings: async (page: number = 0, size: number = 10): Promise<ApiResponse<PaginatedResponse<Rating>>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_ALL_RATINGS}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            console.error('Error getting all ratings:', error);
            throw error;
        }
    },

    // Get ratings by product ID
    getRatingsByProduct: async (productId: string, page: number = 0, size: number = 10): Promise<ApiResponse<PaginatedResponse<Rating>>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATINGS_BY_PRODUCT(productId)}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            console.error('Error getting ratings by product:', error);
            throw error;
        }
    },

    // Get average rating by product ID
    getAverageRatingByProduct: async (productId: string): Promise<ApiResponse<number>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_AVERAGE_RATING_BY_PRODUCT(productId)}`);
            return response.data;
        } catch (error: any) {
            console.error('Error getting average rating by product:', error);
            throw error;
        }
    },

    // Get ratings by account ID
    getRatingsByAccount: async (accountId: string, page: number = 0, size: number = 10): Promise<ApiResponse<PaginatedResponse<Rating>>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATINGS_BY_ACCOUNT(accountId)}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            console.error('Error getting ratings by account:', error);
            throw error;
        }
    },

    // Get rating by account and product
    getRatingByAccountAndProduct: async (accountId: string, productId: string): Promise<ApiResponse<Rating>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATING_BY_ACCOUNT_AND_PRODUCT(accountId, productId)}`);
            return response.data;
        } catch (error: any) {
            console.error('Error getting rating by account and product:', error);
            throw error;
        }
    },

    // Create a new rating
    createRating: async (data: CreateRatingRequest): Promise<ApiResponse<Rating>> => {
        try {
            const response = await axios.post(`${CONFIG.API_GATEWAY}${API.CREATE_RATING}`, data);
            return response.data;
        } catch (error: any) {
            console.error('Error creating rating:', error);
            throw error;
        }
    },

    // Update a rating
    updateRating: async (ratingId: string, data: UpdateRatingRequest): Promise<ApiResponse<Rating>> => {
        try {
            const response = await axios.put(`${CONFIG.API_GATEWAY}${API.UPDATE_RATING(ratingId)}`, data);
            return response.data;
        } catch (error: any) {
            console.error('Error updating rating:', error);
            throw error;
        }
    },

    // Delete a rating
    deleteRating: async (ratingId: string): Promise<ApiResponse<void>> => {
        try {
            const response = await axios.delete(`${CONFIG.API_GATEWAY}${API.DELETE_RATING(ratingId)}`);
            return response.data;
        } catch (error: any) {
            console.error('Error deleting rating:', error);
            throw error;
        }
    },

    // Change rating status (toggle between active/inactive)
    changeRatingStatus: async (ratingId: string): Promise<ApiResponse<Rating>> => {
        try {
            const response = await axios.patch(`${CONFIG.API_GATEWAY}${API.CHANGE_RATING_STATUS(ratingId)}`);
            return response.data;
        } catch (error: any) {
            console.error('Error changing rating status:', error);
            throw error;
        }
    }
};
