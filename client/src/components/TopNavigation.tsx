import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, User, ShoppingCart, Phone, LogOut, UserCircle, History, Settings } from 'lucide-react'
import { Button } from './ui/button'
import { Input } from './ui/input'
import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from './ui/dropdown-menu'
import { useAppSelector, useAppDispatch } from '../hooks/redux'
import { logout } from '../store/slices/authSlice'
import LoginDialog from '../pages/Mainpage/Login'
import Cart from './Cart'
import { cartService } from '../services/cartService'
import { toast } from 'sonner'

const TopNavigation: React.FC = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { isAuthenticated, user } = useAppSelector((state) => state.auth)
  const [isLoginDialogOpen, setIsLoginDialogOpen] = useState(false)
  const [isCartOpen, setIsCartOpen] = useState(false)
  const [cartItemCount, setCartItemCount] = useState(0)

  const handleLogout = () => {
    dispatch(logout())
    navigate('/')
  }

  // Helper function to check authentication and show login message
  const requireAuth = (action: string) => {
    if (!isAuthenticated || !user) {
      toast.error(`Vui lòng đăng nhập để ${action}`)
      setIsLoginDialogOpen(true)
      return false
    }
    return true
  }

  // Fetch cart items count when user is authenticated
  useEffect(() => {
    if (isAuthenticated && user) {
      fetchCartItemsCount()
    } else {
      setCartItemCount(0)
    }
  }, [isAuthenticated, user])

  // Listen for cart updates
  useEffect(() => {
    const handleCartUpdate = () => {
      if (isAuthenticated && user) {
        fetchCartItemsCount()
      }
    }

    const handleCloseCartModal = () => {
      setIsCartOpen(false)
    }

    window.addEventListener('cartUpdated', handleCartUpdate)
    window.addEventListener('closeCartModal', handleCloseCartModal)
    
    return () => {
      window.removeEventListener('cartUpdated', handleCartUpdate)
      window.removeEventListener('closeCartModal', handleCloseCartModal)
    }
  }, [isAuthenticated, user])

  const fetchCartItemsCount = async () => {
    if (!user) return
    
    try {
      const response = await cartService.getCartItems(user.accountId)
      if (response.success && response.data) {
        let count = 0
        if (Array.isArray(response.data)) {
          count = response.data.reduce((total, item) => total + item.quantity, 0)
        } else if (typeof response.data === 'object' && 'items' in response.data) {
          count = (response.data as any).items?.reduce((total: number, item: any) => total + item.quantity, 0) || 0
        }
        setCartItemCount(count)
      }
    } catch (error) {
      console.error('Error fetching cart items count:', error)
      setCartItemCount(0)
    }
  }

  return (
    <div className="bg-slate-700 text-white">
      {/* Top bar with hotline */}
      <div className="bg-amber-400 text-black px-4 py-1 text-sm">
        <div className="container mx-auto flex items-center">
          <Phone className="w-4 h-4 mr-2" />
          <span className="font-medium">Hotline: 0903.400.028</span>
          <span className="ml-2 text-gray-700">(8h - 12h, 13h30 - 17h)</span>
          <span className="ml-auto">Liên hệ hợp tác</span>
        </div>
      </div>

      {/* Main navigation */}
      <div className="container mx-auto px-2 py-3">
        <div className="flex items-center justify-between gap-4">
          {/* Logo */}
          <button 
            onClick={() => navigate('/')}
            className="flex items-center hover:opacity-80 transition-opacity cursor-pointer"
          >
            <div className="bg-blue-500 text-white px-3 py-2 rounded-md mr-3">
              <span className="font-bold text-lg">BC</span>
            </div>
            <div className="text-xl font-bold text-blue-400">
              BOOK<span className="text-white">CITY</span><span className="text-blue-400">.VN</span>
            </div>
          </button>

          {/* Search bar */}
          <div className="flex-1 max-w-2xl mx-8">
            <div className="relative">
              <Input
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                className="w-full pl-4 pr-12 py-2 rounded border-0 bg-white text-black"
              />
              <Button
                size="sm"
                className="absolute right-1 top-1/2 transform -translate-y-1/2 bg-slate-700 hover:bg-slate-600 text-white px-4 rounded"
              >
                <Search className="w-4 h-4" />
              </Button>
            </div>
          </div>

          {/* Divider */}
          <div className="w-px h-8 bg-gray-500 mx-4"></div>

          {/* User actions */}
          <div className="flex items-center gap-4">
            {/* Login/Register or User Info */}
            {isAuthenticated && user ? (
              <DropdownMenu>
                <DropdownMenuTrigger className="flex items-center gap-2 hover:text-amber-400 transition-colors cursor-pointer outline-none">
                  <User className="w-7 h-7" />
                  <div className="text-base text-left">
                    <div className="text-white font-medium">{user.accountName}</div>
                    <div className="text-gray-300 text-sm">{user.accountPhone}</div>
                  </div>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-48 mr-4">
                  <DropdownMenuItem 
                    onClick={() => {
                      if (requireAuth('xem thông tin tài khoản')) {
                        navigate('/account/profile')
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
                        navigate('/customer/orders')
                      }
                    }}
                    className="cursor-pointer"
                  >
                    <History className="mr-2 h-4 w-4" />
                    <span>Lịch sử đơn hàng</span>
                  </DropdownMenuItem>
                  
                  {/* Admin Panel - Only show for admin users */}
                  {user.roles && user.roles.some(role => role.roleName === 'ADMIN') && (
                    <>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem 
                        onClick={() => {
                          if (requireAuth('truy cập trang quản trị')) {
                            navigate('/admin/dashboard')
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
                  <DropdownMenuItem 
                    onClick={handleLogout}
                    className="cursor-pointer text-red-600 focus:text-red-600"
                  >
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Đăng xuất</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <button 
                onClick={() => setIsLoginDialogOpen(true)}
                className="flex items-center gap-2 hover:text-amber-400 transition-colors cursor-pointer"
              >
                <User className="w-7 h-7" />
                <div className="text-base">
                  <div className="text-white font-medium text-sm">Đăng nhập / Đăng ký</div>
                  <div className="text-gray-300 text-sm">Tài khoản của tôi</div>
                </div>
              </button>
            )}

            {/* Divider */}
            <div className="w-px h-8 bg-gray-500 mx-4"></div>

            {/* Shopping cart */}
            <div 
              className="flex items-center gap-2 relative hover:text-amber-400 transition-colors cursor-pointer"
              onClick={() => {
                if (requireAuth('xem giỏ hàng')) {
                  setIsCartOpen(true)
                }
              }}
            >
              <div className="relative">
                <ShoppingCart className="w-8 h-8" />
                {cartItemCount > 0 && (
                  <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-6 h-6 flex items-center justify-center font-semibold">
                    {cartItemCount > 99 ? '99+' : cartItemCount}
                  </span>
                )}
              </div>
              <div className="text-sm">
                <div className="text-white font-medium">Giỏ hàng</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Login Dialog */}
      <LoginDialog 
        isOpen={isLoginDialogOpen} 
        onClose={() => setIsLoginDialogOpen(false)} 
      />

      {/* Cart Dialog */}
      <Cart 
        isOpen={isCartOpen} 
        onClose={() => setIsCartOpen(false)} 
      />
    </div>
  )
}

export default TopNavigation
