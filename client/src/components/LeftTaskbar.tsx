import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAppSelector, useAppDispatch } from '../hooks/redux'
import { logout } from '../store/slices/authSlice'
import { 
  LayoutDashboard, 
  Package, 
  Tags, 
  Users, 
  Shield, 
  LogOut,
  User,
  Home,
  ShoppingBag,
  Truck,
  Star
} from 'lucide-react'

const LeftTaskbar: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const dispatch = useAppDispatch()
  const { user } = useAppSelector((state) => state.auth)

  const handleLogout = () => {
    dispatch(logout())
    navigate('/')
  }

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
      icon: ShoppingBag,
      label: 'Đơn hàng',
      path: '/admin/orders'
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
    <div className="fixed left-4 top-4 bottom-4 w-56 bg-slate-800 text-white flex flex-col rounded-xl shadow-2xl z-50">
      {/* Header */}
      <div className="p-4 border-b border-slate-700">
        <div className="flex items-center gap-3">
          <div className="bg-amber-500 p-2 rounded-full">
            <User className="w-5 h-5 text-white" />
          </div>
          <div>
            <h3 className="font-semibold text-white text-sm">{user?.accountName || 'Admin'}</h3>
            <p className="text-xs text-slate-300">{user?.roles?.[0]?.roleName || 'ADMIN'}</p>
          </div>
        </div>
      </div>

      {/* Body - Menu Items */}
      <div className="flex-1 py-3">
        <nav className="space-y-1 px-3">
          {menuItems.map((item) => {
            const Icon = item.icon
            const isActive = isActivePath(item.path)
            
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left transition-colors ${
                  isActive 
                    ? 'bg-amber-500 text-white shadow-lg' 
                    : 'text-slate-300 hover:bg-slate-700 hover:text-white'
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
        <div className="p-3 border-t border-slate-700 space-y-2">
          <button
            onClick={() => navigate('/')}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 text-slate-300 hover:bg-blue-600 hover:text-white rounded-lg transition-colors text-sm"
          >
            <Home className="w-4 h-4" />
            <span className="font-medium">Về trang khách hàng</span>
          </button>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-3 py-2 text-slate-300 hover:bg-red-600 hover:text-white rounded-lg transition-colors text-sm"
          >
            <LogOut className="w-4 h-4" />
            <span className="font-medium">Đăng xuất</span>
          </button>
        </div>
      </div>
  )
}

export default LeftTaskbar
