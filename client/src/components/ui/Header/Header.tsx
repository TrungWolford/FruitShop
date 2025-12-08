import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, User, ShoppingCart, Phone, LogOut, UserCircle, History, Settings, X } from 'lucide-react';
import { Button } from '../Button/Button';
import { Input } from '../input';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../dropdowns/dropdown-menu';
import { HoverCard, HoverCardContent, HoverCardTrigger } from '../dropdowns/hover';

import { useAppSelector, useAppDispatch } from '../../../hooks/redux';
import { logout } from '../../../store/slices/authSlice';
import LoginDialog from '../../../pages/Mainpage/Login';
import Cart from '../../Cart/Cart';
import { cartService } from '../../../services/cartService';
import { productService } from '../../../services/productService';
import { toast } from 'sonner';
import { imgaes } from '../../../assets/img';
import { localStorageCartService } from '@/services/localStorageCartService';
import { authService } from '@/services/authService';
import type { CartItem } from '@/types/cart';

const TopNavigation: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const [displayUser, setDisplayUser] = useState(user);
  const [isLoginDialogOpen, setIsLoginDialogOpen] = useState(false);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [cartItemCount, setCartItemCount] = useState(0);
  const [hoverCartItems, setHoverCartItems] = useState<CartItem[]>([]);
  const [hasLoadedHoverCart, setHasLoadedHoverCart] = useState(false);
  const [cartStatus, setCartStatus] = useState<number | null>(null);

  // Debug: Log khi user state thay đổi
  useEffect(() => {
    setDisplayUser(user);
  }, [isAuthenticated, user]);

  // Nghe event authUpdated để đồng bộ user từ localStorage khi có login/refresh token
  useEffect(() => {
    const handleAuthUpdate = () => {
      const storedUser = authService.loadUserFromStorage();
      if (storedUser) {
        setDisplayUser(storedUser);
      }
    };

    window.addEventListener('authUpdated', handleAuthUpdate);
    return () => window.removeEventListener('authUpdated', handleAuthUpdate);
  }, []);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  // Handle search
  const handleSearch = async () => {
    
    if (!searchQuery.trim()) {
      toast.error('Vui lòng nhập từ khóa tìm kiếm');
      return;
    }

    try {
      const response = await productService.searchProducts(searchQuery, 0, 12, undefined, undefined, 1);
      // Response trực tiếp có content, totalElements (không có success, data wrapper)
      if (response && response.content) {
        toast.success(`Tìm thấy ${response.totalElements || 0} sản phẩm`);
        
        // Navigate to search results page
        navigate(`/product/search?q=${encodeURIComponent(searchQuery)}`);
      } else {
        toast.error('Không tìm thấy sản phẩm nào');
      }
    } catch {
      toast.error('Đã xảy ra lỗi khi tìm kiếm');
    }
  };

  // Handle search on Enter key
  const handleSearchKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // Helper function to check authentication and show login message
  const requireAuth = (action: string) => {
    if (!isAuthenticated || !user) {
      toast.error(`Vui lòng đăng nhập để ${action}`);
      setIsLoginDialogOpen(true);
      return false;
    }
    return true;
  };

  // Fetch cart items count when user is authenticated
  useEffect(() => {
    const updateCartCount = () => {
      if (isAuthenticated && user) {
        fetchCartItemsCount();
      } else {
        // Lấy từ localStorage
        const count = localStorageCartService.getItemCount();
        setCartItemCount(count);
      }
    };

    updateCartCount();

    window.addEventListener('cartUpdated', updateCartCount);
    return () => window.removeEventListener('cartUpdated', updateCartCount);
  }, [isAuthenticated, user]);

  // Listen for cart updates
  useEffect(() => {
    const handleCartUpdate = () => {
      if (isAuthenticated && user) {
        fetchCartItemsCount();
      } else {
        const count = localStorageCartService.getItemCount();
        setCartItemCount(count);
      }
      // Reset flag để fetch lại khi cart update
      setHasLoadedHoverCart(false);
    };

    const handleCloseCartModal = () => {
      setIsCartOpen(false);
    };

    const handleOpenLoginDialog = () => {
      setIsLoginDialogOpen(true);
    };

    const handleAuthUpdate = () => {
      // Force component to recognize auth state change
      if (isAuthenticated && user) {
        fetchCartItemsCount();
      }
    };

    window.addEventListener('cartUpdated', handleCartUpdate);
    window.addEventListener('closeCartModal', handleCloseCartModal);
    window.addEventListener('openLoginDialog', handleOpenLoginDialog);
    window.addEventListener('authUpdated', handleAuthUpdate);

    return () => {
      window.removeEventListener('cartUpdated', handleCartUpdate);
      window.removeEventListener('closeCartModal', handleCloseCartModal);
      window.removeEventListener('openLoginDialog', handleOpenLoginDialog);
      window.removeEventListener('authUpdated', handleAuthUpdate);
    };
  }, [isAuthenticated, user]);

  const fetchCartItemsCount = async () => {
    if (!user) return;

    try {
      const response = await cartService.getCartItems(user.accountId);
      if (response.success && response.data) {
        let count = 0;
        let status: number | null = null;
        
        if (Array.isArray(response.data)) {
          count = response.data.reduce((total, item) => total + item.quantity, 0);
        } else if (typeof response.data === 'object' && 'items' in response.data) {
          const cartData = response.data as { items?: CartItem[]; status?: number };
          count = cartData.items?.reduce((total: number, item: CartItem) => total + item.quantity, 0) || 0;
          status = cartData.status ?? null;
        }
        
        setCartItemCount(count);
        setCartStatus(status);
      }
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
      setCartItemCount(0);
      setCartStatus(null);
    }
  };

  // Fetch cart items for hover preview (chỉ lấy 3 items đầu)
  const fetchHoverCartItems = async () => {
    // Chỉ fetch nếu chưa load hoặc cart vừa update
    if (hasLoadedHoverCart && hoverCartItems.length > 0) return;
    
    try {
      if (isAuthenticated && user) {
        const response = await cartService.getCartItems(user.accountId);
        if (response.success && response.data) {
          let items: CartItem[] = [];
          if (Array.isArray(response.data)) {
            items = response.data;
          } else if (typeof response.data === 'object' && 'items' in response.data) {
            items = (response.data as { items: CartItem[] }).items || [];
          }
          setHoverCartItems(items.slice(0, 3));
          setHasLoadedHoverCart(true);
        }
      } else {
        const localCart = localStorageCartService.getCartItems();
        setHoverCartItems(localCart.slice(0, 3));
        setHasLoadedHoverCart(true);
      }
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
      setHoverCartItems([]);
    }
  };

  // Helper function to get correct image URL
  const getImageUrl = (item: CartItem): string => {
    // Lấy image đầu tiên từ images array
    const imageUrl = item.images?.[0];
    
    if (!imageUrl) {
      return '/placeholder-image.svg';
    }
    
    // If already a full URL
    if (imageUrl.startsWith('http')) {
      return imageUrl;
    }
    
    // If already starts with /products/
    if (imageUrl.startsWith('/products/')) {
      return imageUrl;
    }
    
    // Clean and add /products/ prefix
    const cleanUrl = imageUrl.startsWith('/') ? imageUrl.slice(1) : imageUrl;
    return `/products/${cleanUrl}`;
  };

  const handleCartClick = () => {
    // Check if cart is disabled
    if (isAuthenticated && user && cartStatus !== null && cartStatus !== 1) {
      toast.error('Giỏ hàng đã bị vô hiệu hóa do vi phạm chính sách, vui lòng liên hệ VuaTraiCay để biết thêm chi tiết');
      return;
    }
    setIsCartOpen(true);
  };

  const handleUpdateQuantity = async (cartItemId: string, currentQuantity: number, delta: number) => {
    const newQuantity = currentQuantity + delta;
    if (newQuantity <= 0) return;

    try {
      if (isAuthenticated && user) {
        // Tìm item để lấy productId
        const item = hoverCartItems.find(i => i.cartItemId === cartItemId);
        if (!item) {
          toast.error('Không tìm thấy sản phẩm trong giỏ hàng');
          return;
        }

        // Lấy thông tin sản phẩm để kiểm tra stock
        const productResponse = await productService.getProductById(item.productId);
        if (productResponse.success && productResponse.data) {
          const product = productResponse.data;
          
          // Kiểm tra stock
          if (newQuantity > product.stock) {
            toast.error(`Sản phẩm này chỉ còn ${product.stock} sản phẩm trong kho`);
            return;
          }
        }

        const response = await cartService.updateCartItem({
          cartItemId,
          quantity: newQuantity,
        });
        if (response.success) {
          // Cập nhật trực tiếp hoverCartItems để UI phản hồi ngay
          setHoverCartItems(prev => 
            prev.map(item => 
              item.cartItemId === cartItemId 
                ? { ...item, quantity: newQuantity } 
                : item
            )
          );
          window.dispatchEvent(new CustomEvent('cartUpdated'));
        }
      } else {
        // Guest user - kiểm tra stock từ localStorage cart
        const item = hoverCartItems.find(i => i.cartItemId === cartItemId);
        if (!item) {
          toast.error('Không tìm thấy sản phẩm trong giỏ hàng');
          return;
        }

        // Lấy thông tin sản phẩm để kiểm tra stock
        const productResponse = await productService.getProductById(item.productId);
        if (productResponse.success && productResponse.data) {
          const product = productResponse.data;
          
          // Kiểm tra stock
          if (newQuantity > product.stock) {
            toast.error(`Sản phẩm này chỉ còn ${product.stock} sản phẩm trong kho`);
            return;
          }
        }

        localStorageCartService.updateQuantity(cartItemId, newQuantity);
        // Cập nhật trực tiếp hoverCartItems để UI phản hồi ngay
        setHoverCartItems(prev => 
          prev.map(item => 
            item.cartItemId === cartItemId 
              ? { ...item, quantity: newQuantity } 
              : item
          )
        );
        window.dispatchEvent(new CustomEvent('cartUpdated'));
      }
    } catch {
      toast.error('Không thể cập nhật số lượng');
    }
  };

  const handleRemoveItem = async (cartItemId: string) => {
    try {
      if (isAuthenticated && user) {
        const response = await cartService.removeFromCart(cartItemId);
        if (response.success) {
          // Xóa item khỏi hoverCartItems ngay lập tức
          setHoverCartItems(prev => prev.filter(item => item.cartItemId !== cartItemId));
          toast.success('Đã xóa sản phẩm');
          window.dispatchEvent(new CustomEvent('cartUpdated'));
        }
      } else {
        localStorageCartService.removeItem(cartItemId);
        // Xóa item khỏi hoverCartItems ngay lập tức
        setHoverCartItems(prev => prev.filter(item => item.cartItemId !== cartItemId));
        toast.success('Đã xóa sản phẩm');
        window.dispatchEvent(new CustomEvent('cartUpdated'));
      }
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
      toast.error('Không thể xóa sản phẩm');
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  return (
    <div className="bg-primary text-white">
      {/* Top bar with hotline */}
      <div className="bg-[#F38258] text-white px-4 py-1 text-sm">
        <div className="container mx-auto flex items-center">
          <Phone className="w-4 h-4 mr-2" />
          <span className="font-medium">Hotline: 0903.400.028</span>
          <span className="ml-2 text-white-700">(8h - 12h, 13h30 - 17h)</span>
          <span className="ml-auto">Liên hệ hợp tác</span>
        </div>
      </div>

      {/* Main navigation */}
      <div className="container h-[75px]  mx-auto px-[70px] py-3 flex justify-between">
        <div className="flex items-center justify-between gap-4">
          {/* Logo */}
          <button
            onClick={() => navigate('/')}
            className="flex items-center hover:opacity-80 transition-opacity cursor-pointer"
          >
            <div className=" px-3 py-2 rounded-md mr-3">
              <img src={imgaes.logo} alt="" className="w-15 h-16 bg-contain" />
            </div>
          </button>

          {/* Search bar */}
          <div className="flex-1 w-[500px] max-w-2xl mx-8">
            <div className="relative">
              <Input
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                className="w-full pl-4 pr-12 py-2 rounded border-0 bg-white text-black"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={handleSearchKeyPress}
              />
              <Button
                size="sm"
                className="absolute right-1 top-1/2 transform -translate-y-1/2 bg-primary hover:bg-[#F38258] text-white px-4 rounded"
                onClick={handleSearch}
              >
                <Search className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>

        {/* User actions */}
        <div className="flex items-center gap-4 ml-auto">
          {/* Login/Register or User Info */}
          {isAuthenticated && displayUser ? (
            <DropdownMenu>
              <DropdownMenuTrigger className="group flex items-center gap-2 hover:text-amber-400 transition-colors cursor-pointer outline-none">
                <User className="w-7 h-7" />
                <div className="text-base text-left">
                  <div className="text-white font-medium group-hover:text-amber-400">{displayUser.accountName}</div>
                  <div className="text-gray-300 text-sm group-hover:text-amber-400">{displayUser.accountPhone}</div>
                </div>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-48 mr-4">
                <DropdownMenuItem
                  onClick={() => {
                    if (requireAuth('xem thông tin tài khoản')) {
                      navigate('/account/profile');
                    }
                  }}
                  className="cursor-pointer"
                >
                  <UserCircle className="mr-2 h-4 w-4" />
                  <span>Thông tin tài khoản</span>
                </DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() => {
                    if (requireAuth('xem lịch sử đơn hàng')) {
                      navigate('/customer/orders');
                    }
                  }}
                  className="cursor-pointer"
                >
                  <History className="mr-2 h-4 w-4" />
                  <span>Lịch sử đơn hàng</span>
                </DropdownMenuItem>

                {/* Admin Panel - Only show for admin users */}
                {displayUser.roles && displayUser.roles.some((role) => role.roleName === 'ADMIN') && (
                  <>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem
                      onClick={() => {
                        if (requireAuth('truy cập trang quản trị')) {
                          navigate('/admin/dashboard');
                        }
                      }}
                      className="cursor-pointer text-blue-600 focus:text-blue-600"
                    >
                      <Settings className="mr-2 h-4 w-4" />
                      <span>Trang quản trị</span>
                    </DropdownMenuItem>
                  </>
                )}

                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-red-600 focus:text-red-600">
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>Đăng xuất</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <button
              onClick={() => setIsLoginDialogOpen(true)}
              className="group flex items-center gap-2 hover:text-amber-400 transition-colors cursor-pointer"
            >
              <User className="w-7 h-7" />
              <div className="text-base">
                <div className="text-white font-medium text-sm group-hover:text-amber-400 transition-colors">Tài khoản</div>
              </div>
            </button>
          )}

          {/* Divider */}
          <div className="w-[1px] h-8 bg-gray-300 mx-4"></div>

          {/* Shopping cart */}
          <div className="flex items-center gap-2 relative hover:text-amber-400 transition-colors cursor-pointer">
            <HoverCard openDelay={200} closeDelay={300}>
              <HoverCardTrigger asChild>
                <div
                  className={`flex items-center group ${
                    isAuthenticated && user && cartStatus !== null && cartStatus !== 1 
                      ? 'cursor-not-allowed opacity-60' 
                      : 'cursor-pointer'
                  }`}
                  onClick={handleCartClick}
                  onMouseEnter={() => {
                    // Chỉ fetch hover cart nếu cart không bị khóa
                    if (!isAuthenticated || !user || cartStatus === null || cartStatus === 1) {
                      fetchHoverCartItems();
                    }
                  }}
                >
                  <ShoppingCart className="w-8 h-8 group-hover:text-amber-400" />
                  <div className="text-white  group-hover:text-amber-400">
                    <div className=" font-medium ml-2 ">Giỏ hàng</div>
                  </div>
                  <div className="relative">
                    {cartItemCount > 0 && (
                      <span className="absolute -top-6 -right-5 bg-red-500 text-white text-xs rounded-full w-6 h-6 flex items-center justify-center font-semibold">
                        {cartItemCount > 99 ? '99+' : cartItemCount}
                      </span>
                    )}
                  </div>
                </div>
              </HoverCardTrigger>
              <HoverCardContent className="w-[420px]" side="bottom" align="end" sideOffset={10} alignOffset={-20}>
                {isAuthenticated && user && cartStatus !== null && cartStatus !== 1 ? (
                  <div className="w-full text-center py-8">
                    <ShoppingCart className="w-12 h-12 mx-auto mb-2 text-red-400" />
                    <p className="text-red-600 font-semibold">Giỏ hàng đã bị vô hiệu hóa</p>
                    <p className="text-sm text-gray-600 mt-2">Vui lòng liên hệ VuaTraiCay để biết thêm chi tiết</p>
                  </div>
                ) : (
                  <div className="flex flex-col items-start justify-center">
                  <h1 className="text-[#F36F40] text-center w-full mb-4">GIỎ HÀNG</h1>

                  {hoverCartItems.length === 0 ? (
                    <div className="w-full text-center py-8 text-gray-500">
                      <ShoppingCart className="w-12 h-12 mx-auto mb-2 text-gray-400" />
                      <p>Giỏ hàng trống</p>
                    </div>
                  ) : (
                    <div className="w-full space-y-4">
                      {hoverCartItems.map((item) => (
                        <div key={item.cartItemId} className="flex items-start justify-between w-full">
                          <div className="flex">
                            <img
                              src={getImageUrl(item)}
                              alt={item.productName}
                              className="w-20 h-20 object-cover rounded"
                              onError={(e) => {
                                const target = e.currentTarget;
                                // Chỉ set placeholder 1 lần để tránh loop
                                if (!target.dataset.errorHandled) {
                                  target.dataset.errorHandled = 'true';
                                  target.src = '/placeholder-image.svg';
                                }
                              }}
                            />
                            <div className="flex flex-col ml-2">
                              <h4 className="text-sm font-medium line-clamp-2 mb-2">{item.productName}</h4>
                              <div className="flex items-center">
                                <Button
                                  size="sm"
                                  onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, -1)}
                                  className="w-6 h-6 p-0 rounded-full bg-[#FEF8F7] text-[#F26F3F] hover:bg-[#F26F3F] hover:text-white"
                                  hover="none"
                                  disabled={item.quantity <= 1}
                                >
                                  -
                                </Button>
                                <Input
                                  value={item.quantity}
                                  readOnly
                                  className="w-12 h-6 mx-2 text-center text-sm rounded-[0px]"
                                />
                                <Button
                                  size="sm"
                                  onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity, 1)}
                                  className="w-6 h-6 p-0 rounded-full bg-[#FEF8F7] text-[#F26F3F] hover:bg-[#F26F3F] hover:text-white"
                                  hover="none"
                                >
                                  +
                                </Button>
                              </div>
                            </div>
                          </div>
                          <div className="flex flex-col items-end">
                            <button
                              onClick={() => handleRemoveItem(item.cartItemId)}
                              className="text-gray-400 hover:text-red-500 mb-2"
                            >
                              <X className="w-5 h-5" />
                            </button>
                            <h4 className="text-sm font-semibold text-red-600">
                              {formatPrice(item.productPrice * item.quantity)}
                            </h4>
                          </div>
                        </div>
                      ))}

                      {cartItemCount > 3 && (
                        <p className="text-center text-gray-500 text-sm">+{cartItemCount - 3} sản phẩm khác</p>
                      )}

                      <div className="pt-4 border-t">
                        <Button
                          onClick={() => setIsCartOpen(true)}
                          className="w-full bg-primary text-white hover:bg-[#F38258]"
                        >
                          Xem giỏ hàng đầy đủ
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
                )}
              </HoverCardContent>
            </HoverCard>
          </div>
        </div>
      </div>

      {/* Login Dialog */}
      <LoginDialog isOpen={isLoginDialogOpen} onClose={() => setIsLoginDialogOpen(false)} />

      {/* Cart Dialog */}
      <Cart isOpen={isCartOpen} onClose={() => setIsCartOpen(false)} />
    </div>
  );
};

export default TopNavigation;
