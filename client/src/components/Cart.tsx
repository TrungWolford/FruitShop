// src/components/Cart.tsx
import React, { useState, useEffect } from 'react';
import { X, ShoppingCart, Package, RefreshCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from './ui/Button/Button';
import { useAppSelector } from '../hooks/redux';
import { cartService } from '../services/cartService';
import { localStorageCartService } from '../services/localStorageCartService';
import { toast } from 'sonner';
import CartItem from './CartItem';
import type { CartItem as CartItemType } from '../types/cart';

interface CartProps {
  isOpen: boolean;
  onClose: () => void;
}

const Cart: React.FC<CartProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [cartItems, setCartItems] = useState<CartItemType[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdating, setIsUpdating] = useState<string | null>(null);

  // Fetch cart items when cart opens
  useEffect(() => {
    if (isOpen) {
      fetchCartItems();
    }
  }, [isOpen, isAuthenticated, user]);

  // Listen for cart updates
  useEffect(() => {
    const handleCartUpdate = () => {
      fetchCartItems();
    };

    window.addEventListener('cartUpdated', handleCartUpdate);
    return () => window.removeEventListener('cartUpdated', handleCartUpdate);
  }, [isAuthenticated, user]);

  const fetchCartItems = async () => {
    setIsLoading(true);
    try {
      if (isAuthenticated && user) {
        // Lấy từ API nếu đã đăng nhập
        const response = await cartService.getCartItems(user.accountId);
        if (response.success && response.data) {
          let items: CartItemType[] = [];
          if (Array.isArray(response.data)) {
            items = response.data;
          } else if (typeof response.data === 'object' && 'items' in response.data) {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            items = (response.data as any).items || [];
          }
          setCartItems(items);
        }
      } else {
        // Lấy từ localStorage nếu chưa đăng nhập
        const localCart = localStorageCartService.getCartItems();
        setCartItems(localCart);
      }
      // eslint-disable-next-line no-unused-vars, @typescript-eslint/no-unused-vars
    } catch (error) {
      setCartItems([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateQuantity = async (cartItemId: string, quantity: number) => {
    if (quantity <= 0) {
      toast.error('Số lượng phải lớn hơn 0');
      return;
    }

    setIsUpdating(cartItemId);
    try {
      if (isAuthenticated && user) {
        // Cập nhật qua API
        const response = await cartService.updateCartItem({
          cartItemId,
          quantity,
        });

        if (response.success) {
          await fetchCartItems();
          toast.success('Đã cập nhật số lượng sản phẩm');
          window.dispatchEvent(new CustomEvent('cartUpdated'));
        } else {
          toast.error(response.message || 'Không thể cập nhật số lượng');
        }
      } else {
        // Cập nhật localStorage
        const updatedCart = localStorageCartService.updateQuantity(cartItemId, quantity);
        setCartItems(updatedCart);
        toast.success('Đã cập nhật số lượng sản phẩm');
      }
    } catch (error) {
      console.error('Error updating cart item:', error);
      toast.error('Đã xảy ra lỗi khi cập nhật');
    } finally {
      setIsUpdating(null);
    }
  };

  const handleRemoveItem = async (cartItemId: string) => {
    setIsUpdating(cartItemId);
    try {
      if (isAuthenticated && user) {
        // Xóa qua API
        const response = await cartService.removeFromCart(cartItemId);
        if (response.success) {
          await fetchCartItems();
          toast.success('Đã xóa sản phẩm khỏi giỏ hàng');
          window.dispatchEvent(new CustomEvent('cartUpdated'));
        } else {
          toast.error(response.message || 'Không thể xóa sản phẩm');
        }
      } else {
        // Xóa từ localStorage
        const updatedCart = localStorageCartService.removeItem(cartItemId);
        setCartItems(updatedCart);
        toast.success('Đã xóa sản phẩm khỏi giỏ hàng');
      }
    } catch (error) {
      console.error('Error removing cart item:', error);
      toast.error('Đã xảy ra lỗi khi xóa sản phẩm');
    } finally {
      setIsUpdating(null);
    }
  };

  const totalAmount = cartItems.reduce((total, item) => total + item.productPrice * item.quantity, 0);
  const itemCount = cartItems.reduce((count, item) => count + item.quantity, 0);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  if (!isOpen) return null;

  return (
    <>
      {/* Overlay */}
      <div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40 transition-all duration-300" onClick={onClose} />

      {/* Cart Dialog */}
      <div className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[650px] h-[85vh] bg-white rounded-lg shadow-2xl z-50 border border-gray-100 flex flex-col">
        {/* Header */}
        <div className="relative px-6 py-4 bg-gradient-to-br from-blue-600 via-blue-700 to-blue-800 rounded-t-lg flex-shrink-0 shadow-lg">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-white/25 rounded-full flex items-center justify-center shadow-inner">
                <ShoppingCart className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-2xl font-bold text-white mb-1">Giỏ hàng</h3>
                <p className="text-white/80 text-sm">
                  {isAuthenticated ? 'Quản lý sản phẩm của bạn' : 'Giỏ hàng tạm thời'}
                  <span className="ml-2 text-red-300 font-semibold">({itemCount} sản phẩm)</span>
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <button
                onClick={fetchCartItems}
                disabled={isLoading}
                className="w-9 h-9 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-all duration-200 group disabled:opacity-50 hover:scale-105"
              >
                <RefreshCcw className={`w-4 h-4 text-white ${isLoading ? 'animate-spin' : 'group-hover:rotate-180'}`} />
              </button>
              <button
                onClick={onClose}
                className="w-9 h-9 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-all duration-200 group hover:scale-105"
              >
                <X className="w-5 h-5 text-white group-hover:scale-110" />
              </button>
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-hidden flex flex-col">
          {isLoading ? (
            <div className="flex-1 flex items-center justify-center">
              <RefreshCcw className="w-5 h-5 animate-spin text-gray-500 mr-3" />
              <span className="text-gray-500">Đang tải giỏ hàng...</span>
            </div>
          ) : cartItems.length === 0 ? (
            <div className="flex-1 flex items-center justify-center">
              <div className="text-center">
                <Package className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h4 className="text-xl font-semibold text-gray-700 mb-2">Giỏ hàng trống</h4>
                <p className="text-gray-500">Bạn chưa có sản phẩm nào trong giỏ hàng</p>
              </div>
            </div>
          ) : (
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {cartItems.map((item) => (
                <CartItem
                  key={item.cartItemId}
                  item={item}
                  onUpdateQuantity={handleUpdateQuantity}
                  onRemoveItem={handleRemoveItem}
                  isUpdating={isUpdating === item.cartItemId}
                />
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        {cartItems.length > 0 && (
          <div className="px-6 py-4 border-t bg-gray-50 rounded-b-lg flex-shrink-0">
            <div className="flex justify-between items-center mb-4">
              <span className="text-lg font-semibold">Tổng tiền:</span>
              <span className="text-2xl font-bold text-red-600">{formatPrice(totalAmount)}</span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <Button onClick={onClose} className="border border-gray-300 bg-white text-gray-700">
                Tiếp tục mua
              </Button>
              <Button
                onClick={() => {
                  onClose();
                  navigate('/checkout');
                }}
                className="bg-blue-600 text-white"
              >
                Thanh toán
              </Button>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default Cart;
