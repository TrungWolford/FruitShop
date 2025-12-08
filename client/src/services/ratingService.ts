import axios from 'axios';
import { CONFIG, API } from '../config/constants';
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
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_ALL_RATINGS}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Get ratings by product ID
    getRatingsByProduct: async (productId: string, page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATINGS_BY_PRODUCT(productId)}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Get average rating by product ID
    getAverageRatingByProduct: async (productId: string): Promise<number> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_AVERAGE_RATING_BY_PRODUCT(productId)}`);
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Get ratings by account ID
    getRatingsByAccount: async (accountId: string, page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATINGS_BY_ACCOUNT(accountId)}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Get rating by account and product
    getRatingByAccountAndProduct: async (accountId: string, productId: string): Promise<Rating[]> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATING_BY_ACCOUNT_AND_PRODUCT(accountId, productId)}`);
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Create a new rating
    createRating: async (data: CreateRatingRequest): Promise<Rating> => {
        try {
            const response = await axios.post(`${CONFIG.API_GATEWAY}${API.CREATE_RATING}`, data);
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Update a rating
    updateRating: async (ratingId: string, data: UpdateRatingRequest): Promise<Rating> => {
        try {
            const response = await axios.put(`${CONFIG.API_GATEWAY}${API.UPDATE_RATING(ratingId)}`, data);
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Delete a rating (soft delete by changing status)
    deleteRating: async (ratingId: string): Promise<Rating> => {
        try {
            const response = await axios.delete(`${CONFIG.API_GATEWAY}${API.DELETE_RATING(ratingId)}`);
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    // Change rating status (toggle between active/inactive)
    changeRatingStatus: async (ratingId: string): Promise<Rating> => {
        try {
            const response = await axios.patch(`${CONFIG.API_GATEWAY}${API.CHANGE_RATING_STATUS(ratingId)}`);
            return response.data;
        } catch (error: any) {
            throw error;
        }
    }
};
