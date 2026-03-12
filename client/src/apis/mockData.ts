// ==============================
// MOCK DATA - FruitShop
// ==============================

// ------ ROLES ------
export const mockRoles = [
  { roleId: "role-001", roleName: "ADMIN" },
  { roleId: "role-002", roleName: "CUSTOMER" },
];

// ------ ACCOUNTS ------
export const mockAccounts = [
  {
    accountId: "acc-001",
    accountName: "Nguyễn Văn Admin",
    accountPhone: "0901234567",
    password: "admin123",
    status: 1,
    roles: [{ roleId: "role-001", roleName: "ADMIN" }],
  },
  {
    accountId: "acc-002",
    accountName: "Trần Thị Lan",
    accountPhone: "0912345678",
    password: "customer123",
    status: 1,
    roles: [{ roleId: "role-002", roleName: "CUSTOMER" }],
  },
  {
    accountId: "acc-003",
    accountName: "Lê Văn Bình",
    accountPhone: "0923456789",
    password: "customer123",
    status: 1,
    roles: [{ roleId: "role-002", roleName: "CUSTOMER" }],
  },
  {
    accountId: "acc-004",
    accountName: "Phạm Thị Hoa",
    accountPhone: "0934567890",
    password: "customer123",
    status: 0,
    roles: [{ roleId: "role-002", roleName: "CUSTOMER" }],
  },
];

// ------ CATEGORIES ------
export const mockCategories = [
  {
    categoryId: "cat-001",
    categoryName: "Trái cây nhập khẩu",
    description: "Các loại trái cây nhập khẩu từ nước ngoài",
    status: 1,
  },
  {
    categoryId: "cat-002",
    categoryName: "Trái cây trong nước",
    description: "Các loại trái cây trồng trong nước",
    status: 1,
  },
  {
    categoryId: "cat-003",
    categoryName: "Trái cây nhiệt đới",
    description: "Các loại trái cây vùng nhiệt đới",
    status: 1,
  },
  {
    categoryId: "cat-004",
    categoryName: "Trái cây theo mùa",
    description: "Các loại trái cây theo mùa vụ",
    status: 1,
  },
  {
    categoryId: "cat-005",
    categoryName: "Combo hộp quà",
    description: "Bộ quà tặng trái cây",
    status: 0,
  },
];

