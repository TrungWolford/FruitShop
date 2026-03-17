import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Package,
  Tags,
  Users,
  Shield,
  Home,
  ShoppingBag,
  ShoppingCart,
  Truck,
  Star,
  RefreshCcw,
  CreditCard,
  MessageSquare
} from 'lucide-react'

interface LeftTaskbar {
  className?: string
}

const LeftTaskbar: React.FC<LeftTaskbar> = ({ className }) => {
  const navigate = useNavigate()
  const location = useLocation()


  const menuItems = [
    {
      icon: LayoutDashboard,
      label: 'Dashboard',
      path: '/admin/dashboard'
    },
    {
      icon: Package,
      label: 'Sản phẩm',
      path: '/admin/products'
    },
    {
      icon: Tags,
      label: 'Thể loại',
      path: '/admin/categories'
    },
    {
      icon: ShoppingCart,
      label: 'Giỏ hàng',
      path: '/admin/carts'
    },
    {
      icon: ShoppingBag,
      label: 'Đơn hàng',
      path: '/admin/orders'
    },
    {
      icon: CreditCard,
      label: 'Thanh toán',
      path: '/admin/payments'
    },
    {
      icon: RefreshCcw,
      label: 'Hoàn trả',
      path: '/admin/refunds'
    },
    {
      icon: Truck,
      label: 'Vận chuyển',
      path: '/admin/shippings'
    },
    {
      icon: Star,
      label: 'Đánh giá',
      path: '/admin/ratings'
    },
    {
      icon: MessageSquare,
      label: 'Tin nhắn',
      path: '/admin/messages'
    },
    {
      icon: Users,
      label: 'Account',
      path: '/admin/accounts'
    },
    {
      icon: Shield,
      label: 'Vai trò',
      path: '/admin/roles'
    }
  ]

  const isActivePath = (path: string) => {
    return location.pathname === path
  }

  // Bỏ check user để hiển thị LeftTaskbar luôn
  // if (!user) return null

  return (
    <div className={`fixed h-[calc(100vh-60px)] left-0 top-0 w-[260px] text-[#7B756C] flex flex-col shadow-2xl ${className ?? ''}`}>

      {/* Body - Menu Items */}
      <div className="flex-1 py-3 overflow-y-auto">
        <nav className="space-y-1 px-3">
          {menuItems.map((item) => {
            const Icon = item.icon
            const isActive = isActivePath(item.path)

            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center gap-3 px-3 py-3 rounded-md text-left ${isActive
                  ? 'bg-amber-500 text-white shadow-lg'
                  : ' hover:bg-[#F5E8D3] hover:text-[#1A1A1A]'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span className="font-medium">{item.label}</span>
              </button>
            )
          })}
        </nav>
      </div>

      {/* Footer - Customer Page & Logout */}
      <div className="h-[60px] shrink-0 p-3 border-t border-slate-300 space-y-2">
        <button
          onClick={() => navigate('/')}
          className="w-full flex justify-start gap-2 px-3 py-2  hover:bg-blue-600 hover:text-white rounded-lg transition-colors text-sm"
        >
          <Home className="w-4 h-4" />
          <span className="font-medium">Về trang khách hàng</span>
        </button>
      </div>
    </div>

  )
}

export default LeftTaskbar
