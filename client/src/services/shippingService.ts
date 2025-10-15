import axiosInstance from '../libs/axios';
import { API } from '../config/constants';

export interface CreateShippingRequest {
  accountId: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
}

export interface UpdateShippingRequest {
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
}

export interface ShippingResponse {
  shippingId: string;
  accountId: string;
  accountName?: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
}

export interface CreateOrderRequest {
  accountId: string;
  shippingId: string;
  paymentMethod: number; // 0: Tiền mặt, 1: Chuyển khoản
  items: Array<{
    productId: string;
    quantity: number;
  }>;
}

export interface OrderResponse {
  orderId: string;
  accountId: string;
  accountName: string;
  orderDate: string;
  status: number; // 0: Huy, 1: Dang van chuyen, 2: Da hoan thanh
  paymentMethod: number; // 0: Tien mat, 1: Ck
  totalAmount: number;
  orderDetails: OrderDetailResponse[];
  totalItems: number;
  shipping: ShippingResponse;
}

export interface OrderDetailResponse {
  orderDetailId: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  productImages: string[];
}

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

export const orderService = {
  // Create new order
  createOrder: async (request: CreateOrderRequest): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.post(API.CREATE_ORDER, request);
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error creating order:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      console.error('Request data:', request);
      
      return {
        success: false,
        message: error.response?.data?.message || error.response?.data || 'Không thể tạo đơn hàng'
      };
    }
  },

  // Get orders by account ID
  getOrdersByAccount: async (accountId: string): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.GET_ORDERS_BY_ACCOUNT(accountId));
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error getting orders:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải đơn hàng'
      };
    }
  },

  // Get order by ID
  getOrderById: async (orderId: string): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.GET_ORDER_BY_ID(orderId));
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error getting order:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải đơn hàng'
      };
    }
  },

  // Cancel order
  cancelOrder: async (orderId: string): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.CANCEL_ORDER(orderId));
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error canceling order:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể hủy đơn hàng'
      };
    }
  }
};