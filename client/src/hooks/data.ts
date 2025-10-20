import type { Account } from '../types/account';
import type { Category, Product } from '../types/product';

export const mockAccounts: Account[] = [
  {
    id: 1,
    username: 'admin',
    phone: '0901234567',
    password: 'admin123',
    status: 'ACTIVE',
    role: 'ADMIN'
  },
  {
    id: 2,
    username: 'nguyenvana',
    phone: '0912345678',
    password: 'password123',
    status: 'ACTIVE',
    role: 'CUSTOMER'
  },
  {
    id: 3,
    username: 'tranthib',
    phone: '0923456789',
    password: 'mypassword',
    status: 'ACTIVE',
    role: 'CUSTOMER'
  },
  {
    id: 4,
    username: 'lequangc',
    phone: '0934567890',
    password: 'securepass',
    status: 'INACTIVE',
    role: 'CUSTOMER'
  },
  {
    id: 5,
    username: 'phamthid',
    phone: '0945678901',
    password: 'userpass123',
    status: 'BANNED',
    role: 'CUSTOMER'
  },
  {
    id: 6,
    username: 'hoanganhe',
    phone: '0956789012',
    password: 'password456',
    status: 'ACTIVE',
    role: 'CUSTOMER'
  },
  {
    id: 7,
    username: 'moderator1',
    phone: '0967890123',
    password: 'mod123456',
    status: 'ACTIVE',
    role: 'ADMIN'
  },
  {
    id: 8,
    username: 'vuthif',
    phone: '0978901234',
    password: 'mypass789',
    status: 'ACTIVE',
    role: 'CUSTOMER'
  },
  {
    id: 9,
    username: 'dangvanb',
    phone: '0989012345',
    password: 'strongpass',
    status: 'INACTIVE',
    role: 'CUSTOMER'
  },
  {
    id: 10,
    username: 'buithing',
    phone: '0990123456',
    password: 'password999',
    status: 'ACTIVE',
    role: 'CUSTOMER'
  }
];

// Helper functions để làm việc với mock data
export const getAccountById = (id: number): Account | undefined => {
  return mockAccounts.find(account => account.id === id);
};

export const getAccountByUsername = (username: string): Account | undefined => {
  return mockAccounts.find(account => account.username === username);
};

export const getAccountsByRole = (role: 'ADMIN' | 'CUSTOMER'): Account[] => {
  return mockAccounts.filter(account => account.role === role);
};

export const getAccountsByStatus = (status: 'ACTIVE' | 'INACTIVE' | 'BANNED'): Account[] => {
  return mockAccounts.filter(account => account.status === status);
};

export const getTotalAccounts = (): number => {
  return mockAccounts.length;
};

export const getActiveAccounts = (): Account[] => {
  return mockAccounts.filter(account => account.status === 'ACTIVE');
};

export const getAdminAccounts = (): Account[] => {
  return mockAccounts.filter(account => account.role === 'ADMIN');
};

// ==================== CATEGORIES ====================
export const mockCategories: Category[] = [
  {
    categoryId: 'C1A2B3C',
    categoryName: 'Văn học',
    description: 'Sách văn học, tiểu thuyết, tho văn',
    status: 1
  },
  {
    categoryId: 'C2D4E5F',
    categoryName: 'Khoa học công nghệ',
    description: 'Sách về khoa học, công nghệ, kỹ thuật',
    status: 1
  },
  {
    categoryId: 'C3G6H7I',
    categoryName: 'Kinh tế - Quản lý',
    description: 'Sách về kinh tế, quản lý, kinh doanh',
    status: 1
  },
  {
    categoryId: 'C4J8K9L',
    categoryName: 'Tâm lý - Kỹ năng sống',
    description: 'Sách về tâm lý học, kỹ năng sống, phát triển bản thân',
    status: 1
  },
  {
    categoryId: 'C5M0N1O',
    categoryName: 'Thiếu nhi',
    description: 'Sách dành cho trẻ em, truyện thiếu nhi',
    status: 1
  },
  {
    categoryId: 'C6P2Q3R',
    categoryName: 'Lịch sử',
    description: 'Sách về lịch sử, văn hóa, truyền thống',
    status: 1
  },
  {
    categoryId: 'C7S4T5U',
    categoryName: 'Triết học',
    description: 'Sách về triết học, tư tưởng, đạo đức',
    status: 1
  },
  {
    categoryId: 'C8V6W7X',
    categoryName: 'Ngoại ngữ',
    description: 'Sách học ngoại ngữ, từ điển, ngữ pháp',
    status: 1
  },
  {
    categoryId: 'C9Y8Z9A',
    categoryName: 'Sách giáo khoa',
    description: 'Sách giáo khoa các cấp học',
    status: 1
  },
  {
    categoryId: 'C0B1C2D',
    categoryName: 'Truyện tranh - Manga',
    description: 'Truyện tranh, manga, comic',
    status: 0
  }
];

