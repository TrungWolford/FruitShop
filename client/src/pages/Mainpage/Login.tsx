import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../hooks/redux'
import { loginStart, loginSuccess, loginFailure, clearAuthError } from '../../store/slices/authSlice'
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
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Reset error khi dialog mở (không reset khi đóng)
  React.useEffect(() => {
    if (isOpen) {
      // Reset error khi dialog mở
      dispatch(clearAuthError())
    }
  }, [isOpen, dispatch])

  // Custom onClose handler để reset form sau khi đóng
  const handleClose = () => {
    // Chỉ đóng nếu không đang submit, không đang loading và KHÔNG có lỗi
    if (!isSubmitting && !loading && !error) {
      onClose()
      // Reset form sau khi đóng (dùng timeout để tránh flash)
      setTimeout(() => {
        setFormData({
          username: '',
          password: '',
          rememberMe: false
        })
        dispatch(clearAuthError())
      }, 300)
    }
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
    // Clear error when user types
    if (error) {
      dispatch(clearAuthError())
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    // Validation 1: Kiểm tra trường bắt buộc
    if (!formData.username || !formData.password) {
      toast.error('Vui lòng nhập đầy đủ thông tin')
      return
    }

    // Validation 2: Kiểm tra định dạng số điện thoại (10-11 số)
    const phoneRegex = /^[0-9]{10,11}$/
    if (!phoneRegex.test(formData.username)) {
      toast.error('Số điện thoại hoặc mật khẩu không đúng')
      return
    }

    // Validation 3: Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
    if (formData.password.length < 6) {
      toast.error('Số điện thoại hoặc mật khẩu không đúng')
      return
    }
    setIsSubmitting(true)
    dispatch(loginStart())
    
    try {
      const response = await authService.login({
        email: formData.username, // Using username field as phone number
        password: formData.password
      })
      
      console.log('🔐 Login response:', response)
      
      // Kiểm tra response có hợp lệ không (phải có user object với accountId)
      if (response.success && response.user && response.user.accountId) {
        // Dispatch loginSuccess TRƯỚC để Redux state được cập nhật ngay
        dispatch(loginSuccess(response.user))
        
        // Trigger event để các component khác biết auth đã cập nhật
        window.dispatchEvent(new CustomEvent('authUpdated'))
        
        // Check for intended route (from checkout button)
        const intendedRoute = sessionStorage.getItem('intendedRoute')
        if (intendedRoute) {
          sessionStorage.removeItem('intendedRoute')
          toast.success('Đăng nhập thành công!')
          console.log('🔐 localStorage user:', localStorage.getItem('user'))
          console.log('🔐 localStorage isAuthenticated:', localStorage.getItem('isAuthenticated'))
          
          // Đóng dialog trước khi navigate
          onClose()
          setTimeout(() => {
            setFormData({
              username: '',
              password: '',
              rememberMe: false
            })
            dispatch(clearAuthError())
          }, 300)
          
          // Navigate sau một khoảng delay nhỏ
          setTimeout(() => {
            navigate(intendedRoute)
          }, 100)
          return
        }
        
        // Check user roles
        const userRoles = response.user.roles || []
        const isCustomer = userRoles.some(role => role.roleName === 'CUSTOMER')
        
        if (isCustomer) {
          toast.success('Đăng nhập thành công! Chào mừng bạn trở lại.')
        } else {
          toast.success('Đăng nhập thành công!')
        }
        
        // Đóng dialog SAU khi đã dispatch success để UI kịp cập nhật
        setTimeout(() => {
          onClose()
          setTimeout(() => {
            setFormData({
              username: '',
              password: '',
              rememberMe: false
            })
            dispatch(clearAuthError())
          }, 300)
        }, 100)
      } else {
        dispatch(loginFailure(response.message || 'Số điện thoại hoặc mật khẩu không đúng'))
        toast.error(response.message || 'Số điện thoại hoặc mật khẩu không đúng')
      }
    } catch (error: any) {
      // Sử dụng error message đã được parse từ authService
      const errorMessage = error.message || 'Số điện thoại hoặc mật khẩu không đúng'
      
      // Kiểm tra nếu là tài khoản bị vô hiệu hóa
      if (errorMessage.toLowerCase().includes('deactivated') || errorMessage.includes('vô hiệu hóa') || errorMessage.includes('tạm ngưng')) {
        dispatch(loginFailure('Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.'))
        toast.error('Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.', {
          duration: 5000
        })
      } else {
        dispatch(loginFailure(errorMessage))
        toast.error(errorMessage)
      }
      
      // KHÔNG redirect khi login failed - giữ user ở trang login
    } finally {
      setIsSubmitting(false)
    }
  }

  const navigateToRegister = () => {
    handleClose()
    // Navigate to register page using React Router
    navigate('/account/register')
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent 
        className="sm:max-w-[500px] p-0 overflow-hidden rounded-lg"
        onInteractOutside={(e) => {
          // Ngăn đóng dialog khi click ra ngoài trong lúc đang submit hoặc có lỗi
          if (isSubmitting || loading || error) {
            e.preventDefault()
          }
        }}
        onEscapeKeyDown={(e) => {
          // Ngăn đóng dialog khi nhấn ESC trong lúc đang submit hoặc có lỗi
          if (isSubmitting || loading || error) {
            e.preventDefault()
          }
        }}
      >
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
                Số điện thoại <span className="text-red-500">*</span>
                <span className="text-xs text-gray-500 ml-2">(10-11 số)</span>
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  id="username"
                  name="username"
                  type="tel"
                  placeholder="Nhập số điện thoại (10-11 số)"
                  value={formData.username}
                  onChange={handleInputChange}
                  className="pl-10 rounded-md"
                  maxLength={11}
                  required
                />
              </div>
            </div>

            {/* Password */}
            <div className="space-y-2">
              <Label htmlFor="password" className="text-sm font-medium">
                Mật khẩu <span className="text-red-500">*</span>
                <span className="text-xs text-gray-500 ml-2">(Tối thiểu 6 ký tự)</span>
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
              disabled={loading || !formData.username || !formData.password}
              className="w-full bg-orange-500 hover:bg-orange-600 text-white font-semibold py-2.5 rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
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