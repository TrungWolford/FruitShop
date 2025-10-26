import axiosInstance from '../libs/axios';
import { API } from '../config/constants';

// ============================================
// SHIPPING TYPES
// ============================================

export interface CreateShippingRequest {
  accountId: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
  shippingFee?: number;
}

export interface UpdateShippingRequest {
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
  shippingFee?: number;
}

export interface ShippingResponse {
  shippingId: string;
  orderId?: string;
  accountId: string;
  accountName?: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
  shippingFee?: number;
  shippedAt?: string;
  status: number; // 0: Chờ xác nhận, 1: Đang vận chuyển, 2: Đã giao hàng
}

// ============================================
// SHIPPING SERVICE
// ============================================

export const shippingService = {
  // Get shipping addresses by account ID
  getShippingByAccount: async (accountId: string): Promise<{ success: boolean; data?: ShippingResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.GET_SHIPPING_BY_ACCOUNT(accountId));
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error getting shipping addresses:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải địa chỉ giao hàng'
      };
    }
  },

  // Create new shipping address
  createShipping: async (request: CreateShippingRequest): Promise<{ success: boolean; data?: ShippingResponse; message?: string }> => {
    try {
      const response = await axiosInstance.post(API.CREATE_SHIPPING, request);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error creating shipping address:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tạo địa chỉ giao hàng'
      };
    }
  },

  // Update shipping address
  updateShipping: async (shippingId: string, request: UpdateShippingRequest): Promise<{ success: boolean; data?: ShippingResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.UPDATE_SHIPPING(shippingId), request);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error updating shipping address:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể cập nhật địa chỉ giao hàng'
      };
    }
  },

  // Delete shipping address
  deleteShipping: async (shippingId: string): Promise<{ success: boolean; message?: string }> => {
    try {
      await axiosInstance.delete(API.DELETE_SHIPPING(shippingId));
      return {
        success: true
      };
    } catch (error: any) {
      console.error('Error deleting shipping address:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể xóa địa chỉ giao hàng'
      };
    }
  }
};

export default shippingService;
