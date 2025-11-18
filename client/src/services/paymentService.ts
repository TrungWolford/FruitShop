import axiosInstance from '../libs/axios';
import { API } from '../config/constants';

// Types
export interface PaymentResponse {
    paymentId: string;
    paymentMethod: string; // COD, Bank Transfer, E-Wallet, Credit Card
    paymentStatus: number; // 0: Pending, 1: Completed, 2: Failed, 3: Refunded
    paymentDate: string;
    amount: number;
    transactionId?: string;
}

export interface CreatePaymentRequest {
    paymentMethod: string;
    paymentStatus: number;
    amount: number;
    transactionId?: string;
}

export interface UpdatePaymentRequest {
    paymentMethod?: string;
    paymentStatus?: number;
    amount?: number;
    transactionId?: string;
}

// Payment Service
export const paymentService = {
    // Get all payments with pagination
    getAllPayments: async (
        page: number = 0,
        size: number = 10,
        sortBy: string = 'paymentDate',
        sortDir: string = 'desc'
    ): Promise<{ success: boolean; data?: PaymentResponse[]; message?: string; totalElements?: number; totalPages?: number }> => {
        try {
            const response = await axiosInstance.get(API.PAYMENT, {
                params: { page, size, sortBy, sortDir }
            });

            // Handle Spring Page format
            if (response.data && response.data.content) {
                return {
                    success: true,
                    data: response.data.content,
                    totalElements: response.data.totalElements,
                    totalPages: response.data.totalPages
                };
            } else if (Array.isArray(response.data)) {
                return {
                    success: true,
                    data: response.data,
                    totalElements: response.data.length,
                    totalPages: 1
                };
            }

            return {
                success: true,
                data: [],
                totalElements: 0,
                totalPages: 0
            };
        } catch (error: any) {
            console.error('Error getting all payments:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể tải danh sách thanh toán'
            };
        }
    },

    // Get payment by ID
    getPaymentById: async (paymentId: string): Promise<{ success: boolean; data?: PaymentResponse; message?: string }> => {
        try {
            const response = await axiosInstance.get(API.GET_PAYMENT_BY_ID(paymentId));
            return {
                success: true,
                data: response.data
            };
        } catch (error: any) {
            console.error('Error getting payment:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể tải thông tin thanh toán'
            };
        }
    },

    // Create payment
    createPayment: async (request: CreatePaymentRequest): Promise<{ success: boolean; data?: PaymentResponse; message?: string }> => {
        try {
            const response = await axiosInstance.post(API.PAYMENT, request);
            return {
                success: true,
                data: response.data,
                message: 'Tạo thanh toán thành công'
            };
        } catch (error: any) {
            console.error('Error creating payment:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể tạo thanh toán'
            };
        }
    },

    // Update payment
    updatePayment: async (paymentId: string, request: UpdatePaymentRequest): Promise<{ success: boolean; data?: PaymentResponse; message?: string }> => {
        try {
            const response = await axiosInstance.put(API.UPDATE_PAYMENT(paymentId), request);
            return {
                success: true,
                data: response.data,
                message: 'Cập nhật thanh toán thành công'
            };
        } catch (error: any) {
            console.error('Error updating payment:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể cập nhật thanh toán'
            };
        }
    },

    // Get payments by status
    getPaymentsByStatus: async (
        status: number,
        page: number = 0,
        size: number = 10
    ): Promise<{ success: boolean; data?: PaymentResponse[]; message?: string; totalElements?: number; totalPages?: number }> => {
        try {
            const response = await axiosInstance.get(API.GET_PAYMENTS_BY_STATUS(status), {
                params: { page, size }
            });

            if (response.data && response.data.content) {
                return {
                    success: true,
                    data: response.data.content,
                    totalElements: response.data.totalElements,
                    totalPages: response.data.totalPages
                };
            } else if (Array.isArray(response.data)) {
                return {
                    success: true,
                    data: response.data,
                    totalElements: response.data.length,
                    totalPages: 1
                };
            }

            return {
                success: true,
                data: [],
                totalElements: 0,
                totalPages: 0
            };
        } catch (error: any) {
            console.error('Error getting payments by status:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể tải danh sách thanh toán'
            };
        }
    },

    // Update payment status
    updatePaymentStatus: async (paymentId: string, status: number): Promise<{ success: boolean; data?: PaymentResponse; message?: string }> => {
        try {
            const response = await axiosInstance.put(API.UPDATE_PAYMENT_STATUS(paymentId), null, {
                params: { status }
            });
            return {
                success: true,
                data: response.data,
                message: 'Cập nhật trạng thái thanh toán thành công'
            };
        } catch (error: any) {
            console.error('Error updating payment status:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể cập nhật trạng thái thanh toán'
            };
        }
    },

    // Get payment by transaction ID
    getPaymentByTransactionId: async (transactionId: string): Promise<{ success: boolean; data?: PaymentResponse; message?: string }> => {
        try {
            const response = await axiosInstance.get(API.GET_PAYMENT_BY_TRANSACTION_ID(transactionId));
            return {
                success: true,
                data: response.data
            };
        } catch (error: any) {
            console.error('Error getting payment by transaction ID:', error);
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể tìm thấy thanh toán'
            };
        }
    },
};

export default paymentService;
