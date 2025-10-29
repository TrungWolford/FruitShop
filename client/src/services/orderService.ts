import axiosInstance from '../libs/axios';
import { API } from '../config/constants';

// Types
export interface OrderDetailResponse {
  orderDetailId: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  productImages: string[];
}

export interface ShippingInfo {
  shippingId: string;
  accountId: string;
  accountName?: string;
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  city: string;
  shipperName?: string;
  shippingFee?: number;
  shippedAt?: string;
  status?: number; // 0: Đã hủy, 1: Đang chuẩn bị, 2: Đang giao, 3: Giao thành công
}

export interface OrderResponse {
  orderId: string;
  accountId: string;
  accountName: string;
  orderDate: string;
  status: number; // 0: Đã hủy, 1: Đang vận chuyển, 2: Đã hoàn thành
  paymentMethod: number; // 0: Tiền mặt (COD), 1: Chuyển khoản
  totalAmount: number;
  orderDetails: OrderDetailResponse[];
  totalItems?: number;
  shipping?: ShippingInfo;
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

  // Safely parse date - handle null/undefined/invalid dates
  const parseDate = (dateValue: any): string => {
    if (!dateValue) {
      return new Date().toISOString();
    }
    try {
      // Check if it's in dd/MM/yyyy HH:mm:ss format (Vietnamese format)
      const vnDatePattern = /^(\d{2})\/(\d{2})\/(\d{4})\s+(\d{2}):(\d{2}):(\d{2})$/;
      const match = String(dateValue).match(vnDatePattern);
      
      if (match) {
        // Parse Vietnamese format: dd/MM/yyyy HH:mm:ss
        const [, day, month, year, hours, minutes, seconds] = match;
        // Create date in ISO format: yyyy-MM-ddTHH:mm:ss
        const isoDate = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
        const date = new Date(isoDate);
        if (!isNaN(date.getTime())) {
          return date.toISOString();
        }
      }
      
      // Try standard date parsing
      const date = new Date(dateValue);
      if (isNaN(date.getTime())) {
        console.warn('Invalid date value:', dateValue, 'using current date');
        return new Date().toISOString();
      }
      return date.toISOString();
    } catch (error) {
      console.error('Error parsing date:', dateValue, error);
      return new Date().toISOString();
    }
  };

  const orderDetails: OrderDetailResponse[] = (o?.orderItems || o?.orderDetails || []).map((it: any) => ({
    orderDetailId: it.orderDetailId || it.orderItemId,
    productId: it.productId,
    productName: it.productName,
    quantity: it.quantity,
    unitPrice: it.unitPrice ?? it.price ?? 0,
    totalPrice: it.totalPrice ?? (it.unitPrice ? it.unitPrice * (it.quantity || 0) : 0),
    productImages: it.productImages || []
  }));

  return {
    orderId: o.orderId,
    accountId: o.accountId,
    accountName: o.accountName,
    orderDate: parseDate(o.createdAt),
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
      shipperName: o.shipping.shipperName,
      shippingFee: o.shipping.shippingFee,
      shippedAt: o.shipping.shippedAt ? parseDate(o.shipping.shippedAt) : undefined,
      status: o.shipping.status,
    } : undefined,
  };
}

