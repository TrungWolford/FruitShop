import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'
import type { Account } from '../../types/account'

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
})

export const { 
  loginStart, 
  loginSuccess, 
  loginFailure, 
  logout, 
  loadUserFromStorage 
} = authSlice.actions

export default authSlice.reducer