// ------ PRODUCTS ------
export const mockProducts = [
  {
    productId: "prod-001",
    productName: "Táo Fuji Nhật Bản",
    categories: [
      {
        categoryId: "cat-001",
        categoryName: "Trái cây nhập khẩu",
        description: "Các loại trái cây nhập khẩu từ nước ngoài",
        status: 1,
      },
    ],
    images: [
      {
        id: 1,
        imageUrl: "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400",
        imageOrder: 1,
      },
      {
        id: 2,
        imageUrl: "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=400",
        imageOrder: 2,
      },
    ],
    price: 85000,
    stock: 150,
    description: "Táo Fuji nhập khẩu từ Nhật Bản, vỏ đỏ bóng, vị ngọt thanh, giòn. Giàu vitamin C và chất xơ.",
    createdAt: "2024-01-15T08:00:00",
    status: 1,
  },
  {
    productId: "prod-002",
    productName: "Cam Sành Vĩnh Long",
    categories: [
      {
        categoryId: "cat-002",
        categoryName: "Trái cây trong nước",
        description: "Các loại trái cây trồng trong nước",
        status: 1,
      },
      {
        categoryId: "cat-003",
        categoryName: "Trái cây nhiệt đới",
        description: "Các loại trái cây vùng nhiệt đới",
        status: 1,
      },
    ],
    images: [
      {
        id: 3,
        imageUrl: "https://images.unsplash.com/photo-1547514701-42782101795e?w=400",
        imageOrder: 1,
      },
    ],
    price: 35000,
    stock: 300,
    description: "Cam sành Vĩnh Long tươi ngon, vỏ xanh bóng, múi vàng nước ngọt, không hạt. Bổ sung vitamin C tự nhiên.",
    createdAt: "2024-01-20T09:00:00",
    status: 1,
  },
  {
    productId: "prod-003",
    productName: "Xoài Cát Chu Đồng Tháp",
    categories: [
      {
        categoryId: "cat-002",
        categoryName: "Trái cây trong nước",
        description: "Các loại trái cây trồng trong nước",
        status: 1,
      },
      {
        categoryId: "cat-003",
        categoryName: "Trái cây nhiệt đới",
        description: "Các loại trái cây vùng nhiệt đới",
        status: 1,
      },
    ],
    images: [
      {
        id: 4,
        imageUrl: "https://images.unsplash.com/photo-1553279768-865429fa0078?w=400",
        imageOrder: 1,
      },
    ],
    price: 45000,
    stock: 200,
    description: "Xoài Cát Chu đặc sản Đồng Tháp, chín vàng thơm, thịt vàng giòn, vị ngọt đậm đà. Thu hoạch theo mùa.",
    createdAt: "2024-02-01T07:30:00",
    status: 1,
  },
  {
    productId: "prod-004",
    productName: "Nho Đỏ Mỹ",
    categories: [
      {
        categoryId: "cat-001",
        categoryName: "Trái cây nhập khẩu",
        description: "Các loại trái cây nhập khẩu từ nước ngoài",
        status: 1,
      },
    ],
    images: [
      {
        id: 5,
        imageUrl: "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400",
        imageOrder: 1,
      },
    ],
    price: 120000,
    stock: 80,
    description: "Nho đỏ nhập khẩu từ Mỹ, chùm to, quả đều, vỏ mỏng, vị ngọt đậm. Giàu chất chống oxy hóa.",
    createdAt: "2024-02-05T10:00:00",
    status: 1,
  },
  {
    productId: "prod-005",
    productName: "Dưa Hấu Hắc Mỹ Nhân",
    categories: [
      {
        categoryId: "cat-002",
        categoryName: "Trái cây trong nước",
        description: "Các loại trái cây trồng trong nước",
        status: 1,
      },
      {
        categoryId: "cat-004",
        categoryName: "Trái cây theo mùa",
        description: "Các loại trái cây theo mùa vụ",
        status: 1,
      },
    ],
    images: [
      {
        id: 6,
        imageUrl: "https://images.unsplash.com/photo-1530129558806-49f5d0e2cd53?w=400",
        imageOrder: 1,
      },
    ],
    price: 25000,
    stock: 500,
    description: "Dưa hấu Hắc Mỹ Nhân vỏ xanh đậm, ruột đỏ, không hạt, ngọt mát. Trái nặng từ 3–5 kg.",
    createdAt: "2024-02-10T08:00:00",
    status: 1,
  },
  {
    productId: "prod-006",
    productName: "Sầu Riêng Monthong Thái",
    categories: [
      {
        categoryId: "cat-001",
        categoryName: "Trái cây nhập khẩu",
        description: "Các loại trái cây nhập khẩu từ nước ngoài",
        status: 1,
      },
      {
        categoryId: "cat-003",
        categoryName: "Trái cây nhiệt đới",
        description: "Các loại trái cây vùng nhiệt đới",
        status: 1,
      },
    ],
    images: [
      {
        id: 7,
        imageUrl: "https://images.unsplash.com/photo-1609252925350-c39c74940c1e?w=400",
        imageOrder: 1,
      },
    ],
    price: 250000,
    stock: 60,
    description: "Sầu riêng Monthong Thái Lan cao cấp, cơm vàng dày, ít hạt, vị ngọt béo thơm đặc trưng.",
    createdAt: "2024-02-15T09:00:00",
    status: 1,
  },
  {
    productId: "prod-007",
    productName: "Bơ Booth Đắk Lắk",
    categories: [
      {
        categoryId: "cat-002",
        categoryName: "Trái cây trong nước",
        description: "Các loại trái cây trồng trong nước",
        status: 1,
      },
    ],
    images: [
      {
        id: 8,
        imageUrl: "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400",
        imageOrder: 1,
      },
    ],
    price: 55000,
    stock: 120,
    description: "Bơ Booth Đắk Lắk chính gốc, trái to, cơm vàng ươm béo ngậy. Thích hợp để ăn trực tiếp hoặc làm sinh tố.",
    createdAt: "2024-03-01T07:00:00",
    status: 1,
  },
  {
    productId: "prod-008",
    productName: "Dâu Tây Đà Lạt",
    categories: [
      {
        categoryId: "cat-002",
        categoryName: "Trái cây trong nước",
        description: "Các loại trái cây trồng trong nước",
        status: 1,
      },
      {
        categoryId: "cat-004",
        categoryName: "Trái cây theo mùa",
        description: "Các loại trái cây theo mùa vụ",
        status: 1,
      },
    ],
    images: [
      {
        id: 9,
        imageUrl: "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400",
        imageOrder: 1,
      },
    ],
    price: 70000,
    stock: 90,
    description: "Dâu tây Đà Lạt tươi hái ngay từ vườn, quả đỏ chín mọng, thơm ngọt chua nhẹ. Đóng hộp 500g.",
    createdAt: "2024-03-05T08:00:00",
    status: 0, // Hết mùa
  },
];

