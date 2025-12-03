import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppSelector, useAppDispatch } from '../../hooks/redux'
import { loginAsync } from '../../store/slices/authSlice'
import { Lock, User, AlertCircle, Loader2 } from 'lucide-react'
// z
const AdminLogin: React.FC = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { user, isAuthenticated, loading } = useAppSelector((state) => state.auth)
  
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    document.title = 'Admin - Đăng nhập'
    
    // If already authenticated as admin, redirect to dashboard
    if (isAuthenticated && user) {
      const userRoles = user.roles || []
      const isAdmin = userRoles.some(role => role.roleName === 'ADMIN')
      
      if (isAdmin) {
        navigate('/admin/dashboard')
      }
    }
  }, [isAuthenticated, user, navigate])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    // Validation 1: Kiểm tra trường bắt buộc
    if (!email || !password) {
      setError('Vui lòng nhập đầy đủ thông tin')
      return
    }

    // Validation 2: Kiểm tra định dạng số điện thoại (10-11 số)
    const phoneRegex = /^[0-9]{10,11}$/
    if (!phoneRegex.test(email)) {
      setError('Số điện thoại không đúng định dạng (10-11 số)')
      return
    }

    // Validation 3: Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
    if (password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự')
      return
    }

    console.log('🔐 Admin Login - Attempting login with:', { email })

    try {
      const result = await dispatch(loginAsync({ email, password })).unwrap()
      
      console.log('✅ Admin Login - Response:', result)
      
      if (result.success && result.user) {
        const userRoles = result.user.roles || []
        const isAdmin = userRoles.some(role => role.roleName === 'ADMIN')
        
        console.log('👤 User roles:', userRoles)
        console.log('🔑 Is admin:', isAdmin)
        
        if (isAdmin) {
          // Dùng replace để tránh back về trang login
          navigate('/admin/dashboard', { replace: true })
        } else {
          setError('Tài khoản không có quyền truy cập trang quản trị')
        }
      } else {
        setError(result.message || 'Sai tài khoản hoặc mật khẩu')
      }
    } catch (err: any) {
      console.error('❌ Admin Login - Error:', err)
      console.error('❌ Error details:', {
        message: err.message,
        response: err.response,
        status: err.response?.status,
        data: err.response?.data
      })
      
      // Hiển thị thông báo thân thiện dựa trên status code
      if (err.response?.status === 401) {
        setError('Tài khoản hoặc mật khẩu không đúng')
      } else if (err.response?.status === 403) {
        setError('Tài khoản không có quyền truy cập')
      } else if (err.response?.status >= 500) {
        setError('Lỗi hệ thống. Vui lòng thử lại sau')
      } else {
        setError(err.message || 'Đăng nhập thất bại. Vui lòng thử lại.')
      }
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50 to-orange-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-8">
        {/* Logo/Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-amber-500 rounded-full mb-4">
            <Lock className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-800">Đăng nhập Admin</h1>
          <p className="text-gray-500 mt-2">Vui lòng đăng nhập để tiếp tục</p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center gap-3">
            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
            <p className="text-red-600 text-sm">{error}</p>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Số điện thoại <span className="text-red-500">*</span>
              <span className="text-xs text-gray-500 ml-2">(10-11 số)</span>
            </label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:border-amber-500 transition-colors"
                placeholder="Nhập số điện thoại (10-11 số)"
                maxLength={11}
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Mật khẩu <span className="text-red-500">*</span>
              <span className="text-xs text-gray-500 ml-2">(Tối thiểu 6 ký tự)</span>
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:border-amber-500 transition-colors"
                placeholder="Nhập mật khẩu"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-amber-500 hover:bg-amber-600 text-white font-semibold py-3 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                Đang đăng nhập...
              </>
            ) : (
              'Đăng nhập'
            )}
          </button>
        </form>

        {/* Back to Home */}
        <div className="mt-6 text-center">
          <button
            onClick={() => navigate('/')}
            className="text-amber-600 hover:text-amber-700 text-sm font-medium"
          >
            ← Quay về trang chủ
          </button>
        </div>
      </div>
    </div>
  )
}

export default AdminLogin