// ==================== PRODUCTS ====================
export const mockProducts: Product[] = [
  {
    productId: 'P1A2B3C4D',
    productName: 'Đắc Nhân Tâm',
    categories: [mockCategories[3]], // Tâm lý - Kỹ năng sống
    image: [
      '/images/dac-nhan-tam-1.jpg',
      '/images/dac-nhan-tam-2.jpg'
    ],
    price: 89000,
    stock: 150,
    description: 'Cuốn sách kinh điển về nghệ thuật giao tiếp và ứng xử trong cuộc sống. Đây là một trong những cuốn sách bán chạy nhất mọi thời đại.',
    createdAt: new Date('2024-01-15'),
    status: 1
  },
  {
    productId: 'P2E5F6G7H',
    productName: 'Sapiens: Lược sử loài người',
    categories: [mockCategories[5]], // Lịch sử
    image: [
      '/images/sapiens-1.jpg',
      '/images/sapiens-2.jpg',
      '/images/sapiens-3.jpg'
    ],
    price: 299000,
    stock: 75,
    description: 'Cuốn sách đưa chúng ta đi qua toàn bộ lịch sử phát triển của loài người từ thời nguyên thủy đến hiện đại.',
    createdAt: new Date('2024-02-20'),
    status: 1
  },
  {
    productId: 'P3I9J0K1L',
    productName: 'Tôi Tài Giỏi, Bạn Cũng Thế',
    categories: [mockCategories[3]], // Tâm lý - Kỹ năng sống
    image: [
      '/images/toi-tai-gioi-1.jpg'
    ],
    price: 79000,
    stock: 200,
    description: 'Cuốn sách giúp phát triển tư duy tích cực và kỹ năng học tập hiệu quả cho học sinh, sinh viên.',
    createdAt: new Date('2024-03-10'),
    status: 1
  },
  {
    productId: 'P4M2N3O4P',
    productName: 'Kinh Tế Học Vĩ Mô',
    categories: [mockCategories[2]], // Kinh tế - Quản lý
    image: [
      '/images/kinh-te-vi-mo-1.jpg',
      '/images/kinh-te-vi-mo-2.jpg'
    ],
    price: 450000,
    stock: 50,
    description: 'Giáo trình kinh tế học vĩ mô dành cho sinh viên đại học, được sử dụng rộng rãi tại các trường đại học.',
    createdAt: new Date('2024-01-25'),
    status: 1
  },
  {
    productId: 'P5Q5R6S7T',
    productName: 'Thành Phố Tình Yêu',
    categories: [mockCategories[0]], // Văn học
    image: [
      '/images/thanh-pho-tinh-yeu-1.jpg'
    ],
    price: 65000,
    stock: 120,
    description: 'Tiểu thuyết lãng mạn về những câu chuyện tình yêu trong thành phố hiện đại.',
    createdAt: new Date('2024-04-05'),
    status: 1
  },
  {
    productId: 'P6U8V9W0X',
    productName: 'Lập Trình Python Cơ Bản',
    categories: [mockCategories[1]], // Khoa học công nghệ
    image: [
      '/images/python-co-ban-1.jpg',
      '/images/python-co-ban-2.jpg'
    ],
    price: 199000,
    stock: 80,
    description: 'Sách hướng dẫn lập trình Python từ cơ bản đến nâng cao, phù hợp cho người mới bắt đầu.',
    createdAt: new Date('2024-02-28'),
    status: 1
  },
  {
    productId: 'P7Y1Z2A3B',
    productName: 'Doraemon - Tập 15',
    categories: [mockCategories[4], mockCategories[9]], // Thiếu nhi + Truyện tranh
    image: [
      '/images/doraemon-15-1.jpg'
    ],
    price: 25000,
    stock: 300,
    description: 'Tập truyện tranh Doraemon với những câu chuyện thú vị về Nobita và chú mèo máy đến từ tương lai.',
    createdAt: new Date('2024-03-20'),
    status: 1
  },
  {
    productId: 'P8C4D5E6F',
    productName: 'Triết Học Phương Đông',
    categories: [mockCategories[6]], // Triết học
    image: [
      '/images/triet-hoc-phuong-dong-1.jpg',
      '/images/triet-hoc-phuong-dong-2.jpg'
    ],
    price: 350000,
    stock: 45,
    description: 'Tổng quan về các trường phái triết học phương Đông, từ Ấn Độ, Trung Quốc đến Nhật Bản.',
    createdAt: new Date('2024-01-30'),
    status: 1
  },
  {
    productId: 'P9G7H8I9J',
    productName: 'English Grammar in Use',
    categories: [mockCategories[7]], // Ngoại ngữ
    image: [
      '/images/english-grammar-1.jpg'
    ],
    price: 180000,
    stock: 95,
    description: 'Sách ngữ pháp tiếng Anh phổ biến nhất thế giới, phù hợp cho người học trình độ trung cấp.',
    createdAt: new Date('2024-04-12'),
    status: 1
  },
  {
    productId: 'P0K0L1M2N',
    productName: 'Toán Lớp 12 - Sách Giáo Khoa',
    categories: [mockCategories[8]], // Sách giáo khoa
    image: [
      '/images/toan-12-sgk-1.jpg'
    ],
    price: 45000,
    stock: 250,
    description: 'Sách giáo khoa Toán lớp 12 theo chương trình giáo dục phổ thông mới.',
    createdAt: new Date('2024-05-01'),
    status: 1
  },
  {
    productId: 'P1O3P4Q5R',
    productName: 'Người Khổng Lồ Sa Ngã',
    categories: [mockCategories[0]], // Văn học
    image: [
      '/images/nguoi-khong-lo-1.jpg',
      '/images/nguoi-khong-lo-2.jpg'
    ],
    price: 420000,
    stock: 30,
    description: 'Tiểu thuyết lịch sử hấp dẫn về cuộc sống của các gia đình trong Thế chiến thứ nhất.',
    createdAt: new Date('2024-03-15'),
    status: 0 // Tạm hết hàng
  },
  {
    productId: 'P2S6T7U8V',
    productName: 'Kỹ Năng Quản Lý Thời Gian',
    categories: [mockCategories[3]], // Tâm lý - Kỹ năng sống
    image: [
      '/images/quan-ly-thoi-gian-1.jpg'
    ],
    price: 95000,
    stock: 110,
    description: 'Hướng dẫn các phương pháp quản lý thời gian hiệu quả để tăng năng suất làm việc.',
    createdAt: new Date('2024-04-20'),
    status: 1
  }
];

