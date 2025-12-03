import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppSelector } from '../../hooks/redux'
import { productService } from '../../services/productService'
import { categoryService } from '../../services/categoryService'
import { accountService } from '../../services/adminAccountService'
import { orderService } from '../../services/orderService'
import type { Product } from '../../types/product'
import type { Category } from '../../services/categoryService'
import type { Account } from '../../types/account'
import LeftTaskbar from '../../components/Admin/LeftTaskbar'
import { 
  BarChart, 
  Package, 
  Users, 
  Tags, 
  TrendingUp,
  DollarSign,
  Eye,
  AlertTriangle
} from 'lucide-react'

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAppSelector((state) => state.auth)
  
  // State for data from API
  const [products, setProducts] = useState<Product[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [orders, setOrders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    document.title = 'BookCity - Dashboard'
    
    // Check if user is authenticated and has ADMIN role
    if (!isAuthenticated || !user) {
      navigate('/admin')
      return
    }
    
    const userRoles = user.roles || []
    const isAdmin = userRoles.some(role => role.roleName === 'ADMIN')
    
    if (!isAdmin) {
      navigate('/admin')
      return
    }

    // Load data from APIs
    loadDashboardData()
  }, [isAuthenticated, user, navigate])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      
      // Load products
      const productsResponse = await productService.getAllProducts(0, 100)
      const productsData = productsResponse.content || productsResponse
      setProducts(Array.isArray(productsData) ? productsData : [])
      
      // Load categories
      const categoriesResponse = await categoryService.getAllCategories(0, 100)
      const categoriesData = categoriesResponse.content || categoriesResponse
      setCategories(Array.isArray(categoriesData) ? categoriesData : [])
      
      // Load accounts
      const accountsResponse = await accountService.getAllAccounts(0, 100)
      const accountsData = accountsResponse.content || accountsResponse
      setAccounts(Array.isArray(accountsData) ? accountsData : [])
      
      // Load orders
      try {
        const ordersResponse = await orderService.getAllOrders(0, 100)
        if (ordersResponse.success && ordersResponse.data) {
          setOrders(ordersResponse.data)
        } else {
          setOrders([])
        }
      } catch (error) {
        console.log('Could not load orders:', error)
        setOrders([])
      }
      
    } catch (error) {
      console.error('Error loading dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const totalProducts = products.length
  const activeProducts = products.filter(p => p.status === 1).length
  const totalCategories = categories.filter(c => c.status === 1).length
  const totalUsers = accounts.filter(a => a.roles?.some(role => role.roleName === 'CUSTOMER')).length
  const totalRevenue = orders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)

  const stats = [
    {
      title: 'Tổng sản phẩm',
      value: totalProducts,
      icon: Package,
      color: 'bg-blue-500',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-600'
    },
    {
      title: 'Sản phẩm hoạt động',
      value: activeProducts,
      icon: Eye,
      color: 'bg-green-500',
      bgColor: 'bg-green-50',
      textColor: 'text-green-600'
    },
    {
      title: 'Thể loại',
      value: totalCategories,
      icon: Tags,
      color: 'bg-purple-500',
      bgColor: 'bg-purple-50',
      textColor: 'text-purple-600'
    },
    {
      title: 'Khách hàng',
      value: totalUsers,
      icon: Users,
      color: 'bg-orange-500',
      bgColor: 'bg-orange-50',
      textColor: 'text-orange-600'
    }
  ]

  const recentProducts = products.slice(0, 5)
  const lowStockProducts = products.filter((p: Product) => p.stock < 20 && p.status === 1)

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price)
  }

  // Bỏ check authentication để truy cập trực tiếp admin
  // if (!isAuthenticated || !user || user.role !== 'ADMIN') {
  //   return null
  // }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <LeftTaskbar />
        <div className="ml-64 p-8 flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-500 mx-auto"></div>
            <p className="mt-4 text-gray-600">Đang tải dữ liệu...</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <LeftTaskbar />
      
      <div className="ml-64 p-4">
          {/* Header */}
          <div className="mb-3">
            <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
              <BarChart className="w-5 h-5 text-amber-500" />
              Dashboard
            </h1>
            <p className="text-gray-600 mt-0.5 text-base">Tổng quan hệ thống quản lý BookCity</p>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
            {stats.map((stat, index) => {
              const Icon = stat.icon
              return (
                <div key={index} className="bg-white rounded-lg shadow-md p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-600 mb-1">{stat.title}</p>
                      <p className="text-2xl font-bold text-gray-800">{stat.value}</p>
                    </div>
                    <div className={`${stat.bgColor} p-3 rounded-full`}>
                      <Icon className={`w-6 h-6 ${stat.textColor}`} />
                    </div>
                  </div>
                </div>
              )
            })}
          </div>

          {/* Revenue Card */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
            <div className="lg:col-span-1 bg-gradient-to-r from-amber-500 to-amber-600 rounded-lg shadow-md p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-amber-100 text-sm mb-1">Doanh thu ước tính</p>
                  <p className="text-2xl font-bold">{formatPrice(totalRevenue)}</p>
                  <div className="flex items-center gap-1 mt-2">
                    <TrendingUp className="w-4 h-4" />
                    <span className="text-sm">+12.5% so với tháng trước</span>
                  </div>
                </div>
                <div className="bg-white bg-opacity-20 p-3 rounded-full">
                  <DollarSign className="w-6 h-6" />
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="lg:col-span-2 bg-white rounded-lg shadow-md p-6">
              <h3 className="text-lg font-semibold text-gray-800 mb-4">Thao tác nhanh</h3>
              <div className="grid grid-cols-2 gap-4">
                <button 
                  onClick={() => navigate('/admin/products')}
                  className="flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <Package className="w-5 h-5 text-blue-600" />
                  <span className="font-medium">Quản lý sản phẩm</span>
                </button>
                <button 
                  onClick={() => navigate('/admin/categories')}
                  className="flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <Tags className="w-5 h-5 text-purple-600" />
                  <span className="font-medium">Quản lý thể loại</span>
                </button>
                <button 
                  onClick={() => navigate('/admin/accounts')}
                  className="flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <Users className="w-5 h-5 text-orange-600" />
                  <span className="font-medium">Quản lý tài khoản</span>
                </button>
                <button 
                  onClick={() => navigate('/admin/roles')}
                  className="flex items-center gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <BarChart className="w-5 h-5 text-green-600" />
                  <span className="font-medium">Báo cáo thống kê</span>
                </button>
              </div>
            </div>
          </div>

          {/* Recent Products & Low Stock */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Recent Products */}
            <div className="bg-white rounded-lg shadow-md">
              <div className="p-6 border-b">
                <h3 className="text-lg font-semibold text-gray-800">Sản phẩm mới nhất</h3>
              </div>
              <div className="p-6">
                <div className="space-y-4">
                  {recentProducts.map((product: Product) => (
                    <div key={product.productId} className="flex items-center justify-between">
                      <div>
                        <p className="font-medium text-gray-800">{product.productName}</p>
                        <p className="text-sm text-gray-500">{product.description?.slice(0, 60) || ''}</p>
                      </div>
                      <div className="text-right">
                        <p className="font-medium text-green-600">{formatPrice(product.price)}</p>
                        <p className="text-sm text-gray-500">Kho: {product.stock}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Low Stock Alert */}
            <div className="bg-white rounded-lg shadow-md">
              <div className="p-6 border-b">
                <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                  <AlertTriangle className="w-5 h-5 text-red-500" />
                  Cảnh báo tồn kho thấp
                </h3>
              </div>
              <div className="p-6">
                {lowStockProducts.length > 0 ? (
                  <div className="space-y-4">
                    {lowStockProducts.slice(0, 5).map((product: Product) => (
                      <div key={product.productId} className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-gray-800">{product.productName}</p>
                          <p className="text-sm text-gray-500">{product.description?.slice(0, 60) || ''}</p>
                        </div>
                        <div className="text-right">
                          <p className="font-medium text-red-600">Còn {product.stock} cuốn</p>
                          <p className="text-sm text-gray-500">{formatPrice(product.price)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-gray-500 text-center py-4">Tất cả sản phẩm đều có tồn kho đủ</p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
  )
}

export default AdminDashboard
