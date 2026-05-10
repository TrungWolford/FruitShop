
// Production URL (Railway) hoặc localhost cho development
const PRODUCTION_API_URL = "https://fruitshop-665b.onrender.com";
const DEV_API_URL = "http://localhost:8080";

// Tự động chọn URL dựa trên môi trường
const BASE_URL = import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.PROD ? PRODUCTION_API_URL : DEV_API_URL);

export const CONFIG = {
  API_GATEWAY: BASE_URL,
  WS_ENDPOINT: import.meta.env.VITE_WS_URL || BASE_URL.replace('https', 'wss').replace('http', 'ws') + "/ws",
};

export const API_URL = import.meta.env.VITE_API_URL || BASE_URL;

export const API = {
  // Accounts - Theo AccountController.java
  GET_ALL_ACCOUNTS: '/account-service/api/account', // GET /api/account?page=0&size=10
  GET_ACCOUNT_BY_ID: (accountId: string) => `/account-service/api/account/${accountId}`, // GET /api/account/{accountId}
  CREATE_ACCOUNT: '/account-service/api/account', // POST /api/account
  UPDATE_ACCOUNT: (accountId: string) => `/account-service/api/account/${accountId}`, // PUT /api/account/{accountId}
  DELETE_ACCOUNT: (accountId: string) => `/account-service/api/account/${accountId}`, // DELETE /api/account/{accountId}
  GET_ACCOUNTS_BY_STATUS: (status: number) => `/account-service/api/account/status/${status}`, // GET /api/account/status/{status}?page=0&size=10
  GET_ACCOUNT_BY_PHONE: (accountPhone: string) => `/account-service/api/account/phone/${accountPhone}`, // GET /api/account/phone/{accountPhone}
  SEARCH_ACCOUNTS: '/account-service/api/account/search', // GET /api/account/search?accountName=xxx&page=0&size=10
  ACCOUNT_LOGIN: '/account-service/api/account/login', // POST /api/account/login

  // Products - Theo ProductController.java
  GET_ALL_PRODUCTS: '/catalog-service/api/catalog/products', // GET /api/catalog/products
  GET_PRODUCT_BY_ID: (productId: string) => `/catalog-service/api/catalog/products/${productId}`, // GET /api/catalog/products/{productId}
  CREATE_PRODUCT: '/catalog-service/api/catalog/products', // POST /api/catalog/products (Admin only)
  UPDATE_PRODUCT: (productId: string) => `/catalog-service/api/catalog/products/${productId}`, // PUT /api/catalog/products/{productId} (Admin only)
  DELETE_PRODUCT: (productId: string) => `/catalog-service/api/catalog/products/${productId}`, // DELETE /api/catalog/products/{productId} (Admin only)
  FILTER_PRODUCTS: '/catalog-service/api/catalog/products/filter', // GET /api/catalog/products/filter?...
  SEARCH_PRODUCTS: '/catalog-service/api/catalog/products/search', // GET /api/catalog/products/search?...

  // File Upload - Cloudinary
  UPLOAD_IMAGE: '/catalog-service/api/upload/image', // POST /api/upload/image
  UPLOAD_FILE: '/catalog-service/api/upload/file', // POST /api/upload/file  
  GET_FILE_INFO: '/catalog-service/api/upload/info', // GET /api/upload/info?publicId=xxx
  DELETE_FILE: '/catalog-service/api/upload/delete', // DELETE /api/upload/delete/{publicId}
  OPTIMIZE_URL: '/catalog-service/api/upload/optimize', // POST /api/upload/optimize

  // Categories - Theo CategoryController.java
  GET_ALL_CATEGORIES: '/catalog-service/api/catalog/categories', // GET /api/catalog/categories
  GET_CATEGORY_BY_ID: (categoryId: string) => `/catalog-service/api/catalog/categories/${categoryId}`, // GET /api/catalog/categories/{categoryId}
  CREATE_CATEGORY: '/catalog-service/api/catalog/categories', // POST /api/catalog/categories (Admin only)
  UPDATE_CATEGORY: (categoryId: string) => `/catalog-service/api/catalog/categories/${categoryId}`, // PUT /api/catalog/categories/{categoryId} (Admin only)
  DELETE_CATEGORY: (categoryId: string) => `/catalog-service/api/catalog/categories/${categoryId}`, // DELETE /api/catalog/categories/{categoryId} (Admin only)
  SEARCH_CATEGORIES: '/catalog-service/api/catalog/categories/search', // GET /api/catalog/categories/search?keyword=...

  // Orders - Theo OrderController.java
  GET_ALL_ORDERS: '/order-service/api/order', // GET /api/order (Admin only)
  GET_ORDER_BY_ID: (orderId: string) => `/order-service/api/order/${orderId}`, // GET /api/order/{orderId}
  CREATE_ORDER: '/order-service/api/order', // POST /api/order (Customer only)
  UPDATE_ORDER: (orderId: string) => `/order-service/api/order/${orderId}`, // PUT /api/order/{orderId} (Admin only)
  DELETE_ORDER: (orderId: string) => `/order-service/api/order/${orderId}`, // DELETE /api/order/{orderId} (Admin only)
  GET_ORDERS_BY_ACCOUNT: (accountId: string) => `/order-service/api/order/account/${accountId}`, // GET /api/order/account/{accountId} (Customer only)
  CANCEL_ORDER: (orderId: string) => `/order-service/api/order/${orderId}/cancel`, // PUT /api/order/{orderId}/cancel (Customer only)
  COMPLETE_ORDER: (orderId: string) => `/order-service/api/order/${orderId}/complete`, // PUT /api/order/{orderId}/complete (Customer confirms delivery)
  CONFIRM_ORDER: (orderId: string) => `/order-service/api/order/${orderId}/confirm`, // PUT /api/order/{orderId}/confirm (Admin confirms order)
  START_DELIVERY: (orderId: string) => `/order-service/api/order/${orderId}/start-delivery`, // PUT /api/order/{orderId}/start-delivery (Admin starts delivery)
  UPDATE_ORDER_STATUS: (orderId: string) => `/order-service/api/order/${orderId}/update-status`, // PUT /api/order/{orderId}/update-status?status=xxx (Admin only)
  GET_ORDER_DETAILS: (orderId: string) => `/order-service/api/order/${orderId}/details`, // GET /api/order/{orderId}/details
  FILTER_ORDERS_BY_STATUS: '/order-service/api/order/status', // GET /api/order/status?status=xxx&page=0&size=10 (Admin only)
  FILTER_ORDERS_BY_DATE: '/order-service/api/order/date-range', // GET /api/order/date-range?startDate=xxx&endDate=xxx&page=0&size=10 (Admin only)
  SEARCH_ORDERS: '/order-service/api/order/search', // GET /api/order/search?keyword=xxx&page=0&size=10 (Admin only)
  FILTER_ORDERS: '/order-service/api/order/filter', // GET /api/order/filter?status=xxx&page=0&size=10 (Admin only)
  SEARCH_AND_FILTER_ORDERS: '/order-service/api/order/search-filter', // GET /api/order/search-filter?keyword=xxx&status=xxx&page=0&size=10 (Admin only)

  // Cart - Theo CartController.java
  GET_ALL_CARTS: '/cart-service/api/cart', // GET /api/cart?page=0&size=10 (Admin only)
  GET_CART_BY_ID: (cartId: string) => `/cart-service/api/cart/${cartId}`, // GET /api/cart/{cartId} (Admin only)
  GET_CART_BY_ACCOUNT: (accountId: string) => `/cart-service/api/cart/account/${accountId}`, // GET /api/cart/account/{accountId}
  CREATE_CART: (accountId: string) => `/cart-service/api/cart/account/${accountId}`, // POST /api/cart/account/{accountId}
  DELETE_CART: (cartId: string) => `/cart-service/api/cart/${cartId}`, // DELETE /api/cart/{cartId}
  ADD_ITEM_TO_CART: (accountId: string) => `/cart-service/api/cart/account/${accountId}/items`, // POST /api/cart/account/{accountId}/items
  UPDATE_CART_ITEM: (cartItemId: string) => `/cart-service/api/cart/items/${cartItemId}`, // PUT /api/cart/items/{cartItemId}
  REMOVE_ITEM_FROM_CART: (cartItemId: string) => `/cart-service/api/cart/items/${cartItemId}`, // DELETE /api/cart/items/{cartItemId}
  GET_CART_ITEMS: (accountId: string) => `/cart-service/api/cart/account/${accountId}/items`, // GET /api/cart/account/{accountId}/items
  CLEAR_CART: (accountId: string) => `/cart-service/api/cart/account/${accountId}/clear`, // DELETE /api/cart/account/{accountId}/clear
  DISABLE_CART: (cartId: string) => `/cart-service/api/cart/${cartId}/disable`, // PUT /api/cart/{cartId}/disable (Admin only)
  ENABLE_CART: (cartId: string) => `/cart-service/api/cart/${cartId}/enable`, // PUT /api/cart/{cartId}/enable (Admin only)
  UPDATE_CART_STATUS: (cartId: string, status: number) => `/cart-service/api/cart/${cartId}/status/${status}`, // PUT /api/cart/{cartId}/status/{status} (Admin only)

  // Roles - Theo RoleController.java
  GET_ALL_ROLES: '/account-service/api/role', // GET /api/role
  GET_ROLE_BY_ID: (roleId: string) => `/account-service/api/role/${roleId}`, // GET /api/role/{roleId}
  CREATE_ROLE: '/account-service/api/role', // POST /api/role
  UPDATE_ROLE: (roleId: string) => `/account-service/api/role/${roleId}`, // PUT /api/role/{roleId}
  DELETE_ROLE: (roleId: string) => `/account-service/api/role/${roleId}`, // DELETE /api/role/{roleId}

  // Shipping - Theo ShippingController.java
  GET_ALL_SHIPPING: '/order-service/api/shipping', // GET /api/shipping
  GET_SHIPPING_BY_ID: (shippingId: string) => `/order-service/api/shipping/${shippingId}`, // GET /api/shipping/{shippingId}
  CREATE_SHIPPING: '/order-service/api/shipping', // POST /api/shipping
  UPDATE_SHIPPING: (shippingId: string) => `/order-service/api/shipping/${shippingId}`, // PUT /api/shipping/{shippingId}
  DELETE_SHIPPING: (shippingId: string) => `/order-service/api/shipping/${shippingId}`, // DELETE /api/shipping/{shippingId}
  GET_SHIPPING_BY_ORDER: (orderId: string) => `/order-service/api/orders/${orderId}/shipping`, // GET /api/orders/{orderId}/shipping
  GET_SHIPPING_BY_ACCOUNT: (accountId: string) => `/order-service/api/shipping/account/${accountId}`, // GET /api/shipping/account/{accountId}
  UPDATE_SHIPPING_STATUS: (shippingId: string) => `/order-service/api/shipping/${shippingId}/status`, // PUT /api/shipping/{shippingId}/status
  SEARCH_SHIPPINGS: '/order-service/api/shipping/search', // GET /api/shipping/search?keyword=xxx&page=0&size=10
  FILTER_SHIPPINGS: '/order-service/api/shipping/filter', // GET /api/shipping/filter?status=xxx&page=0&size=10
  SEARCH_AND_FILTER_SHIPPINGS: '/order-service/api/shipping/search', // GET /api/shipping/search?keyword=xxx&status=xxx&page=0&size=10

  // Ratings - Theo RatingController.java
  GET_ALL_RATINGS: '/review-service/api/rating', // GET /api/rating?page=0&size=10
  GET_RATINGS_BY_ACCOUNT: (accountId: string) => `/review-service/api/rating/account/${accountId}`, // GET /api/rating/account/{accountId}?page=0&size=10
  GET_RATINGS_BY_PRODUCT: (productId: string) => `/review-service/api/rating/product/${productId}`, // GET /api/rating/product/{productId}?page=0&size=10
  GET_RATING_BY_ACCOUNT_AND_PRODUCT: (accountId: string, productId: string) => `/review-service/api/rating/account/${accountId}/product/${productId}`, // GET /api/rating/account/{accountId}/product/{productId}
  GET_AVERAGE_RATING_BY_PRODUCT: (productId: string) => `/review-service/api/rating/product/${productId}/average`, // GET /api/rating/product/{productId}/average
  CREATE_RATING: '/review-service/api/rating', // POST /api/rating
  UPDATE_RATING: (ratingId: string) => `/review-service/api/rating/${ratingId}`, // PUT /api/rating/{ratingId}
  CHANGE_RATING_STATUS: (ratingId: string) => `/review-service/api/rating/${ratingId}/status`, // PATCH /api/rating/{ratingId}/status
  DELETE_RATING: (ratingId: string) => `/review-service/api/rating/${ratingId}`, // DELETE /api/rating/{ratingId}

  // MoMo Payment - Theo MomoController.java
  CREATE_MOMO_PAYMENT: '/momo/create-payment', // POST /api/momo/create-payment
  MOMO_IPN_HANDLER: '/momo/ipn-handler', // POST /api/momo/ipn-handler (called by MoMo)
  MOMO_RETURN: '/momo/return', // GET /api/momo/return
  CHECK_MOMO_STATUS: (orderId: string) => `/momo/check-status/${orderId}`, // GET /api/momo/check-status/{orderId}

  // Refunds - Theo RefundController.java (order-service)
  REFUND: '/order-service/api/refunds', // GET /api/refunds?page=0&size=10 | POST /api/refunds
  GET_REFUND_BY_ID: (refundId: string) => `/order-service/api/refunds/${refundId}`, // GET /api/refunds/{refundId}
  GET_REFUNDS_BY_STATUS: (status: string) => `/order-service/api/refunds/status/${status}`, // GET /api/refunds/status/{status}
  GET_REFUNDS_BY_ORDER: (orderId: string) => `/order-service/api/refunds/order/${orderId}`, // GET /api/refunds/order/{orderId}
  SEARCH_REFUNDS: '/order-service/api/refunds/search', // GET /api/refunds/search?keyword=xxx
  GET_REFUNDS_BY_DATE_RANGE: '/order-service/api/refunds/daterange', // GET /api/refunds/daterange?startDate=xxx&endDate=xxx
  UPDATE_REFUND_STATUS: (refundId: string) => `/order-service/api/refunds/${refundId}/status`, // PUT /api/refunds/{refundId}/status
  APPROVE_REFUND: (refundId: string) => `/order-service/api/refunds/${refundId}/approve`, // PUT (via order) /api/orders/{orderId}/refunds/{refundId}/approve
  REJECT_REFUND: (refundId: string) => `/order-service/api/refunds/${refundId}/reject`, // PUT /api/refunds/{refundId}/reject
  COMPLETE_REFUND: (refundId: string) => `/order-service/api/refunds/${refundId}/complete`, // PUT /api/refunds/{refundId}/complete
  DELETE_REFUND: (refundId: string) => `/order-service/api/refunds/${refundId}`, // DELETE /api/refunds/{refundId}
  CANCEL_REFUND: (refundId: string) => `/order-service/api/refunds/${refundId}/cancel`, // PUT /api/refunds/{refundId}/cancel
  GET_PENDING_REFUNDS_COUNT: '/order-service/api/refunds/pending/count', // GET /api/refunds/pending/count

  // Payments - Theo PaymentController.java
  PAYMENT: '/payment-service/api/payment', // GET /api/payment?page=0&size=10 | POST /api/payment
  GET_PAYMENT_BY_ID: (paymentId: string) => `/payment-service/api/payment/${paymentId}`, // GET /api/payment/{paymentId}
  UPDATE_PAYMENT: (paymentId: string) => `/payment-service/api/payment/${paymentId}`, // PUT /api/payment/{paymentId}
  GET_PAYMENTS_BY_STATUS: (status: number) => `/payment-service/api/payment/status/${status}`, // GET /api/payment/status/{status}?page=0&size=10
  UPDATE_PAYMENT_STATUS: (paymentId: string) => `/payment-service/api/payment/${paymentId}/status`, // PUT /api/payment/{paymentId}/status?status=1
  GET_PAYMENT_BY_TRANSACTION_ID: (transactionId: string) => `/payment-service/api/payment/transaction/${transactionId}`, // GET /api/payment/transaction/{transactionId}

  //Chatbot
  SESSION: '/ai-service/api/chat/sessions',
  MESSAGE: '/ai-service/api/chat/messages',

  //adminChat
  GET_ADMIN_SESSIONS: '/ai-service/api/chat/admin/sessions',
  GET_PENDING_TICKETS: '/ai-service/api/chat/admin/customer-care/tickets',
  GET_TICKET_MESSAGES: (sessionId: string) => `/ai-service/api/chat/admin/customer-care/tickets/${sessionId}/messages`,
  REPLY_TICKET: (sessionId: string) => `/ai-service/api/chat/admin/customer-care/tickets/${sessionId}/reply`,
  MARK_SESSION_AS_READ: (sessionId: string) => `/ai-service/api/chat/sessions/${sessionId}/read`,
  UPDATE_SESSION: (sessionId: string) => `/ai-service/api/chat/sessions/${sessionId}`,

} as const;