// ==================== CATEGORY HELPER FUNCTIONS ====================
export const getCategoryById = (categoryId: string): Category | undefined => {
  return mockCategories.find(category => category.categoryId === categoryId);
};

export const getCategoryByName = (categoryName: string): Category | undefined => {
  return mockCategories.find(category => 
    category.categoryName.toLowerCase().includes(categoryName.toLowerCase())
  );
};

export const getActiveCategories = (): Category[] => {
  return mockCategories.filter(category => category.status === 1);
};

// ==================== PRODUCT HELPER FUNCTIONS ====================
export const getProductById = (productId: string): Product | undefined => {
  return mockProducts.find(product => product.productId === productId);
};

export const getProductsByCategory = (categoryId: string): Product[] => {
  return mockProducts.filter(product => 
    product.categories.some(category => category.categoryId === categoryId)
  );
};

export const getProductsByStatus = (status: number): Product[] => {
  return mockProducts.filter(product => product.status === status);
};

export const getActiveProducts = (): Product[] => {
  return mockProducts.filter(product => product.status === 1);
};

export const getProductsByPriceRange = (minPrice: number, maxPrice: number): Product[] => {
  return mockProducts.filter(product => 
    product.price >= minPrice && product.price <= maxPrice && product.status === 1
  );
};

