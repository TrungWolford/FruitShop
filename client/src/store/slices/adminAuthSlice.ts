import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'
import type { Account } from '../../types/account'
import { STORAGE_KEYS } from '../../config/constants'
import { authService, type LoginCredentials, type LoginResponse } from '../../services/authService'

interface AdminAuthState {
  isAuthenticated: boolean
  user: Account | null
  loading: boolean
  error: string | null
  isInitialized: boolean
}

const initialState: AdminAuthState = {
  isAuthenticated: false,
  user: null,
  loading: false,
  error: null,
  isInitialized: false,
}

// Async thunk for admin login
export const adminLoginAsync = createAsyncThunk<LoginResponse, LoginCredentials>(
  'adminAuth/loginAsync',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials)
      
      // Kiểm tra xem user có role ADMIN không
      if (response.success && response.user) {
        const userRoles = response.user.roles || []
        const isAdmin = userRoles.some(role => role.roleName === 'ADMIN')
        
        if (!isAdmin) {
          // Xóa data đã lưu vì không phải admin
          localStorage.removeItem('admin_user')
          localStorage.removeItem('admin_isAuthenticated')
          return rejectWithValue('Tài khoản không có quyền truy cập trang quản trị')
        }
        
        // Lưu vào localStorage với key riêng cho admin
        localStorage.setItem('admin_user', JSON.stringify(response.user))
        localStorage.setItem('admin_isAuthenticated', 'true')
      }
      
      return response
    } catch (error: any) {
      return rejectWithValue(error.message || 'Đăng nhập thất bại')
    }
  }
)

const adminAuthSlice = createSlice({
  name: 'adminAuth',
  initialState,
  reducers: {
    adminLoginStart: (state) => {
      state.loading = true
      state.error = null
    },
    adminLoginSuccess: (state, action: PayloadAction<Account>) => {
      state.isAuthenticated = true
      state.user = action.payload
      state.loading = false
      state.error = null
      // Lưu vào localStorage với key riêng cho admin
      localStorage.setItem('admin_user', JSON.stringify(action.payload))
      localStorage.setItem('admin_isAuthenticated', 'true')
    },
    adminLoginFailure: (state, action: PayloadAction<string>) => {
      state.isAuthenticated = false
      state.user = null
      state.loading = false
      state.error = action.payload
    },
    adminLogout: (state) => {
      state.isAuthenticated = false
      state.user = null
      state.loading = false
      state.error = null
      // Xóa khỏi localStorage (admin keys + shared token keys)
      localStorage.removeItem('admin_user')
      localStorage.removeItem('admin_isAuthenticated')
      localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
      localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER_PROFILE)
    },
    loadAdminUserFromStorage: (state, action: PayloadAction<Account>) => {
      state.isAuthenticated = true
      state.user = action.payload
      state.loading = false
      state.error = null
      state.isInitialized = true
    },
    setAdminInitialized: (state) => {
      state.isInitialized = true
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(adminLoginAsync.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(adminLoginAsync.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success && action.payload.user) {
          state.isAuthenticated = true
          state.user = action.payload.user
          state.error = null
          // Lưu vào localStorage với key riêng cho admin
          localStorage.setItem('admin_user', JSON.stringify(action.payload.user))
          localStorage.setItem('admin_isAuthenticated', 'true')
        } else {
          state.error = action.payload.message || 'Đăng nhập thất bại'
        }
      })
      .addCase(adminLoginAsync.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string || 'Đăng nhập thất bại'
      })
  },
})

export const { 
  adminLoginStart, 
  adminLoginSuccess, 
  adminLoginFailure, 
  adminLogout, 
  loadAdminUserFromStorage,
  setAdminInitialized
} = adminAuthSlice.actions

export default adminAuthSlice.reducer