// Order Service
export const orderService = {
  // Create new order
  createOrder: async (request: CreateOrderRequest): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      console.log('📦 Creating order with request:', request);
      console.log('Request items detail:', JSON.stringify(request.items, null, 2));
      
      const response = await axiosInstance.post(API.CREATE_ORDER, request);
      
      console.log('✅ Order created successfully - Raw response:', response.data);
      console.log('📅 createdAt value:', response.data?.createdAt);
      console.log('📅 createdAt type:', typeof response.data?.createdAt);
      
      const mapped = response.data ? mapBackendOrderToFrontend(response.data) : undefined;
      
      console.log('✅ Mapped order:', mapped);
      
      return {
        success: true,
        data: mapped,
        message: 'Đơn hàng đã được tạo thành công'
      };
    } catch (error: any) {
      console.error('❌ Error creating order:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      console.error('Request data:', request);
      
      return {
        success: false,
        message: error.response?.data?.message || error.response?.data || 'Không thể tạo đơn hàng'
      };
    }
  },

  // Get all orders (for admin)
  getAllOrders: async (page: number = 0, size: number = 10): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.GET_ALL_ORDERS, {
        params: { page, size }
      });
      
      // Backend returns Spring Page format with { content: OrderResponse[], totalPages, totalElements, etc. }
      let orders: OrderResponse[] = [];
      
      if (response.data && response.data.content) {
        // Spring Page format
        orders = response.data.content.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (Array.isArray(response.data)) {
        // Direct array format
        orders = response.data.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (response.data === null || response.data === undefined) {
        // No data
        orders = [];
      }
      
      return {
        success: true,
        data: orders
      };
    } catch (error: any) {
      console.error('Error getting all orders:', error);
      
      // Return empty array instead of failing when backend has issues
      if (error.response?.status === 500) {
        console.warn('⚠️ Backend error 500, returning empty array');
        return {
          success: true,
          data: [],
          message: 'Không có dữ liệu đơn hàng'
        };
      }
      
      return {
        success: false,
        data: [],
        message: error.response?.data?.message || 'Không thể tải danh sách đơn hàng'
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
        data: mapBackendOrderToFrontend(response.data)
      };
    } catch (error: any) {
      console.error('Error canceling order:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể hủy đơn hàng'
      };
    }
  },

  // Confirm order (Admin: status 1 -> 2)
  confirmOrder: async (orderId: string): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.CONFIRM_ORDER(orderId));
      return {
        success: true,
        data: mapBackendOrderToFrontend(response.data),
        message: 'Đã xác nhận đơn hàng thành công'
      };
    } catch (error: any) {
      console.error('Error confirming order:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể xác nhận đơn hàng'
      };
    }
  },

  // Start delivery (Admin: status 2 -> 3)
  startDelivery: async (orderId: string): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.START_DELIVERY(orderId));
      return {
        success: true,
        data: mapBackendOrderToFrontend(response.data),
        message: 'Đã bắt đầu giao hàng'
      };
    } catch (error: any) {
      console.error('Error starting delivery:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể bắt đầu giao hàng'
      };
    }
  },

  // Complete order (Customer/Admin: status 3 -> 4)
  completeOrder: async (orderId: string): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.COMPLETE_ORDER(orderId));
      return {
        success: true,
        data: mapBackendOrderToFrontend(response.data),
        message: 'Đã hoàn thành đơn hàng'
      };
    } catch (error: any) {
      console.error('Error completing order:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể hoàn thành đơn hàng'
      };
    }
  },

  // Update order status (admin)
  updateOrderStatus: async (orderId: string, status: number): Promise<{ success: boolean; data?: OrderResponse; message?: string }> => {
    try {
      const response = await axiosInstance.put(API.UPDATE_ORDER_STATUS(orderId), null, {
        params: { status }
      });
      return {
        success: true,
        data: response.data,
        message: 'Cập nhật trạng thái đơn hàng thành công'
      };
    } catch (error: any) {
      console.error('Error updating order status:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể cập nhật trạng thái đơn hàng'
      };
    }
  },

  // Get orders by status (admin)
  getOrdersByStatus: async (status: number, page: number = 0, size: number = 10): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.FILTER_ORDERS_BY_STATUS, {
        params: { status, page, size }
      });
      
      let orders: OrderResponse[] = [];
      
      if (response.data && response.data.content) {
        orders = response.data.content.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (Array.isArray(response.data)) {
        orders = response.data.map((o: any) => mapBackendOrderToFrontend(o));
      }
      
      return {
        success: true,
        data: orders
      };
    } catch (error: any) {
      console.error('Error getting orders by status:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải đơn hàng'
      };
    }
  },

  // Get orders by date range (admin)
  getOrdersByDateRange: async (
    startDate: string, 
    endDate: string, 
    page: number = 0, 
    size: number = 10
  ): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.FILTER_ORDERS_BY_DATE, {
        params: { startDate, endDate, page, size }
      });
      
      let orders: OrderResponse[] = [];
      
      if (response.data && response.data.content) {
        orders = response.data.content.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (Array.isArray(response.data)) {
        orders = response.data.map((o: any) => mapBackendOrderToFrontend(o));
      }
      
      return {
        success: true,
        data: orders
      };
    } catch (error: any) {
      console.error('Error getting orders by date range:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tải đơn hàng'
      };
    }
  },

  // Search orders by keyword (orderId or accountName)
  searchOrders: async (
    keyword: string, 
    page: number = 0, 
    size: number = 10
  ): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.SEARCH_ORDERS, {
        params: { keyword, page, size }
      });
      
      let orders: OrderResponse[] = [];
      
      if (response.data && response.data.content) {
        orders = response.data.content.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (Array.isArray(response.data)) {
        orders = response.data.map((o: any) => mapBackendOrderToFrontend(o));
      }
      
      return {
        success: true,
        data: orders
      };
    } catch (error: any) {
      console.error('Error searching orders:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tìm kiếm đơn hàng'
      };
    }
  },

  // Filter orders by status
  filterOrdersByStatus: async (
    status: number, 
    page: number = 0, 
    size: number = 10
  ): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const response = await axiosInstance.get(API.FILTER_ORDERS, {
        params: { status, page, size }
      });
      
      let orders: OrderResponse[] = [];
      
      if (response.data && response.data.content) {
        orders = response.data.content.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (Array.isArray(response.data)) {
        orders = response.data.map((o: any) => mapBackendOrderToFrontend(o));
      }
      
      return {
        success: true,
        data: orders
      };
    } catch (error: any) {
      console.error('Error filtering orders:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể lọc đơn hàng'
      };
    }
  },

  // Search and filter orders (combined)
  searchAndFilterOrders: async (
    keyword: string | null,
    status: number | null,
    page: number = 0,
    size: number = 10
  ): Promise<{ success: boolean; data?: OrderResponse[]; message?: string }> => {
    try {
      const params: any = { page, size };
      if (keyword) params.keyword = keyword;
      if (status !== null) params.status = status;

      const response = await axiosInstance.get(API.SEARCH_AND_FILTER_ORDERS, { params });
      
      let orders: OrderResponse[] = [];
      
      if (response.data && response.data.content) {
        orders = response.data.content.map((o: any) => mapBackendOrderToFrontend(o));
      } else if (Array.isArray(response.data)) {
        orders = response.data.map((o: any) => mapBackendOrderToFrontend(o));
      }
      
      return {
        success: true,
        data: orders
      };
    } catch (error: any) {
      console.error('Error searching and filtering orders:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Không thể tìm kiếm và lọc đơn hàng'
      };
    }
  }
};

export default orderService;