export const searchProducts = (keyword: string): Product[] => {
  const lowerKeyword = keyword.toLowerCase();
  return mockProducts.filter(product => 
    product.status === 1 && (
      product.productName.toLowerCase().includes(lowerKeyword) ||
      product.description.toLowerCase().includes(lowerKeyword)
    )
  );
};

export const getProductsByAuthor = (author: string): Product[] => {
  // This helper is deprecated after removing author field; return empty list
  return [];
};

export const getTotalProducts = (): number => {
  return mockProducts.length;
};

export const getTotalActiveProducts = (): number => {
  return mockProducts.filter(product => product.status === 1).length;
};

// ==================== ORDER TYPES ====================
export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  price: number;
  total: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  customerName: string;
  customerPhone: string;
  customerAddress: string;
  items: OrderItem[];
  subtotal: number;
  shippingFee: number;
  total: number;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';
  paymentMethod: 'CASH' | 'BANK_TRANSFER' | 'CREDIT_CARD';
  paymentStatus: 'PENDING' | 'PAID' | 'FAILED';
  orderDate: Date;
  deliveryDate?: Date;
  notes?: string;
}

// ==================== MOCK ORDERS ====================
export const mockOrders: Order[] = [
  {
    id: 1,
    orderNumber: 'ORD-2024-001',
    customerId: 2,
    customerName: 'Nguyễn Văn A',
    customerPhone: '0912345678',
    customerAddress: '123 Đường ABC, Quận 1, TP.HCM',
    items: [
      {
        id: 1,
        product: { ...mockProducts[0], image: ['/banner1.jpg'] },
        quantity: 2,
        price: 89000,
        total: 178000
      },
      {
        id: 2,
        product: { ...mockProducts[2], image: ['/banner2.jpg'] },
        quantity: 1,
        price: 79000,
        total: 79000
      }
    ],
    subtotal: 257000,
    shippingFee: 30000,
    total: 287000,
    status: 'DELIVERED',
    paymentMethod: 'BANK_TRANSFER',
    paymentStatus: 'PAID',
    orderDate: new Date('2024-01-15'),
    deliveryDate: new Date('2024-01-18'),
    notes: 'Giao hàng vào buổi chiều'
  },
  {
    id: 2,
    orderNumber: 'ORD-2024-002',
    customerId: 2,
    customerName: 'Nguyễn Văn A',
    customerPhone: '0912345678',
    customerAddress: '123 Đường ABC, Quận 1, TP.HCM',
    items: [
      {
        id: 3,
        product: { ...mockProducts[1], image: ['/banner3.jpg'] },
        quantity: 1,
        price: 299000,
        total: 299000
      },
      {
        id: 4,
        product: { ...mockProducts[5], image: ['/banner4.jpg'] },
        quantity: 1,
        price: 65000,
        total: 65000
      },
      {
        id: 5,
        product: { ...mockProducts[6], image: ['/banner1.jpg'] },
        quantity: 1,
        price: 199000,
        total: 199000
      }
    ],
    subtotal: 563000,
    shippingFee: 0,
    total: 563000,
    status: 'SHIPPING',
    paymentMethod: 'CREDIT_CARD',
    paymentStatus: 'PAID',
    orderDate: new Date('2024-02-20'),
    notes: 'Miễn phí vận chuyển cho đơn hàng trên 500k'
  },
  {
    id: 3,
    orderNumber: 'ORD-2024-003',
    customerId: 2,
    customerName: 'Nguyễn Văn A',
    customerPhone: '0912345678',
    customerAddress: '123 Đường ABC, Quận 1, TP.HCM',
    items: [
      {
        id: 6,
        product: { ...mockProducts[3], image: ['/banner2.jpg'] },
        quantity: 1,
        price: 450000,
        total: 450000
      }
    ],
    subtotal: 450000,
    shippingFee: 30000,
    total: 480000,
    status: 'CONFIRMED',
    paymentMethod: 'CASH',
    paymentStatus: 'PENDING',
    orderDate: new Date('2024-03-10'),
    notes: 'Thanh toán khi nhận hàng'
  },
  {
    id: 4,
    orderNumber: 'ORD-2024-004',
    customerId: 2,
    customerName: 'Nguyễn Văn A',
    customerPhone: '0912345678',
    customerAddress: '123 Đường ABC, Quận 1, TP.HCM',
    items: [
      {
        id: 7,
        product: { ...mockProducts[7], image: ['/banner3.jpg'] },
        quantity: 3,
        price: 25000,
        total: 75000
      },
      {
        id: 8,
        product: { ...mockProducts[8], image: ['/banner4.jpg'] },
        quantity: 1,
        price: 350000,
        total: 350000
      },
      {
        id: 9,
        product: { ...mockProducts[9], image: ['/banner1.jpg'] },
        quantity: 1,
        price: 180000,
        total: 180000
      }
    ],
    subtotal: 605000,
    shippingFee: 0,
    total: 605000,
    status: 'PENDING',
    paymentMethod: 'BANK_TRANSFER',
    paymentStatus: 'PENDING',
    orderDate: new Date('2024-04-05'),
    notes: 'Đơn hàng mới'
  },
  {
    id: 5,
    orderNumber: 'ORD-2024-005',
    customerId: 2,
    customerName: 'Nguyễn Văn A',
    customerPhone: '0912345678',
    customerAddress: '123 Đường ABC, Quận 1, TP.HCM',
    items: [
      {
        id: 10,
        product: { ...mockProducts[10], image: ['/banner2.jpg'] },
        quantity: 2,
        price: 45000,
        total: 90000
      },
      {
        id: 11,
        product: { ...mockProducts[11], image: ['/banner3.jpg'] },
        quantity: 1,
        price: 420000,
        total: 420000
      }
    ],
    subtotal: 510000,
    shippingFee: 30000,
    total: 540000,
    status: 'CANCELLED',
    paymentMethod: 'CREDIT_CARD',
    paymentStatus: 'FAILED',
    orderDate: new Date('2024-04-15'),
    notes: 'Hủy do lý do cá nhân'
  }
];