// HTTP Status Codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
} as const;

// Local Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER_PROFILE: 'user_profile',
  THEME: 'theme',
  LANGUAGE: 'language',
  REMEMBER_ME: 'remember_me',
  CART: 'cart',
} as const;

// Application Constants
export const APP_CONFIG = {
  APP_NAME: 'WebSach',
  APP_VERSION: '1.0.0',
  PAGINATION: {
    DEFAULT_PAGE_SIZE: 10,
    MAX_PAGE_SIZE: 100,
  },
  FILE_UPLOAD: {
    MAX_FILE_SIZE: 10 * 1024 * 1024, // 10MB
    ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
    ALLOWED_VIDEO_TYPES: ['video/mp4', 'video/webm', 'video/ogg'],
    ALLOWED_DOCUMENT_TYPES: ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
  },

  // Account status constants
  ACCOUNT_STATUS: {
    INACTIVE: 0,   // Không hoạt động
    ACTIVE: 1,     // Đang hoạt động
  },

  // Product status constants
  PRODUCT_STATUS: {
    INACTIVE: 0,   // Không hoạt động
    ACTIVE: 1,     // Đang hoạt động
  },

  // Order status constants
  ORDER_STATUS: {
    PENDING: 0,      // Chờ xử lý
    CONFIRMED: 1,    // Đã xác nhận
    SHIPPING: 2,     // Đang giao hàng
    DELIVERED: 3,    // Đã giao hàng
    CANCELLED: 4,    // Đã hủy
  },

  // Role types
  ROLE_TYPES: {
    ADMIN: 'ADMIN',
    CUSTOMER: 'CUSTOMER',
  },
} as const;

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet.',
  UNAUTHORIZED: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  FORBIDDEN: 'Bạn không có quyền truy cập tính năng này.',
  NOT_FOUND: 'Không tìm thấy tài nguyên yêu cầu.',
  SERVER_ERROR: 'Lỗi máy chủ. Vui lòng thử lại sau.',
  VALIDATION_ERROR: 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.',
  FILE_TOO_LARGE: 'File quá lớn. Kích thước tối đa cho phép là 10MB.',
  INVALID_FILE_TYPE: 'Định dạng file không được hỗ trợ.',

  // Account specific errors
  ACCOUNT_NOT_FOUND: 'Không tìm thấy tài khoản.',
  ACCOUNT_CREATE_FAILED: 'Tạo tài khoản thất bại.',
  ACCOUNT_UPDATE_FAILED: 'Cập nhật tài khoản thất bại.',
  LOGIN_FAILED: 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.',

  // Product specific errors
  PRODUCT_NOT_FOUND: 'Không tìm thấy sản phẩm.',
  PRODUCT_CREATE_FAILED: 'Tạo sản phẩm thất bại.',
  PRODUCT_UPDATE_FAILED: 'Cập nhật sản phẩm thất bại.',

  // Category specific errors
  CATEGORY_NOT_FOUND: 'Không tìm thấy danh mục.',
  CATEGORY_CREATE_FAILED: 'Tạo danh mục thất bại.',
  CATEGORY_UPDATE_FAILED: 'Cập nhật danh mục thất bại.',

  // Order specific errors
  ORDER_NOT_FOUND: 'Không tìm thấy đơn hàng.',
  ORDER_CREATE_FAILED: 'Tạo đơn hàng thất bại.',
  ORDER_UPDATE_FAILED: 'Cập nhật đơn hàng thất bại.',
  ORDER_CANCEL_FAILED: 'Hủy đơn hàng thất bại.',

  // Cart specific errors
  CART_NOT_FOUND: 'Không tìm thấy giỏ hàng.',
  CART_ADD_ITEM_FAILED: 'Thêm sản phẩm vào giỏ hàng thất bại.',
  CART_UPDATE_FAILED: 'Cập nhật giỏ hàng thất bại.',
  CART_REMOVE_ITEM_FAILED: 'Xóa sản phẩm khỏi giỏ hàng thất bại.',
} as const;

