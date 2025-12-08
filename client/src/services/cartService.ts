import axios from '../libs/axios';
import { CONFIG, API } from '../config/constants';
import type { 
  AddToCartRequest, 
  UpdateCartItemRequest,
  CartResponse,
  AddToCartResponse 
} from '../types/cart';

export const cartService = {
  // Admin: Lấy tất cả giỏ hàng với phân trang
  async getAllCarts(page: number = 0, size: number = 10): Promise<CartResponse> {
    try {
      const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_ALL_CARTS}`, {
        params: { page, size }
      });
      return {
        success: true,
        message: 'Lấy danh sách giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi lấy danh sách giỏ hàng',
        error: error.message
      };
    }
  },

  // Admin: Lấy chi tiết giỏ hàng theo cartId
  async getCartById(cartId: string): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Getting cart by ID with URL:', `${CONFIG.API_GATEWAY}${API.GET_CART_BY_ID(cartId)}`);
      
      const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_CART_BY_ID(cartId)}`);
      return {
        success: true,
        message: 'Lấy thông tin giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi lấy thông tin giỏ hàng',
        error: error.message
      };
    }
  },

  // Lấy giỏ hàng theo account ID
  async getCartByAccount(accountId: string): Promise<CartResponse> {
    try {
      const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_CART_BY_ACCOUNT(accountId)}`);
      
      // Backend trả về CartResponse object trực tiếp, wrap nó trong success response
      return {
        success: true,
        message: 'Lấy giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      // Don't log 404 errors - it's normal for accounts to not have carts yet
      if (error.response?.status !== 404) {
      }
      return {
        success: false,
        message: error.response?.data?.message || 'Lỗi khi lấy giỏ hàng',
        error: error.message
      };
    }
  },

  // Tạo giỏ hàng mới
  async createCart(accountId: string): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Creating cart with URL:', `${CONFIG.API_GATEWAY}${API.CREATE_CART(accountId)}`);
      
      const response = await axios.post(`${CONFIG.API_GATEWAY}${API.CREATE_CART(accountId)}`);
      // Wrap response data trong success format
      return {
        success: true,
        message: 'Tạo giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi tạo giỏ hàng',
        error: error.message
      };
    }
  },

  // Thêm sản phẩm vào giỏ hàng
  async addToCart(request: AddToCartRequest): Promise<AddToCartResponse> {
    try {
      const response = await axios.post(`${CONFIG.API_GATEWAY}${API.ADD_ITEM_TO_CART(request.accountId)}`, {
        productId: request.productId,
        quantity: request.quantity
      });
      // Nếu backend trả về trực tiếp data, wrap nó trong success response
      return {
        success: true,
        message: 'Thêm vào giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi thêm vào giỏ hàng',
        error: error.message
      };
    }
  },

  // Cập nhật số lượng sản phẩm trong giỏ hàng
  async updateCartItem(request: UpdateCartItemRequest): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Updating cart item with URL:', `${CONFIG.API_GATEWAY}${API.UPDATE_CART_ITEM(request.cartItemId)}`);
      const response = await axios.put(`${CONFIG.API_GATEWAY}${API.UPDATE_CART_ITEM(request.cartItemId)}`, request);
      // Đảm bảo response format nhất quán
      if (response.data && typeof response.data === 'object') {
        // Nếu backend trả về success: true/false
        if ('success' in response.data) {
          return response.data;
        }
        // Nếu backend chỉ trả về message hoặc data
        return {
          success: true,
          message: response.data.message || 'Cập nhật số lượng thành công',
          data: response.data.data || response.data
        };
      }
      
      // Fallback response
      return {
        success: true,
        message: 'Cập nhật số lượng thành công',
        data: undefined
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi cập nhật giỏ hàng',
        error: error.message
      };
    }
  },

  // Xóa sản phẩm khỏi giỏ hàng
  async removeFromCart(cartItemId: string): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Removing cart item with URL:', `${CONFIG.API_GATEWAY}${API.REMOVE_ITEM_FROM_CART(cartItemId)}`);
      
      const response = await axios.delete(`${CONFIG.API_GATEWAY}${API.REMOVE_ITEM_FROM_CART(cartItemId)}`);
      // Đảm bảo response format nhất quán
      if (response.data && typeof response.data === 'object') {
        // Nếu backend trả về success: true/false
        if ('success' in response.data) {
          return response.data;
        }
        // Nếu backend chỉ trả về message hoặc data
        return {
          success: true,
          message: response.data.message || 'Xóa sản phẩm khỏi giỏ hàng thành công',
          data: response.data.data || response.data
        };
      }
      
      // Fallback response
      return {
        success: true,
        message: 'Xóa sản phẩm khỏi giỏ hàng thành công',
        data: undefined
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi xóa sản phẩm khỏi giỏ hàng',
        error: error.message
      };
    }
  },

  // Lấy danh sách cart items (và trạng thái cart)
  async getCartItems(accountId: string): Promise<CartResponse> {
    try {
      
      const response = await axios.get(`${CONFIG.API_GATEWAY}${API.GET_CART_ITEMS(accountId)}`);
      // Nếu backend trả về object có status/statusText thì trả về nguyên cart
      if (response.data && typeof response.data === 'object' && 'status' in response.data) {
        return {
          success: true,
          message: 'Lấy giỏ hàng thành công',
          data: response.data
        };
      }
      // Nếu chỉ trả về items
      return {
        success: true,
        message: 'Lấy danh sách thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi lấy danh sách giỏ hàng',
        error: error.message
      };
    }
  },

  // Xóa toàn bộ giỏ hàng
  async clearCart(accountId: string): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Clearing cart with URL:', `${CONFIG.API_GATEWAY}${API.CLEAR_CART(accountId)}`);
      
      const response = await axios.delete(`${CONFIG.API_GATEWAY}${API.CLEAR_CART(accountId)}`);
      // Backend trả về ResponseEntity<Void> với status 200 = success
      if (response.status === 200) {
        return {
          success: true,
          message: 'Xóa giỏ hàng thành công',
          data: undefined
        };
      }
      
      // Fallback case
      return {
        success: false,
        message: 'Có lỗi xảy ra khi xóa giỏ hàng',
        error: 'Unexpected response'
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi xóa giỏ hàng',
        error: error.message
      };
    }
  },

  // Admin: Vô hiệu hóa giỏ hàng
  async disableCart(cartId: string): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Disabling cart with URL:', `${CONFIG.API_GATEWAY}${API.DISABLE_CART(cartId)}`);
      
      const response = await axios.put(`${CONFIG.API_GATEWAY}${API.DISABLE_CART(cartId)}`);
      return {
        success: true,
        message: 'Vô hiệu hóa giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi vô hiệu hóa giỏ hàng',
        error: error.message
      };
    }
  },

  // Admin: Kích hoạt lại giỏ hàng
  async enableCart(cartId: string): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Enabling cart with URL:', `${CONFIG.API_GATEWAY}${API.ENABLE_CART(cartId)}`);
      
      const response = await axios.put(`${CONFIG.API_GATEWAY}${API.ENABLE_CART(cartId)}`);
      return {
        success: true,
        message: 'Kích hoạt giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi kích hoạt giỏ hàng',
        error: error.message
      };
    }
  },

  // Admin: Cập nhật trạng thái giỏ hàng
  async updateCartStatus(cartId: string, status: number): Promise<CartResponse> {
    try {
      console.log('🔄 CartService: Updating cart status with URL:', `${CONFIG.API_GATEWAY}${API.UPDATE_CART_STATUS(cartId, status)}`);
      
      const response = await axios.put(`${CONFIG.API_GATEWAY}${API.UPDATE_CART_STATUS(cartId, status)}`);
      return {
        success: true,
        message: 'Cập nhật trạng thái giỏ hàng thành công',
        data: response.data
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.response?.data?.message || error.message || 'Lỗi khi cập nhật trạng thái giỏ hàng',
        error: error.message
      };
    }
  }
};
