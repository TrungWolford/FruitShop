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
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
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
        <div className="bg-gradient-to-r from-orange-400 to-orange-600 text-white p-6 rounded-t-md">
          <DialogHeader>
            <DialogTitle className="text-2xl font-bold text-center">
              Đăng nhập
            </DialogTitle>
            <DialogDescription className="text-white-100 text-center">
              Chào mừng bạn quay lại VuaTraiCay
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
              className="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold py-2.5 rounded-md disabled:opacity-50"
            >
              {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </Button>

            {/* Forgot Password */}
            <div className="text-center">
              <a href="#" className="text-sm text-orange-500 hover:text-orange-600 underline">
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
                className="text-orange-500 hover:text-orange-600 font-semibold underline"
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
              
            </div>
            
           
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

export default LoginDialog