// ==================== ORDER HELPER FUNCTIONS ====================
export const getOrderById = (id: number): Order | undefined => {
  return mockOrders.find(order => order.id === id);
};

export const getOrdersByCustomerId = (customerId: number): Order[] => {
  return mockOrders.filter(order => order.customerId === customerId);
};

export const getOrdersByStatus = (status: Order['status']): Order[] => {
  return mockOrders.filter(order => order.status === status);
};

export const getOrdersByDateRange = (startDate: Date, endDate: Date): Order[] => {
  return mockOrders.filter(order => 
    order.orderDate >= startDate && order.orderDate <= endDate
  );
};

export const getTotalOrders = (): number => {
  return mockOrders.length;
};

export const getTotalOrdersByCustomer = (customerId: number): number => {
  return mockOrders.filter(order => order.customerId === customerId).length;
};

export const getOrderStatusText = (status: Order['status']): string => {
  switch (status) {
    case 'PENDING': return 'Chờ xác nhận';
    case 'CONFIRMED': return 'Đã xác nhận';
    case 'SHIPPING': return 'Đang giao';
    case 'DELIVERED': return 'Đã giao';
    case 'CANCELLED': return 'Đã hủy';
    default: return 'Không xác định';
  }
};

export const getPaymentStatusText = (status: Order['paymentStatus']): string => {
  switch (status) {
    case 'PENDING': return 'Chờ thanh toán';
    case 'PAID': return 'Đã thanh toán';
    case 'FAILED': return 'Thanh toán thất bại';
    default: return 'Không xác định';
  }
};

export const getPaymentMethodText = (method: Order['paymentMethod']): string => {
  switch (method) {
    case 'CASH': return 'Tiền mặt';
    case 'BANK_TRANSFER': return 'Chuyển khoản';
    case 'CREDIT_CARD': return 'Thẻ tín dụng';
    default: return 'Không xác định';
  }
};
