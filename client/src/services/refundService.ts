import axiosInstance from '../libs/axios';
import { API } from '../config/constants';
import type { OrderResponse } from './orderService';

// Types
export interface RefundResponse {
  refundId: string;
  order?: OrderResponse; // Full order object from backend
  orderId?: string; // Extracted orderId for convenience
  orderDate?: string; // Extracted from order
  accountName?: string; // Extracted from order.account
  reason: string;
  refundStatus: string; // "Chờ xác nhận", "Đã duyệt", "Từ chối", "Hoàn thành"
  requestedAt: string;
  processedAt?: string;
  refundAmount: number;
  originalPaymentId?: string;
}

export interface CreateRefundRequest {
  orderId: string;
  reason: string;
  refundAmount: number;
  originalPaymentId?: string;
}

export interface UpdateRefundStatusRequest {
  refundStatus: string;
}

export interface PaginatedRefundResponse {
  content: RefundResponse[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  size: number;
}

// Helper function to map backend response to frontend format
const mapRefundResponse = (refund: any): RefundResponse => {
  console.log('🔍 Mapping refund:', refund);
  console.log('🔍 Order data:', refund.order);
  
  return {
    ...refund,
    orderId: refund.order?.orderId || refund.orderId,
    orderDate: refund.order?.createdAt || refund.orderDate,
    accountName: refund.order?.accountName || refund.accountName,
  };
};

// Service functions
const refundService = {
  // Create new refund request
  async createRefund(request: CreateRefundRequest) {
    try {
      const response = await axiosInstance.post(`${API.REFUND}`, request);
      return {
        success: true,
        data: mapRefundResponse(response.data),
      };
    } catch (error: any) {
      console.error('Error creating refund:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tạo yêu cầu hoàn tiền',
      };
    }
  },

  // Get all refunds with pagination
  async getAllRefunds(page: number = 0, size: number = 10) {
    try {
      const response = await axiosInstance.get(`${API.REFUND}`, {
        params: { page, size },
      });
      
      // Map all refunds in the content array
      const mappedData = {
        ...response.data,
        content: response.data.content.map(mapRefundResponse),
      };
      
      return {
        success: true,
        data: mappedData as PaginatedRefundResponse,
      };
    } catch (error: any) {
      console.error('Error fetching refunds:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải danh sách hoàn tiền',
      };
    }
  },

  // Get refund by ID
  async getRefundById(refundId: string) {
    try {
      const response = await axiosInstance.get(`${API.REFUND}/${refundId}`);
      return {
        success: true,
        data: mapRefundResponse(response.data),
      };
    } catch (error: any) {
      console.error('Error fetching refund:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải thông tin hoàn tiền',
      };
    }
  },

  // Get refunds by status
  async getRefundsByStatus(status: string, page: number = 0, size: number = 10) {
    try {
      const response = await axiosInstance.get(`${API.REFUND}/status/${status}`, {
        params: { page, size },
      });
      
      const mappedData = {
        ...response.data,
        content: response.data.content.map(mapRefundResponse),
      };
      
      return {
        success: true,
        data: mappedData as PaginatedRefundResponse,
      };
    } catch (error: any) {
      console.error('Error fetching refunds by status:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải danh sách hoàn tiền theo trạng thái',
      };
    }
  },

  // Get refunds by order ID
  async getRefundsByOrderId(orderId: string) {
    try {
      const response = await axiosInstance.get(`${API.REFUND}/order/${orderId}`);
      return {
        success: true,
        data: response.data.map(mapRefundResponse),
      };
    } catch (error: any) {
      console.error('Error fetching refunds by order:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải danh sách hoàn tiền',
      };
    }
  },

  // Search refunds
  async searchRefunds(keyword: string, page: number = 0, size: number = 10) {
    try {
      const response = await axiosInstance.get(`${API.REFUND}/search`, {
        params: { keyword, page, size },
      });
      
      const mappedData = {
        ...response.data,
        content: response.data.content.map(mapRefundResponse),
      };
      
      return {
        success: true,
        data: mappedData as PaginatedRefundResponse,
      };
    } catch (error: any) {
      console.error('Error searching refunds:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tìm kiếm hoàn tiền',
      };
    }
  },

  // Get refunds by date range
  async getRefundsByDateRange(
    startDate: Date,
    endDate: Date,
    page: number = 0,
    size: number = 10
  ) {
    try {
      const response = await axiosInstance.get(`${API.REFUND}/date-range`, {
        params: {
          startDate: startDate.getTime(),
          endDate: endDate.getTime(),
          page,
          size,
        },
      });
      return {
        success: true,
        data: response.data as PaginatedRefundResponse,
      };
    } catch (error: any) {
      console.error('Error fetching refunds by date range:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải danh sách hoàn tiền',
      };
    }
  },

  // Update refund status
  async updateRefundStatus(refundId: string, request: UpdateRefundStatusRequest) {
    try {
      const response = await axiosInstance.put(`${API.REFUND}/${refundId}/status`, request);
      return {
        success: true,
        data: mapRefundResponse(response.data),
      };
    } catch (error: any) {
      console.error('Error updating refund status:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể cập nhật trạng thái hoàn tiền',
      };
    }
  },

  // Approve refund
  async approveRefund(refundId: string) {
    try {
      const response = await axiosInstance.put(`${API.REFUND}/${refundId}/approve`);
      return {
        success: true,
        data: mapRefundResponse(response.data),
      };
    } catch (error: any) {
      console.error('Error approving refund:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể duyệt yêu cầu hoàn tiền',
      };
    }
  },

  // Reject refund
  async rejectRefund(refundId: string) {
    try {
      const response = await axiosInstance.put(`${API.REFUND}/${refundId}/reject`);
      return {
        success: true,
        data: response.data as RefundResponse,
      };
    } catch (error: any) {
      console.error('Error rejecting refund:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể từ chối yêu cầu hoàn tiền',
      };
    }
  },

  // Complete refund
  async completeRefund(refundId: string) {
    try {
      const response = await axiosInstance.put(`${API.REFUND}/${refundId}/complete`);
      return {
        success: true,
        data: response.data as RefundResponse,
      };
    } catch (error: any) {
      console.error('Error completing refund:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể hoàn thành hoàn tiền',
      };
    }
  },

  // Cancel/Delete refund
  async cancelRefund(refundId: string) {
    try {
      await axiosInstance.delete(`${API.REFUND}/${refundId}`);
      return {
        success: true,
      };
    } catch (error: any) {
      console.error('Error canceling refund:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể hủy yêu cầu hoàn tiền',
      };
    }
  },

  // Get pending refunds count
  async getPendingRefundsCount() {
    try {
      const response = await axiosInstance.get(`${API.REFUND}/stats/pending-count`);
      return {
        success: true,
        data: response.data.count as number,
      };
    } catch (error: any) {
      console.error('Error fetching pending refunds count:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải số lượng hoàn tiền chờ xử lý',
      };
    }
  },
};

export default refundService;