// Success Messages
export const SUCCESS_MESSAGES = {
  // General success messages
  UPDATE_SUCCESS: 'Cập nhật thành công!',
  DELETE_SUCCESS: 'Xóa thành công!',
  SAVE_SUCCESS: 'Lưu thành công!',

  // Account specific success
  ACCOUNT_CREATED: 'Tạo tài khoản thành công!',
  ACCOUNT_UPDATED: 'Cập nhật tài khoản thành công!',
  LOGIN_SUCCESS: 'Đăng nhập thành công!',
  LOGOUT_SUCCESS: 'Đăng xuất thành công!',

  // Product specific success
  PRODUCT_CREATED: 'Tạo sản phẩm thành công!',
  PRODUCT_UPDATED: 'Cập nhật sản phẩm thành công!',
  PRODUCT_STATUS_CHANGED: 'Thay đổi trạng thái sản phẩm thành công!',

  // Category specific success
  CATEGORY_CREATED: 'Tạo danh mục thành công!',
  CATEGORY_UPDATED: 'Cập nhật danh mục thành công!',

  // Order specific success
  ORDER_CREATED: 'Tạo đơn hàng thành công!',
  ORDER_UPDATED: 'Cập nhật đơn hàng thành công!',
  ORDER_CANCELLED: 'Hủy đơn hàng thành công!',

  // Cart specific success
  CART_ITEM_ADDED: 'Thêm sản phẩm vào giỏ hàng thành công!',
  CART_ITEM_UPDATED: 'Cập nhật giỏ hàng thành công!',
  CART_ITEM_REMOVED: 'Xóa sản phẩm khỏi giỏ hàng thành công!',
  CART_CLEARED: 'Xóa toàn bộ giỏ hàng thành công!',
} as const;

