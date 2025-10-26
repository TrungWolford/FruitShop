import axiosInstance from '../libs/axios';
import { API } from '../config/constants';

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
      // Backend returns OrderResponse with fields: createdAt, orderItems, payment (object)
      // Map backend shape to frontend OrderResponse expected by components
      const mapped: OrderResponse[] = (response.data || []).map((o: any) => mapBackendOrderToFrontend(o));
      return {
        success: true,
        data: mapped
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
      const mapped = response.data ? mapBackendOrderToFrontend(response.data) : undefined;
      return {
        success: true,
        data: mapped
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

// Helper: map backend Order DTO (server) to frontend OrderResponse shape used in components
export function mapBackendOrderToFrontend(o: any): OrderResponse {
  const paymentMethodFromBackend = (() => {
    // backend returns payment.paymentMethod as string like "COD" or "BANK_TRANSFER"
    const pm = o?.payment?.paymentMethod || o?.paymentMethod;
    if (!pm) return 0; // default to COD if missing
    const lower = String(pm).toLowerCase();
    if (lower.includes('cod') || lower.includes('cash')) return 0;
    if (lower.includes('bank') || lower.includes('transfer') || lower.includes('ck')) return 1;
    return 0;
  })();

  const orderDetails: OrderDetailResponse[] = (o?.orderItems || o?.orderDetails || []).map((it: any) => ({
    orderDetailId: it.orderDetailId || it.orderDetailId,
    productId: it.productId,
    productName: it.productName,
    quantity: it.quantity,
    unitPrice: it.unitPrice ?? it.unitPrice,
    totalPrice: it.totalPrice ?? (it.unitPrice ? it.unitPrice * (it.quantity || 0) : 0),
    productImages: it.productImages || []
  }));

  return {
    orderId: o.orderId,
    accountId: o.accountId,
    accountName: o.accountName,
    orderDate: (o.createdAt ? new Date(o.createdAt).toISOString() : new Date().toISOString()),
    status: o.status ?? 0,
    paymentMethod: paymentMethodFromBackend,
    totalAmount: o.totalAmount ?? 0,
    orderDetails,
    totalItems: o.totalItems ?? orderDetails.length,
    shipping: o.shipping ? {
      shippingId: o.shipping.shippingId,
      accountId: o.shipping.accountId,
      accountName: o.shipping.accountName,
      receiverName: o.shipping.receiverName,
      receiverPhone: o.shipping.receiverPhone,
      receiverAddress: o.shipping.receiverAddress,
      city: o.shipping.city,
    } : undefined as any,
  };
}