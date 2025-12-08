import { useEffect } from 'react'
import { useAppDispatch } from '../../hooks/redux'
import { loadAdminUserFromStorage, setAdminInitialized } from '../../store/slices/adminAuthSlice'
import type { Account } from '../../types/account'

const AdminAuthInitializer: React.FC = () => {
  const dispatch = useAppDispatch()

  useEffect(() => {
    // Load admin user from localStorage on app startup
    try {
      const adminUserStr = localStorage.getItem('admin_user')
      const isAuthenticated = localStorage.getItem('admin_isAuthenticated') === 'true'
      
      if (adminUserStr && isAuthenticated) {
        const adminUser = JSON.parse(adminUserStr) as Account
        
        // Kiểm tra xem user có role ADMIN không
        const userRoles = adminUser.roles || []
        const isAdmin = userRoles.some(role => role.roleName === 'ADMIN')
        
        if (isAdmin) {
          dispatch(loadAdminUserFromStorage(adminUser))
        } else {
          // Không phải admin, xóa khỏi localStorage
          localStorage.removeItem('admin_user')
          localStorage.removeItem('admin_isAuthenticated')
          dispatch(setAdminInitialized())
        }
      } else {
        // Không có admin user trong storage, đánh dấu là đã initialized
        dispatch(setAdminInitialized())
      }
    } catch (error) {
      // Silent error handling
      dispatch(setAdminInitialized())
    }
  }, [dispatch])

  return null
}

export default AdminAuthInitializer
