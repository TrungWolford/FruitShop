import axiosInstance from '../libs/axios';

/**
 * MoMo Payment Service
 * Handles all MoMo payment related API calls
 */

export interface CreateMomoPaymentRequest {
    orderId: string;
}

export interface MomoPaymentData {
    partnerCode: string;
    orderId: string;
    requestId: string;
    amount: number;
    responseTime: number;
    message: string;
    resultCode: number;
    payUrl: string;
    qrCodeUrl: string;
    deeplink: string;
    deeplinkMiniApp: string;
    signature: string;
}

export interface MomoPaymentResponse {
    success: boolean;
    message: string;
    data?: MomoPaymentData;
    resultCode?: number;
}

export interface PaymentStatusResponse {
    success: boolean;
    orderId: string;
    paymentStatus: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'UNKNOWN';
    orderStatus: number;
    totalAmount: number;
}

export const momoService = {
    /**
     * Create MoMo QR payment for an order
     * @param orderId - The order ID to create payment for
     * @returns MomoPaymentResponse with QR code and payment URL
     */
    createPayment: async (orderId: string): Promise<MomoPaymentResponse> => {
        try {
            const response = await axiosInstance.post<MomoPaymentResponse>(
                '/momo/create-payment',
                { orderId }
            );
            return response.data;
        } catch (error: any) {
            // Return error response
            return {
                success: false,
                message: error.response?.data?.message || 'Không thể tạo thanh toán MoMo',
                resultCode: error.response?.data?.resultCode || -1
            };
        }
    },

    /**
     * Check payment status for an order
     * @param orderId - The order ID to check status
     * @returns PaymentStatusResponse with current payment status
     */
    checkPaymentStatus: async (orderId: string): Promise<PaymentStatusResponse> => {
        try {
            const response = await axiosInstance.get<PaymentStatusResponse>(
                `/momo/check-status/${orderId}`
            );
            return response.data;
        } catch (error: any) {
            throw error;
        }
    },

    /**
     * Handle MoMo return callback (when user comes back from MoMo)
     * @param params - URL query parameters from MoMo
     * @returns Return callback response
     */
    handleReturn: async (params: URLSearchParams): Promise<any> => {
        try {
            const response = await axiosInstance.get('/momo/return', {
                params: Object.fromEntries(params.entries())
            });
            return response.data;
        } catch (error: any) {
            throw error;
        }
    }
};
