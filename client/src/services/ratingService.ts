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
            console.log('📦 getAllRatings response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error getting all ratings:', error);
            throw error;
        }
    },

    // Get ratings by product ID
    getRatingsByProduct: async (productId: string, page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATINGS_BY_PRODUCT(productId)}`, {
                params: { page, size }
            });
            console.log('📦 getRatingsByProduct response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error getting ratings by product:', error);
            throw error;
        }
    },

    // Get average rating by product ID
    getAverageRatingByProduct: async (productId: string): Promise<number> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_AVERAGE_RATING_BY_PRODUCT(productId)}`);
            console.log('📦 getAverageRatingByProduct response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error getting average rating by product:', error);
            throw error;
        }
    },

    // Get ratings by account ID
    getRatingsByAccount: async (accountId: string, page: number = 0, size: number = 10): Promise<PageResponse<Rating>> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATINGS_BY_ACCOUNT(accountId)}`, {
                params: { page, size }
            });
            console.log('📦 getRatingsByAccount response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error getting ratings by account:', error);
            throw error;
        }
    },

    // Get rating by account and product
    getRatingByAccountAndProduct: async (accountId: string, productId: string): Promise<Rating> => {
        try {
            const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_RATING_BY_ACCOUNT_AND_PRODUCT(accountId, productId)}`);
            console.log('📦 getRatingByAccountAndProduct response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error getting rating by account and product:', error);
            throw error;
        }
    },

    // Create a new rating
    createRating: async (data: CreateRatingRequest): Promise<Rating> => {
        try {
            const response = await axios.post(`${CONFIG.API_GATEWAY}${API.CREATE_RATING}`, data);
            console.log('📦 createRating response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error creating rating:', error);
            throw error;
        }
    },

    // Update a rating
    updateRating: async (ratingId: string, data: UpdateRatingRequest): Promise<Rating> => {
        try {
            const response = await axios.put(`${CONFIG.API_GATEWAY}${API.UPDATE_RATING(ratingId)}`, data);
            console.log('📦 updateRating response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error updating rating:', error);
            throw error;
        }
    },

    // Delete a rating (soft delete by changing status)
    deleteRating: async (ratingId: string): Promise<Rating> => {
        try {
            const response = await axios.delete(`${CONFIG.API_GATEWAY}${API.DELETE_RATING(ratingId)}`);
            console.log('📦 deleteRating response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error deleting rating:', error);
            throw error;
        }
    },

    // Change rating status (toggle between active/inactive)
    changeRatingStatus: async (ratingId: string): Promise<Rating> => {
        try {
            const response = await axios.patch(`${CONFIG.API_GATEWAY}${API.CHANGE_RATING_STATUS(ratingId)}`);
            console.log('📦 changeRatingStatus response:', response.data);
            return response.data;
        } catch (error: any) {
            console.error('❌ Error changing rating status:', error);
            throw error;
        }
    }
};