// Route Paths
export const ROUTES = {
  HOME: '/',

  // Auth routes
  REGISTER: '/account/register',

  // Product routes
  PRODUCTS: '/products',
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,
  PRODUCT_CREATE: '/products/create',
  PRODUCT_EDIT: (id: string) => `/products/edit/${id}`,

  // Category routes
  CATEGORIES: '/categories',
  CATEGORY_DETAIL: (id: string) => `/categories/${id}`,
  CATEGORY_CREATE: '/categories/create',
  CATEGORY_EDIT: (id: string) => `/categories/edit/${id}`,

  // Order routes
  ORDERS: '/orders',
  ORDER_DETAIL: (id: string) => `/orders/${id}`,
  ORDER_HISTORY: '/orders/history',

  // Cart routes
  CART: '/cart',
  CHECKOUT: '/checkout',

  // Account routes
  PROFILE: '/profile',
  ACCOUNT_SETTINGS: '/account/settings',

  // Admin routes
  ADMIN: {
    DASHBOARD: '/admin',
    ACCOUNTS: '/admin/accounts',
    PRODUCTS: '/admin/products',
    CATEGORIES: '/admin/categories',
    ORDERS: '/admin/orders',
    ROLES: '/admin/roles',
    SHIPPING: '/admin/shipping',
  },

  // Search
  SEARCH: '/search',
} as const;

