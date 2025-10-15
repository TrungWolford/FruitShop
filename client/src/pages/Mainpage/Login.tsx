import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../hooks/redux'
import { loginStart, loginSuccess, loginFailure } from '../../store/slices/authSlice'
import { authService } from '../../services/authService'
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogHeader, 
  DialogTitle, 
  Button, 
  Input, 
  Label,
  Checkbox
} from '../../components/ui'
import { Eye, EyeOff, Mail, Lock } from 'lucide-react'
import { toast } from 'sonner'

interface LoginDialogProps {
  isOpen: boolean
  onClose: () => void
}

const LoginDialog: React.FC<LoginDialogProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { loading, error } = useAppSelector((state) => state.auth)
  const [showPassword, setShowPassword] = useState(false)
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    rememberMe: false
  })

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    dispatch(loginStart())
    
    try {
      const response = await authService.login({
        email: formData.username, // Using username field as phone number
        password: formData.password
      })
      
      if (response.success && response.user) {
        dispatch(loginSuccess(response.user))
        onClose()
        
        // Check user roles and navigate accordingly
        const userRoles = response.user.roles || []
        const isAdmin = userRoles.some(role => role.roleName === 'ADMIN')
        const isCustomer = userRoles.some(role => role.roleName === 'CUSTOMER')
        
        // Add a small delay to ensure Redux state is updated
        setTimeout(() => {
          if (isAdmin) {
            toast.success('Đăng nhập thành công! Chào mừng Admin.')
            navigate('/admin/dashboard')
          } else if (isCustomer) {
            toast.success('Đăng nhập thành công! Chào mừng bạn trở lại.')
            // Không navigate đến trang customer nữa, ở lại trang hiện tại
          } else {
            // Default fallback
            toast.success('Đăng nhập thành công!')
            // Không navigate đến trang customer nữa, ở lại trang hiện tại
          }
        }, 100)
      } else {
        dispatch(loginFailure(response.message || 'Đăng nhập thất bại'))
      }
    } catch (error) {
      dispatch(loginFailure('Có lỗi xảy ra khi đăng nhập'))
    }
  }

  const navigateToRegister = () => {
    onClose()
    // Navigate to register page using React Router
    navigate('/account/register')
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[500px] p-0 overflow-hidden rounded-lg">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-800 text-white p-6 rounded-t-md">
          <DialogHeader>
            <DialogTitle className="text-2xl font-bold text-center">
              Đăng nhập
            </DialogTitle>
            <DialogDescription className="text-blue-100 text-center">
              Chào mừng bạn quay lại BookCity.VN
            </DialogDescription>
          </DialogHeader>
        </div>

        {/* Form */}
        <div className="p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Phone Number */}
            <div className="space-y-2">
              <Label htmlFor="username" className="text-sm font-medium">
                Số điện thoại *
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="username"
                  name="username"
                  type="tel"
                  placeholder="Nhập số điện thoại"
                  value={formData.username}
                  onChange={handleInputChange}
                  className="pl-10 rounded-md"
                  required
                />
              </div>
            </div>

            {/* Password */}
            <div className="space-y-2">
              <Label htmlFor="password" className="text-sm font-medium">
                Mật khẩu *
              </Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Nhập mật khẩu"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="pl-10 pr-10 rounded-md"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-blue-600"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            {/* Remember Me */}
            <div className="flex items-center space-x-2">
              <Checkbox
                id="rememberMe"
                name="rememberMe"
                checked={formData.rememberMe}
                onCheckedChange={(checked) => 
                  setFormData(prev => ({ ...prev, rememberMe: checked as boolean }))
                }
              />
              <Label htmlFor="rememberMe" className="text-sm text-gray-600">
                Ghi nhớ đăng nhập
              </Label>
            </div>

            {/* Error Message */}
            {error && (
              <div className="text-red-500 text-sm text-center bg-red-50 p-2 rounded-md">
                {error}
              </div>
            )}

            {/* Submit Button */}
            <Button 
              type="submit" 
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-md disabled:opacity-50"
            >
              {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </Button>

            {/* Forgot Password */}
            <div className="text-center">
              <a href="#" className="text-sm text-blue-600 hover:text-blue-700 underline">
                Quên mật khẩu?
              </a>
            </div>
          </form>

          {/* Register Link */}
          <div className="mt-6 pt-4 border-t border-gray-200 text-center">
            <p className="text-sm text-gray-600">
              Chưa có tài khoản?{' '}
              <button
                type="button"
                onClick={navigateToRegister}
                className="text-blue-600 hover:text-blue-700 font-semibold underline"
              >
                Đăng ký ngay
              </button>
            </p>
          </div>

          {/* Social Login */}
          <div className="mt-4">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Hoặc đăng nhập với</span>
              </div>
            </div>
            
            <div className="mt-4 grid grid-cols-2 gap-3">
              <Button variant="outline" className="w-full rounded-md">
                <svg className="w-4 h-4 mr-2" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Google
              </Button>
              <Button variant="outline" className="w-full rounded-md">
                <svg className="w-4 h-4 mr-2" fill="#1877F2" viewBox="0 0 24 24">
                  <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                </svg>
                Facebook
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

export default LoginDialog