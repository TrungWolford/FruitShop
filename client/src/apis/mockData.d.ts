// Type declarations cho mockData.js

export const mockRoles: any[];
export const mockAccounts: any[];
export const mockCategories: any[];
export const mockProducts: any[];
export const mockProductPaginated: any;
export const mockCategoryPaginated: any;
export const mockAccountPaginated: any;
export const mockCartItems: any[];
export const mockCarts: any[];
export const mockRatings: any[];
export const mockPayments: any[];
export const mockShippings: any[];
export const mockOrders: any[];
export const mockRefunds: any[];
export const mockOrderPaginated: any;
export const mockRefundPaginated: any;
export const mockLoginResponse: any;

export function mockLogin(phone: string, password: string): any;
export function getProductById(productId: string): any;
export function getProductsByCategory(categoryId: string): any[];
export function getOrdersByAccount(accountId: string): any[];
export function getOrderById(orderId: string): any;
export function getOrdersByStatus(status: number): any[];
export function getShippingByOrder(orderId: string): any;
export function getRefundsByOrder(orderId: string): any[];
export function getOrderStatusText(status: number): string;
export function getShippingStatusText(status: number): string;
export function getPaymentStatusText(status: number): string;