// Validation Rules
export const VALIDATION_RULES = {
  EMAIL: {
    PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    MESSAGE: 'Email không hợp lệ',
  },
  PASSWORD: {
    MIN_LENGTH: 8,
    PATTERN: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
    MESSAGE: 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt',
  },
  PHONE: {
    PATTERN: /^(\+84|84|0)?([3|5|7|8|9])+([0-9]{8})$/,
    MESSAGE: 'Số điện thoại không hợp lệ',
  },
  ACCOUNT_NAME: {
    MIN_LENGTH: 2,
    MAX_LENGTH: 50,
    MESSAGE: 'Tên tài khoản phải từ 2-50 ký tự',
  },
  PRODUCT_NAME: {
    MIN_LENGTH: 2,
    MAX_LENGTH: 200,
    MESSAGE: 'Tên sản phẩm phải từ 2-200 ký tự',
  },
  CATEGORY_NAME: {
    MIN_LENGTH: 2,
    MAX_LENGTH: 100,
    MESSAGE: 'Tên danh mục phải từ 2-100 ký tự',
  },
  DESCRIPTION: {
    MAX_LENGTH: 3600,
    MESSAGE: 'Mô tả không được vượt quá 3600 ký tự',
  },
  PRICE: {
    MIN: 0,
    MESSAGE: 'Giá sản phẩm phải lớn hơn 0',
  },
  QUANTITY: {
    MIN: 0,
    MESSAGE: 'Số lượng phải lớn hơn hoặc bằng 0',
  },
} as const;

// Theme Configuration
export const THEME_CONFIG = {
  LIGHT: 'light',
  DARK: 'dark',
  SYSTEM: 'system',
} as const;

// Language Configuration
export const LANGUAGE_CONFIG = {
  VI: 'vi',
  EN: 'en',
} as const;

export default {
  CONFIG,
  API_URL,
  API,
  HTTP_STATUS,
  STORAGE_KEYS,
  APP_CONFIG,
  ERROR_MESSAGES,
  SUCCESS_MESSAGES,
  ROUTES,
  VALIDATION_RULES,
  THEME_CONFIG,
  LANGUAGE_CONFIG,
};
