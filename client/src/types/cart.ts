export interface CartItem {
  cartItemId: string;
  productId: string;
  productName: string;
  productPrice: number;
  totalPrice: number;
  quantity: number;
  images?: string[]; // List of image URLs from backend
  discount?: number;
}

export interface Cart {
  cartId: string;
  accountId: string;
  accountName?: string; // Tên account (backward compatibility)
  account?: {
    accountId: string;
    accountName: string;
    accountPhone: string;
    status: number; // Account status
  };
  items: CartItem[];
  totalAmount: number;
  itemCount: number;
  createdAt: Date;
  updatedAt: Date;
  status: number; // 0: Disabled, 1: Active
  statusText?: string; // "Hoạt động" or "Khóa"
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
  accountId: string;
}

export interface UpdateCartItemRequest {
  cartItemId: string;
  quantity: number;
}

export interface CartResponse {
  success: boolean;
  message: string;
  data?: Cart | CartItem[] | CartItem | PaginatedCartResponse;
  error?: string;
}

export interface PaginatedCartResponse {
  content: Cart[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface AddToCartResponse {
  success: boolean;
  message: string;
  data?: {
    cartItemId: string;
    productId: string;
    quantity: number;
  };
  error?: string;
}
