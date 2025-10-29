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
  shipperName?: string;
  shippingFee?: number;
}

export interface UpdateShippingRequest {
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
  shipperName?: string;
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
  shipperName?: string;
  shippingFee?: number;
  shippedAt?: string;
  status: number; // 0: Đã hủy, 1: Đang chuẩn bị, 2: Đang giao, 3: Giao thành công
}

export interface ShippingPageResponse {
  content: ShippingResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
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
  },

  // Get all shippings (admin)
  getAllShippings: async (page: number = 0, size: number = 10): Promise<{ success: boolean; data?: ShippingResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.GET_ALL_SHIPPING, {
        params: { page, size }
      });

      let shippings: ShippingResponse[] = [];

      if (response.data && response.data.content) {
        // Spring Page format
        shippings = response.data.content;
      } else if (Array.isArray(response.data)) {
        // Direct array format
        shippings = response.data;
      }

      return {
        success: true,
        data: shippings
      };
    } catch (error: any) {
      console.error('Error getting all shippings:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải danh sách vận chuyển'
      };
    }
  },

  // Get shipping by ID
  getShippingById: async (shippingId: string): Promise<{ success: boolean; data?: ShippingResponse; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.GET_SHIPPING_BY_ID(shippingId));
      return {
        success: true,
        data: response.data
      };
    } catch (error: any) {
      console.error('Error getting shipping:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải thông tin vận chuyển'
      };
    }
  },

  // Update shipping status
  updateShippingStatus: async (shippingId: string, status: number): Promise<{ success: boolean; data?: ShippingResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.UPDATE_SHIPPING_STATUS(shippingId), null, {
        params: { status }
      });
      return {
        success: true,
        data: response.data,
        message: 'Cập nhật trạng thái thành công'
      };
    } catch (error: any) {
      console.error('Error updating shipping status:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể cập nhật trạng thái'
      };
    }
  },

  // Search shippings by keyword
  searchShippings: async (keyword: string, page: number = 0, size: number = 10): Promise<{ success: boolean; data?: ShippingResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.SEARCH_SHIPPINGS, {
        params: { keyword, page, size }
      });

      let shippings: ShippingResponse[] = [];

      if (response.data && response.data.content) {
        shippings = response.data.content;
      } else if (Array.isArray(response.data)) {
        shippings = response.data;
      }

      return {
        success: true,
        data: shippings
      };
    } catch (error: any) {
      console.error('Error searching shippings:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tìm kiếm vận chuyển'
      };
    }
  },

  // Filter shippings by status
  filterShippingsByStatus: async (status: number, page: number = 0, size: number = 10): Promise<{ success: boolean; data?: ShippingResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.FILTER_SHIPPINGS, {
        params: { status, page, size }
      });

      let shippings: ShippingResponse[] = [];

      if (response.data && response.data.content) {
        shippings = response.data.content;
      } else if (Array.isArray(response.data)) {
        shippings = response.data;
      }

      return {
        success: true,
        data: shippings
      };
    } catch (error: any) {
      console.error('Error filtering shippings:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể lọc vận chuyển'
      };
    }
  },

  // Search and filter shippings
  searchAndFilterShippings: async (
    keyword: string | null,
    status: number | null,
    page: number = 0,
    size: number = 10
  ): Promise<{ success: boolean; data?: ShippingResponse[]; totalElements?: number; totalPages?: number; message?: string }> => {
    try {
      const params: any = { page, size };
      if (keyword) params.keyword = keyword;
      if (status !== null) params.status = status;

      const response = await axiosInstance.get(API.SEARCH_AND_FILTER_SHIPPINGS, { params });

      let shippings: ShippingResponse[] = [];
      let totalElements = 0;
      let totalPages = 0;

      if (response.data && response.data.content) {
        // Spring Page format
        shippings = response.data.content;
        totalElements = response.data.totalElements || 0;
        totalPages = response.data.totalPages || 0;
      } else if (Array.isArray(response.data)) {
        // Direct array format
        shippings = response.data;
        totalElements = shippings.length;
        totalPages = Math.ceil(totalElements / size);
      }

      return {
        success: true,
        data: shippings,
        totalElements,
        totalPages
      };
    } catch (error: any) {
      console.error('Error searching and filtering shippings:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tìm kiếm và lọc vận chuyển'
      };
    }
  }
};

export default shippingService;
