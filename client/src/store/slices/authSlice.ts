import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'
import type { Account } from '../../types/account'
import { authService, type LoginCredentials, type LoginResponse } from '../../services/authService'

interface AuthState {
  isAuthenticated: boolean
  user: Account | null
  loading: boolean
  error: string | null
}

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  loading: false,
  error: null,
}

// Async thunk for login
export const loginAsync = createAsyncThunk<LoginResponse, LoginCredentials>(
  'auth/loginAsync',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials)
      return response
    } catch (error: any) {
      return rejectWithValue(error.message || 'Đăng nhập thất bại')
    }
  }
)

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginStart: (state) => {
      state.loading = true
      state.error = null
    },
    loginSuccess: (state, action: PayloadAction<Account>) => {
      state.isAuthenticated = true
      state.user = action.payload
      state.loading = false
      state.error = null
      // Lưu vào localStorage
      localStorage.setItem('user', JSON.stringify(action.payload))
      localStorage.setItem('isAuthenticated', 'true')
    },
    loginFailure: (state, action: PayloadAction<string>) => {
      state.isAuthenticated = false
      state.user = null
      state.loading = false
      state.error = action.payload
    },
    logout: (state) => {
      state.isAuthenticated = false
      state.user = null
      state.loading = false
      state.error = null
      // Xóa khỏi localStorage
      localStorage.removeItem('user')
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    },
    loadUserFromStorage: (state, action: PayloadAction<Account>) => {
      state.isAuthenticated = true
      state.user = action.payload
      state.loading = false
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginAsync.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(loginAsync.fulfilled, (state, action) => {
        state.loading = false
        if (action.payload.success && action.payload.user) {
          state.isAuthenticated = true
          state.user = action.payload.user
          state.error = null
        } else {
          state.error = action.payload.message || 'Đăng nhập thất bại'
        }
      })
      .addCase(loginAsync.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string || 'Đăng nhập thất bại'
      })
  },
})

export const { 
  loginStart, 
  loginSuccess, 
  loginFailure, 
  logout, 
  loadUserFromStorage 
} = authSlice.actions

export default authSlice.reducer
