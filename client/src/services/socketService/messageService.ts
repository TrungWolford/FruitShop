// client/src/services/socketService/messageService.ts
import axiosInstance from "@/libs/axios"
import { API } from '@/config/constants';

export interface SendMessagePayload {
  sessionId: string,
  senderId: string | null,
  content: string,
  senderRole: 'CUSTOMER' | 'ADMIN' | 'SYSTEM',
  messageType: 'TEXT',
  intent: string
}


export const messageService = {

  // tạo phiên chat mới 
  createSession: async (accountId?: string | null) => {
    const getAccountIdFromStorage = () => {
      const userStr = localStorage.getItem('user')
      if (!userStr) return null

      try {
        const userObj = JSON.parse(userStr)
        console.log('userStr', userStr)
        return userObj.accountId || userObj.id || null
      } catch (error) {
        console.error(error)
        return null
      }
    }

    const preferredAccountId = accountId !== undefined ? accountId : getAccountIdFromStorage()
    try {
      const response = await axiosInstance.post(API.SESSION, {
        accountId: preferredAccountId,
        title: null,
      })
      return response.data
    } catch (error: any) {
      const statusCode = error?.response?.status

      // Nếu accountId trong local/client không tồn tại trên BE, fallback tạo session guest để không chặn gửi tin.
      if (preferredAccountId && statusCode === 404) {
        const guestResponse = await axiosInstance.post(API.SESSION, {
          accountId: null,
          title: null,
        })
        return guestResponse.data
      }

      console.error('Lỗi khi tạo session:', error)
      throw error
    }
  },

  //  lấy lịch sử tin nhắn của một phiên chat
  getHistoryChat: async (sessionId: string) => {
    try {
      // Đổi thành API chuẩn của BE
      const response = await axiosInstance.get(`${API.MESSAGE}/${sessionId}`) 
      return response.data
    } catch (error) {
      console.error('Lỗi khi lấy lịch sử tin nhắn', error)
      throw error;
    }
  },

  sendMessage: async (payload: SendMessagePayload) => {
    try {
      const response = await axiosInstance.post(API.MESSAGE, payload)
      return response.data
    } catch (error) {
      console.error(error)
      throw error
    }
  }

}