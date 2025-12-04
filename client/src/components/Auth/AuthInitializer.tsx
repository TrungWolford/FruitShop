import { useEffect } from 'react'
import { useAppDispatch } from '../../hooks/redux'
import { loadUserFromStorage, setInitialized } from '../../store/slices/authSlice'
import { authService } from '../../services/authService'

const AuthInitializer: React.FC = () => {
  const dispatch = useAppDispatch()

  useEffect(() => {
    // Load user from localStorage on app startup
    const user = authService.loadUserFromStorage()
    if (user && authService.isAuthenticated()) {
      dispatch(loadUserFromStorage(user))
    } else {
      // Không có user trong storage, vẫn phải đánh dấu là đã initialized
      dispatch(setInitialized())
    }
  }, [dispatch])

  return null
}

export default AuthInitializer
