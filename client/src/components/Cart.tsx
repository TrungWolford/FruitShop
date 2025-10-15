import React, { useState, useEffect } from 'react';
import { X, ShoppingCart, Package, RefreshCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from './ui/button';
import { useAppSelector } from '../hooks/redux';
import { cartService } from '../services/cartService';
import { toast } from 'sonner';
import CartItem from './CartItem';
import type { CartItem as CartItemType } from '../types/cart';

type CartItemData = CartItemType;

interface CartProps {
  isOpen: boolean;
  onClose: () => void;
}

const Cart: React.FC<CartProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [cartItems, setCartItems] = useState<CartItemData[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdating, setIsUpdating] = useState<string | null>(null);

  // Fetch cart items when cart opens
  useEffect(() => {
    if (isOpen && isAuthenticated && user) {
      fetchCartItems();
    }
  }, [isOpen, isAuthenticated, user]);

  // Listen for cart updates from other components
  useEffect(() => {
    const handleCartUpdate = () => {
      console.log('🔄 Cart: Received cartUpdated event, isOpen:', isOpen, 'isAuthenticated:', isAuthenticated);
      if (isAuthenticated && user) {
        console.log('🔄 Cart: Fetching cart items due to cartUpdated event');
        if (isOpen) {
          fetchCartItems();
        }
        // Always clear cart items when cart is updated (for case of clear cart)
        setCartItems([]);
      }
    };

    const handleCartItemsUpdate = (event: CustomEvent) => {
      console.log('🔄 Cart: Received cartItemsUpdated event:', event.detail, 'isOpen:', isOpen);
      if (isAuthenticated && user) {
        console.log('🔄 Cart items updated event received:', event.detail);
        // Cập nhật state ngay lập tức nếu có data
        if (event.detail && Array.isArray(event.detail.items)) {
          console.log('🔄 Cart: Setting cart items to:', event.detail.items);
          setCartItems(event.detail.items);
        }
      }
    };

    window.addEventListener('cartUpdated', handleCartUpdate);
    window.addEventListener('cartItemsUpdated', handleCartItemsUpdate as EventListener);
    
    return () => {
      window.removeEventListener('cartUpdated', handleCartUpdate);
      window.removeEventListener('cartItemsUpdated', handleCartItemsUpdate as EventListener);
    };
  }, [isOpen, isAuthenticated, user]);

  const fetchCartItems = async () => {
    if (!user) return;
    
    setIsLoading(true);
    try {
      console.log('🔄 Fetching cart items for user:', user.accountId);
      const response = await cartService.getCartItems(user.accountId);
      
      console.log('📋 Fetch response:', response);
      
      if (response.success && response.data) {
        console.log('✅ Cart items fetched:', response.data);
        
        // Handle different response data types
        let items: CartItemData[] = [];
        
        if (Array.isArray(response.data)) {
          items = response.data;
        } else if (typeof response.data === 'object' && 'items' in response.data) {
          // If it's a Cart object with items array
          items = (response.data as any).items || [];
        } else if (typeof response.data === 'object' && 'cartItems' in response.data) {
          // Alternative field name
          items = (response.data as any).cartItems || [];
        } else {
          // Single item or other format
          items = [response.data as any];
        }
        
        console.log('📦 Processed items:', items);
        setCartItems(items);
        
        // Dispatch event để cập nhật UI ở nơi khác
        window.dispatchEvent(new CustomEvent('cartItemsUpdated', { 
          detail: { items, count: items.length } 
        }));
      } else {
        console.error('❌ Failed to fetch cart items:', response);
        setCartItems([]);
      }
    } catch (error) {
      console.error('💥 Error fetching cart items:', error);
      setCartItems([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateQuantity = async (cartItemId: string, quantity: number) => {
    // Validation: quantity phải > 0
    if (quantity <= 0) {
      toast.error("Số lượng phải lớn hơn 0");
      return;
    }
    
    // Tìm item hiện tại để so sánh
    const currentItem = cartItems.find(item => item.cartItemId === cartItemId);
    if (currentItem && currentItem.quantity === quantity) {
      console.log('🔄 Quantity unchanged, skipping update');
      return;
    }
    
    console.log('🔄 Starting quantity update...');
    console.log('📋 Current item:', currentItem);
    console.log('📋 New quantity:', quantity);
    
    setIsUpdating(cartItemId);
    try {
      console.log('🔄 Calling cartService.updateCartItem...');
      
      const response = await cartService.updateCartItem({
        cartItemId,
        quantity
      });
      
      console.log('📋 Update response:', response);
      
      if (response.success) {
        console.log('✅ Update successful, updating local state...');
        
        // Cập nhật state ngay lập tức để UI responsive
        setCartItems(prevItems => {
          const updatedItems = prevItems.map(item => 
            item.cartItemId === cartItemId 
              ? { ...item, quantity } 
              : item
          );
          console.log('📦 Updated items:', updatedItems);
          return updatedItems;
        });
        
        // Refresh cart items để đảm bảo data đồng bộ
        console.log('🔄 Refreshing cart items...');
        await fetchCartItems();
        
        toast.success("Đã cập nhật số lượng sản phẩm");
        
        // Dispatch event để các component khác biết giỏ hàng đã thay đổi
        window.dispatchEvent(new CustomEvent('cartUpdated'));
      } else {
        console.error('❌ Update failed:', response.message);
        toast.error(response.message || "Không thể cập nhật số lượng");
      }
    } catch (error) {
      console.error('💥 Error updating cart item:', error);
      toast.error("Đã xảy ra lỗi khi cập nhật");
    } finally {
      setIsUpdating(null);
    }
  };

  const handleRemoveItem = async (cartItemId: string) => {
    setIsUpdating(cartItemId);
    try {
      console.log('🔄 Removing cart item:', cartItemId);
      const response = await cartService.removeFromCart(cartItemId);
      
      console.log('📋 Remove response:', response);
      
      if (response.success) {
        // Refresh cart items ngay lập tức
        console.log('🔄 Refreshing cart after successful removal...');
        await fetchCartItems();
        
        // Hiển thị thông báo thành công
        toast.success("Đã xóa sản phẩm khỏi giỏ hàng");
        
        // Dispatch event để các component khác biết giỏ hàng đã thay đổi
        window.dispatchEvent(new CustomEvent('cartUpdated'));
      } else {
        console.error('❌ Remove failed:', response.message);
        toast.error(response.message || "Không thể xóa sản phẩm");
      }
    } catch (error) {
      console.error('💥 Error removing cart item:', error);
      toast.error("Đã xảy ra lỗi khi xóa sản phẩm");
    } finally {
      setIsUpdating(null);
    }
  };

  const totalAmount = cartItems.reduce((total, item) => total + (item.productPrice * item.quantity), 0);
  const itemCount = cartItems.reduce((count, item) => count + item.quantity, 0);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  if (!isOpen) return null;

  return (
    <>
      {/* Overlay with backdrop blur */}
      <div 
        className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40 transition-all duration-300"
        onClick={onClose}
      />
      
      {/* Cart Dialog with animation */}
      <div className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[650px] h-[85vh] bg-white rounded-lg shadow-2xl z-50 border border-gray-100 transition-all duration-300 animate-in fade-in-0 zoom-in-95 slide-in-from-bottom-2 flex flex-col">
        {/* Header with enhanced gradient */}
        <div className="relative px-6 py-4 bg-gradient-to-br from-blue-600 via-blue-700 to-blue-800 rounded-t-lg flex-shrink-0 shadow-lg">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-white/25 rounded-full flex items-center justify-center shadow-inner">
                <ShoppingCart className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-2xl font-bold text-white mb-1">Giỏ hàng</h3>
                <p className="text-white/80 text-sm">
                  Quản lý sản phẩm của bạn
                  <span className="ml-2 text-red-300 font-semibold">
                    ({itemCount} sản phẩm)
                  </span>
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              {/* Refresh button */}
              <button
                onClick={fetchCartItems}
                disabled={isLoading}
                className="w-9 h-9 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-all duration-200 group disabled:opacity-50 hover:scale-105"
                title="Làm mới giỏ hàng"
              >
                <RefreshCcw className={`w-4 h-4 text-white transition-transform ${isLoading ? 'animate-spin' : 'group-hover:rotate-180'}`} />
              </button>
              {/* Close button */}
              <button
                onClick={onClose}
                className="w-9 h-9 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition-all duration-200 group hover:scale-105"
                title="Đóng giỏ hàng"
              >
                <X className="w-5 h-5 text-white group-hover:scale-110 transition-transform" />
              </button>
            </div>
          </div>
          

        </div>

        {/* Cart Content */}
        <div className="flex-1 overflow-hidden flex flex-col">
          {isLoading ? (
            <div className="flex-1 flex items-center justify-center py-12">
              <div className="flex items-center gap-3 text-gray-500">
                <RefreshCcw className="w-5 h-5 animate-spin" />
                <span>Đang tải giỏ hàng...</span>
              </div>
            </div>
          ) : cartItems.length === 0 ? (
            <div className="flex-1 flex items-center justify-center py-12">
              <div className="text-center">
                <div className="w-24 h-24 mx-auto mb-6 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center">
                  <Package className="w-12 h-12 text-gray-400" />
                </div>
                <h4 className="text-xl font-semibold text-gray-700 mb-2">Giỏ hàng trống</h4>
                <p className="text-gray-500 mb-6">Bạn chưa có sản phẩm nào trong giỏ hàng</p>
                
                {/* Decorative elements */}
                <div className="flex items-center justify-center gap-2">
                  <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse"></div>
                  <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse" style={{animationDelay: '0.2s'}}></div>
                  <div className="w-2 h-2 bg-red-400 rounded-full animate-pulse" style={{animationDelay: '0.4s'}}></div>
                </div>
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

        {/* Footer with total and checkout */}
        {cartItems.length > 0 && (
          <div className="px-6 py-4 border-t border-gray-100 bg-gray-50 rounded-b-lg flex-shrink-0">
            {/* Cart Summary */}
            <div className="mb-4">
              <div className="border-t border-gray-200 pt-3">
                <div className="flex items-center justify-between">
                  <span className="text-lg font-semibold text-gray-800">Tổng tiền:</span>
                  <span className="text-2xl font-bold text-red-600">
                    {formatPrice(totalAmount)}
                  </span>
                </div>
              </div>
            </div>

            {/* Action buttons */}
            <div className="grid grid-cols-2 gap-3">
              <Button
                onClick={onClose}
                className="font-semibold py-3 text-base border border-gray-300 bg-white text-gray-700 hover:bg-gray-50"
              >
                Tiếp tục mua
              </Button>
              <Button
                onClick={() => {
                  onClose(); // Close cart modal
                  navigate('/checkout'); // Navigate to checkout page
                }}
                className="bg-gradient-to-r from-blue-600 to-blue-700 text-white font-semibold py-3 text-base rounded-xl shadow-lg transition-all duration-200"
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
