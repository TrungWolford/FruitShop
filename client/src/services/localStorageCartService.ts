// src/services/localStorageCartService.ts
import type { CartItem } from '../types/cart';

const CART_STORAGE_KEY = 'shopping_cart';

export const localStorageCartService = {
    // Lấy tất cả items từ localStorage
    getCartItems: (): CartItem[] => {
        try {
            const cartData = localStorage.getItem(CART_STORAGE_KEY);
            return cartData ? JSON.parse(cartData) : [];
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (error) {
            return [];
        }
    },

    // Thêm sản phẩm vào giỏ hàng
    addToCart: (product: {
    productId: string;
    productName: string;
    productPrice: number;
    images: string[];
    quantity?: number;
}): CartItem[] => {
    try {
        const cart = localStorageCartService.getCartItems();
        const existingItemIndex = cart.findIndex((item) => item.productId === product.productId);

        if (existingItemIndex > -1) {
            // Sản phẩm đã tồn tại, tăng số lượng
            cart[existingItemIndex].quantity += product.quantity || 1;
            // Cập nhật totalPrice
            cart[existingItemIndex].totalPrice = cart[existingItemIndex].quantity * cart[existingItemIndex].productPrice;
        } else {
            // Thêm sản phẩm mới
            const quantity = product.quantity || 1;
            const newItem: CartItem = {
                cartItemId: `local_${Date.now()}_${product.productId}`,
                productId: product.productId,
                productName: product.productName,
                productPrice: product.productPrice,
                images: product.images,
                quantity: quantity,
                totalPrice: product.productPrice * quantity // Thêm totalPrice
            };
            cart.push(newItem);
        }

        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));

        // Dispatch event để cập nhật UI
        window.dispatchEvent(new CustomEvent('cartUpdated'));

        return cart;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {

        return [];
    }
},

// Cập nhật số lượng
updateQuantity: (cartItemId: string, quantity: number): CartItem[] => {
    try {
        const cart = localStorageCartService.getCartItems();
        const itemIndex = cart.findIndex((item) => item.cartItemId === cartItemId);

        if (itemIndex > -1) {
            if (quantity <= 0) {
                cart.splice(itemIndex, 1);
            } else {
                cart[itemIndex].quantity = quantity;
                // Cập nhật totalPrice khi thay đổi quantity
                cart[itemIndex].totalPrice = quantity * cart[itemIndex].productPrice;
            }
        }

        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
        window.dispatchEvent(new CustomEvent('cartUpdated'));

        return cart;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
        return [];
    }
},

    // Xóa sản phẩm
    removeItem: (cartItemId: string): CartItem[] => {
        try {
            let cart = localStorageCartService.getCartItems();
            cart = cart.filter((item) => item.cartItemId !== cartItemId);

            localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
            window.dispatchEvent(new CustomEvent('cartUpdated'));

            return cart;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (error) {
            return [];
        }
    },

    // Xóa toàn bộ giỏ hàng
    clearCart: (): void => {
        localStorage.removeItem(CART_STORAGE_KEY);
        window.dispatchEvent(new CustomEvent('cartUpdated'));
    },

    // Đếm số lượng items
    getItemCount: (): number => {
        const cart = localStorageCartService.getCartItems();
        return cart.reduce((total, item) => total + item.quantity, 0);
    },

    // Tính tổng tiền
    getTotalAmount: (): number => {
        const cart = localStorageCartService.getCartItems();
        return cart.reduce((total, item) => total + item.productPrice * item.quantity, 0);
    }
};