// ------ PAGINATED PRODUCT RESPONSE (mock API format) ------
export const mockProductPaginated = {
  content: mockProducts,
  totalElements: mockProducts.length,
  totalPages: Math.ceil(mockProducts.length / 5),
  size: 5,
  number: 0,
  first: true,
  last: false,
  empty: false,
};

// ------ PAGINATED CATEGORY RESPONSE ------
export const mockCategoryPaginated = {
  content: mockCategories,
  totalElements: mockCategories.length,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

// ------ PAGINATED ACCOUNT RESPONSE ------
export const mockAccountPaginated = {
  content: mockAccounts,
  totalElements: mockAccounts.length,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

// ------ CART ITEMS ------
export const mockCartItems = [
  {
    cartItemId: "ci-001",
    productId: "prod-001",
    productName: "Táo Fuji Nhật Bản",
    productPrice: 85000,
    totalPrice: 170000,
    quantity: 2,
    images: ["https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400"],
    discount: 0,
  },
  {
    cartItemId: "ci-002",
    productId: "prod-002",
    productName: "Cam Sành Vĩnh Long",
    productPrice: 35000,
    totalPrice: 105000,
    quantity: 3,
    images: ["https://images.unsplash.com/photo-1547514701-42782101795e?w=400"],
    discount: 0,
  },
  {
    cartItemId: "ci-003",
    productId: "prod-004",
    productName: "Nho Đỏ Mỹ",
    productPrice: 120000,
    totalPrice: 120000,
    quantity: 1,
    images: ["https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400"],
    discount: 0,
  },
];

// ------ CARTS ------
export const mockCarts = [
  {
    cartId: "cart-001",
    accountId: "acc-002",
    accountName: "Trần Thị Lan",
    account: {
      accountId: "acc-002",
      accountName: "Trần Thị Lan",
      accountPhone: "0912345678",
      status: 1,
    },
    items: mockCartItems,
    totalAmount: 395000,
    itemCount: 3,
    createdAt: new Date("2024-03-10T10:00:00"),
    updatedAt: new Date("2024-03-10T11:30:00"),
    status: 1,
    statusText: "Hoạt động",
  },
  {
    cartId: "cart-002",
    accountId: "acc-003",
    accountName: "Lê Văn Bình",
    account: {
      accountId: "acc-003",
      accountName: "Lê Văn Bình",
      accountPhone: "0923456789",
      status: 1,
    },
    items: [
      {
        cartItemId: "ci-004",
        productId: "prod-006",
        productName: "Sầu Riêng Monthong Thái",
        productPrice: 250000,
        totalPrice: 500000,
        quantity: 2,
        images: ["https://images.unsplash.com/photo-1609252925350-c39c74940c1e?w=400"],
        discount: 0,
      },
    ],
    totalAmount: 500000,
    itemCount: 1,
    createdAt: new Date("2024-03-11T09:00:00"),
    updatedAt: new Date("2024-03-11T09:00:00"),
    status: 1,
    statusText: "Hoạt động",
  },
];

// ------ RATINGS ------
export const mockRatings = [
  {
    ratingId: "rating-001",
    account: {
      accountId: "acc-002",
      accountName: "Trần Thị Lan",
      accountPhone: "0912345678",
    },
    product: {
      productId: "prod-001",
      productName: "Táo Fuji Nhật Bản",
    },
    orderItemId: "oi-001",
    comment: "Táo rất ngon, tươi, ngọt và giòn. Đóng gói cẩn thận, giao hàng nhanh. Sẽ mua lại!",
    ratingStar: 5,
    status: 1,
    createdAt: "2024-03-12T14:00:00",
    updatedAt: "2024-03-12T14:00:00",
  },
  {
    ratingId: "rating-002",
    account: {
      accountId: "acc-003",
      accountName: "Lê Văn Bình",
      accountPhone: "0923456789",
    },
    product: {
      productId: "prod-001",
      productName: "Táo Fuji Nhật Bản",
    },
    orderItemId: "oi-002",
    comment: "Chất lượng ổn, nhưng có một vài quả hơi nhỏ. Nhìn chung vẫn ngon.",
    ratingStar: 4,
    status: 1,
    createdAt: "2024-03-13T10:00:00",
    updatedAt: "2024-03-13T10:00:00",
  },
  {
    ratingId: "rating-003",
    account: {
      accountId: "acc-002",
      accountName: "Trần Thị Lan",
      accountPhone: "0912345678",
    },
    product: {
      productId: "prod-006",
      productName: "Sầu Riêng Monthong Thái",
    },
    orderItemId: "oi-003",
    comment: "Sầu riêng cơm dày, béo ngậy, thơm phức. Chuẩn vị Monthong xịn!",
    ratingStar: 5,
    status: 1,
    createdAt: "2024-03-14T16:00:00",
    updatedAt: "2024-03-14T16:00:00",
  },
  {
    ratingId: "rating-004",
    account: {
      accountId: "acc-003",
      accountName: "Lê Văn Bình",
      accountPhone: "0923456789",
    },
    product: {
      productId: "prod-002",
      productName: "Cam Sành Vĩnh Long",
    },
    orderItemId: "oi-004",
    comment: "Cam ngọt và nhiều nước, rất thích. Giá hợp lý.",
    ratingStar: 4,
    status: 1,
    createdAt: "2024-03-15T08:30:00",
    updatedAt: "2024-03-15T08:30:00",
  },
];

// ------ PAYMENTS (theo PaymentResponse.java) ------
export const mockPayments = [
  {
    paymentId: "pay-001",
    paymentMethod: "COD",
    paymentStatus: 1, // 0: Pending, 1: Completed, 2: Failed, 3: Refunded
    paymentDate: "2024-03-10T12:00:00",
    amount: 275000,
    transactionId: null,
  },
  {
    paymentId: "pay-002",
    paymentMethod: "BANK_TRANSFER",
    paymentStatus: 1,
    paymentDate: "2024-03-11T10:30:00",
    amount: 500000,
    transactionId: "TXN-20240311-001",
  },
  {
    paymentId: "pay-003",
    paymentMethod: "COD",
    paymentStatus: 0, // Pending
    paymentDate: "2024-03-12T08:00:00",
    amount: 370000,
    transactionId: null,
  },
  {
    paymentId: "pay-004",
    paymentMethod: "BANK_TRANSFER",
    paymentStatus: 1,
    paymentDate: "2024-03-08T14:00:00",
    amount: 170000,
    transactionId: "TXN-20240308-002",
  },
  {
    paymentId: "pay-005",
    paymentMethod: "COD",
    paymentStatus: 2, // Failed (đơn bị hủy)
    paymentDate: "2024-03-05T09:00:00",
    amount: 120000,
    transactionId: null,
  },
  {
    paymentId: "pay-006",
    paymentMethod: "COD",
    paymentStatus: 1,
    paymentDate: "2024-03-13T16:00:00",
    amount: 195000,
    transactionId: null,
  },
  {
    paymentId: "pay-007",
    paymentMethod: "BANK_TRANSFER",
    paymentStatus: 0, // Pending
    paymentDate: "2024-03-14T11:00:00",
    amount: 310000,
    transactionId: "TXN-20240314-003",
  },
];

// ------ SHIPPINGS (theo ShippingResponse.java) ------
export const mockShippings = [
  {
    shippingId: "ship-001",
    orderId: "order-001",
    accountId: "acc-002",
    receiverName: "Trần Thị Lan",
    receiverPhone: "0912345678",
    receiverAddress: "123 Nguyễn Huệ, Quận 1",
    city: "Hồ Chí Minh",
    shipperName: "Nguyễn Văn Shipper",
    shippingFee: 30000,
    shippedAt: "2024-03-11T08:00:00",
    status: 3, // 0: Đã hủy, 1: Đang chuẩn bị, 2: Đang giao, 3: Giao thành công
  },
  {
    shippingId: "ship-002",
    orderId: "order-002",
    accountId: "acc-003",
    receiverName: "Lê Văn Bình",
    receiverPhone: "0923456789",
    receiverAddress: "456 Lê Lợi, Quận 3",
    city: "Hồ Chí Minh",
    shipperName: "Trần Văn Giao",
    shippingFee: 25000,
    shippedAt: "2024-03-12T09:00:00",
    status: 3,
  },
  {
    shippingId: "ship-003",
    orderId: "order-003",
    accountId: "acc-002",
    receiverName: "Trần Thị Lan",
    receiverPhone: "0912345678",
    receiverAddress: "123 Nguyễn Huệ, Quận 1",
    city: "Hồ Chí Minh",
    shipperName: "Phạm Quốc Bảo",
    shippingFee: 30000,
    shippedAt: null,
    status: 2, // Đang giao
  },
  {
    shippingId: "ship-004",
    orderId: "order-004",
    accountId: "acc-002",
    receiverName: "Trần Thị Lan",
    receiverPhone: "0912345678",
    receiverAddress: "789 Trần Hưng Đạo, Quận 5",
    city: "Hồ Chí Minh",
    shipperName: null,
    shippingFee: 20000,
    shippedAt: null,
    status: 1, // Đang chuẩn bị
  },
  {
    shippingId: "ship-005",
    orderId: "order-005",
    accountId: "acc-003",
    receiverName: "Lê Văn Bình",
    receiverPhone: "0923456789",
    receiverAddress: "456 Lê Lợi, Quận 3",
    city: "Hồ Chí Minh",
    shipperName: null,
    shippingFee: 25000,
    shippedAt: null,
    status: 0, // Đã hủy
  },
  {
    shippingId: "ship-006",
    orderId: "order-006",
    accountId: "acc-003",
    receiverName: "Lê Văn Bình",
    receiverPhone: "0923456789",
    receiverAddress: "12 Hai Bà Trưng, Quận 1",
    city: "Hồ Chí Minh",
    shipperName: "Nguyễn Văn Shipper",
    shippingFee: 30000,
    shippedAt: "2024-03-14T10:00:00",
    status: 3,
  },
  {
    shippingId: "ship-007",
    orderId: "order-007",
    accountId: "acc-002",
    receiverName: "Trần Thị Lan",
    receiverPhone: "0912345678",
    receiverAddress: "55 Pasteur, Quận 1",
    city: "Hồ Chí Minh",
    shipperName: null,
    shippingFee: 25000,
    shippedAt: null,
    status: 1, // Đang chuẩn bị
  },
];

// ------ ORDERS (theo OrderResponse.java) ------
// Status: 1 = Chờ xác nhận, 2 = Đã xác nhận, 3 = Đang giao, 4 = Giao thành công, 0 = Đã hủy
export const mockOrders = [
  {
    orderId: "order-001",
    accountId: "acc-002",
    accountName: "Trần Thị Lan",
    createdAt: "2024-03-10T10:30:00",
    status: 4, // Giao thành công
    totalAmount: 275000,
    totalItems: 2,
    orderItems: [
      {
        orderDetailId: "oi-001",
        productId: "prod-001",
        productName: "Táo Fuji Nhật Bản",
        quantity: 2,
        unitPrice: 85000,
        totalPrice: 170000,
        productImages: ["https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-002",
        productId: "prod-002",
        productName: "Cam Sành Vĩnh Long",
        quantity: 3,
        unitPrice: 35000,
        totalPrice: 105000,
        productImages: ["https://images.unsplash.com/photo-1547514701-42782101795e?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[0],
    payment: mockPayments[0],
  },
  {
    orderId: "order-002",
    accountId: "acc-003",
    accountName: "Lê Văn Bình",
    createdAt: "2024-03-11T09:15:00",
    status: 4, // Giao thành công
    totalAmount: 500000,
    totalItems: 2,
    orderItems: [
      {
        orderDetailId: "oi-003",
        productId: "prod-006",
        productName: "Sầu Riêng Monthong Thái",
        quantity: 2,
        unitPrice: 250000,
        totalPrice: 500000,
        productImages: ["https://images.unsplash.com/photo-1609252925350-c39c74940c1e?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[1],
    payment: mockPayments[1],
  },
  {
    orderId: "order-003",
    accountId: "acc-002",
    accountName: "Trần Thị Lan",
    createdAt: "2024-03-12T07:45:00",
    status: 3, // Đang giao
    totalAmount: 370000,
    totalItems: 3,
    orderItems: [
      {
        orderDetailId: "oi-004",
        productId: "prod-004",
        productName: "Nho Đỏ Mỹ",
        quantity: 2,
        unitPrice: 120000,
        totalPrice: 240000,
        productImages: ["https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-005",
        productId: "prod-003",
        productName: "Xoài Cát Chu Đồng Tháp",
        quantity: 1,
        unitPrice: 45000,
        totalPrice: 45000,
        productImages: ["https://images.unsplash.com/photo-1553279768-865429fa0078?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-006",
        productId: "prod-001",
        productName: "Táo Fuji Nhật Bản",
        quantity: 1,
        unitPrice: 85000,
        totalPrice: 85000,
        productImages: ["https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[2],
    payment: mockPayments[2],
  },
  {
    orderId: "order-004",
    accountId: "acc-002",
    accountName: "Trần Thị Lan",
    createdAt: "2024-03-08T14:20:00",
    status: 2, // Đã xác nhận
    totalAmount: 170000,
    totalItems: 2,
    orderItems: [
      {
        orderDetailId: "oi-007",
        productId: "prod-001",
        productName: "Táo Fuji Nhật Bản",
        quantity: 2,
        unitPrice: 85000,
        totalPrice: 170000,
        productImages: ["https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[3],
    payment: mockPayments[3],
  },
  {
    orderId: "order-005",
    accountId: "acc-003",
    accountName: "Lê Văn Bình",
    createdAt: "2024-03-05T08:30:00",
    status: 0, // Đã hủy
    totalAmount: 120000,
    totalItems: 1,
    orderItems: [
      {
        orderDetailId: "oi-008",
        productId: "prod-004",
        productName: "Nho Đỏ Mỹ",
        quantity: 1,
        unitPrice: 120000,
        totalPrice: 120000,
        productImages: ["https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[4],
    payment: mockPayments[4],
  },
  {
    orderId: "order-006",
    accountId: "acc-003",
    accountName: "Lê Văn Bình",
    createdAt: "2024-03-13T15:45:00",
    status: 4, // Giao thành công
    totalAmount: 195000,
    totalItems: 2,
    orderItems: [
      {
        orderDetailId: "oi-009",
        productId: "prod-007",
        productName: "Bơ Booth Đắk Lắk",
        quantity: 1,
        unitPrice: 55000,
        totalPrice: 55000,
        productImages: ["https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-010",
        productId: "prod-005",
        productName: "Dưa Hấu Hắc Mỹ Nhân",
        quantity: 2,
        unitPrice: 25000,
        totalPrice: 50000,
        productImages: ["https://images.unsplash.com/photo-1530129558806-49f5d0e2cd53?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-011",
        productId: "prod-003",
        productName: "Xoài Cát Chu Đồng Tháp",
        quantity: 2,
        unitPrice: 45000,
        totalPrice: 90000,
        productImages: ["https://images.unsplash.com/photo-1553279768-865429fa0078?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[5],
    payment: mockPayments[5],
  },
  {
    orderId: "order-007",
    accountId: "acc-002",
    accountName: "Trần Thị Lan",
    createdAt: "2024-03-14T10:00:00",
    status: 1, // Chờ xác nhận
    totalAmount: 310000,
    totalItems: 2,
    orderItems: [
      {
        orderDetailId: "oi-012",
        productId: "prod-006",
        productName: "Sầu Riêng Monthong Thái",
        quantity: 1,
        unitPrice: 250000,
        totalPrice: 250000,
        productImages: ["https://images.unsplash.com/photo-1609252925350-c39c74940c1e?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-013",
        productId: "prod-005",
        productName: "Dưa Hấu Hắc Mỹ Nhân",
        quantity: 1,
        unitPrice: 25000,
        totalPrice: 25000,
        productImages: ["https://images.unsplash.com/photo-1530129558806-49f5d0e2cd53?w=400"],
        status: null,
      },
      {
        orderDetailId: "oi-014",
        productId: "prod-002",
        productName: "Cam Sành Vĩnh Long",
        quantity: 1,
        unitPrice: 35000,
        totalPrice: 35000,
        productImages: ["https://images.unsplash.com/photo-1547514701-42782101795e?w=400"],
        status: null,
      },
    ],
    shipping: mockShippings[6],
    payment: mockPayments[6],
  },
];

// ------ REFUNDS (theo RefundResponse.java) ------
export const mockRefunds = [
  {
    refundId: "refund-001",
    order: mockOrders[0], // order-001
    orderItem: mockOrders[0].orderItems[1], // Cam Sành - oi-002
    reason: "Cam bị héo, không tươi như quảng cáo",
    refundStatus: "Completed", // Pending, Approved, Rejected, Completed
    requestedAt: "2024-03-12T10:00:00",
    processedAt: "2024-03-13T15:00:00",
    refundAmount: 105000,
    imageUrls: [
      "https://images.unsplash.com/photo-1547514701-42782101795e?w=200",
    ],
    originalPayment: mockPayments[0],
  },
  {
    refundId: "refund-002",
    order: mockOrders[1], // order-002
    orderItem: mockOrders[1].orderItems[0], // Sầu Riêng - oi-003
    reason: "Sầu riêng bị hư, chín quá không ăn được",
    refundStatus: "Pending",
    requestedAt: "2024-03-14T08:00:00",
    processedAt: null,
    refundAmount: 250000,
    imageUrls: [
      "https://images.unsplash.com/photo-1609252925350-c39c74940c1e?w=200",
    ],
    originalPayment: mockPayments[1],
  },
  {
    refundId: "refund-003",
    order: mockOrders[5], // order-006
    orderItem: mockOrders[5].orderItems[0], // Bơ Booth - oi-009
    reason: "Bơ bị dập nhiều chỗ, chất lượng kém",
    refundStatus: "Approved",
    requestedAt: "2024-03-15T09:30:00",
    processedAt: null,
    refundAmount: 55000,
    imageUrls: [],
    originalPayment: mockPayments[5],
  },
];

// ------ PAGINATED ORDER RESPONSE ------
export const mockOrderPaginated = {
  content: mockOrders,
  totalElements: mockOrders.length,
  totalPages: Math.ceil(mockOrders.length / 10),
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

// ------ PAGINATED REFUND RESPONSE ------
export const mockRefundPaginated = {
  content: mockRefunds,
  totalElements: mockRefunds.length,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

// ------ AUTH MOCK RESPONSE ------
export const mockLoginResponse = {
  success: true,
  token: "mock-jwt-token-eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
  account: mockAccounts[0],
};

// ==============================
// HELPER FUNCTIONS
// ==============================

// ------ HELPER: tìm account theo phone + password ------
export const mockLogin = (phone, password) => {
  const found = mockAccounts.find(
    (acc) => acc.accountPhone === phone && acc.password === password
  );
  if (found) {
    return {
      success: true,
      token: "mock-jwt-token-" + found.accountId,
      account: found,
    };
  }
  return { success: false, message: "Sai số điện thoại hoặc mật khẩu" };
};

// ------ HELPER: tìm product theo ID ------
export const getProductById = (productId) =>
  mockProducts.find((p) => p.productId === productId) || null;

// ------ HELPER: tìm products theo category ------
export const getProductsByCategory = (categoryId) =>
  mockProducts.filter((p) =>
    p.categories.some((c) => c.categoryId === categoryId)
  );

// ------ HELPER: tìm orders theo accountId ------
export const getOrdersByAccount = (accountId) =>
  mockOrders.filter((o) => o.accountId === accountId);

// ------ HELPER: tìm order theo orderId ------
export const getOrderById = (orderId) =>
  mockOrders.find((o) => o.orderId === orderId) || null;

// ------ HELPER: tìm orders theo status ------
export const getOrdersByStatus = (status) =>
  mockOrders.filter((o) => o.status === status);

// ------ HELPER: tìm shipping theo orderId ------
export const getShippingByOrder = (orderId) =>
  mockShippings.find((s) => s.orderId === orderId) || null;

// ------ HELPER: tìm refunds theo orderId ------
export const getRefundsByOrder = (orderId) =>
  mockRefunds.filter((r) => r.order.orderId === orderId);

// ------ HELPER: Map order status number to text ------
export const getOrderStatusText = (status) => {
  const statusMap = {
    0: "Đã hủy",
    1: "Chờ xác nhận",
    2: "Đã xác nhận",
    3: "Đang giao",
    4: "Giao thành công",
  };
  return statusMap[status] || "Không xác định";
};

// ------ HELPER: Map shipping status number to text ------
export const getShippingStatusText = (status) => {
  const statusMap = {
    0: "Đã hủy",
    1: "Đang chuẩn bị",
    2: "Đang giao",
    3: "Giao thành công",
  };
  return statusMap[status] || "Không xác định";
};

// ------ HELPER: Map payment status number to text ------
export const getPaymentStatusText = (status) => {
  const statusMap = {
    0: "Đang chờ",
    1: "Đã thanh toán",
    2: "Thất bại",
    3: "Đã hoàn tiền",
  };
  return statusMap[status] || "Không xác định";
};
