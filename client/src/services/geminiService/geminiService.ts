import axiosInstance from '@/libs/axios';
import { API } from '@/config/constants';
import { messageService } from '../socketService/messageService';

export interface Message {
  sessionId: string,
  content: string,
  senderRole: 'CUSTOMER' | 'SYSTEM',
  messageType: 'TEXT'
  senderId: string | null,
}

// Dùng chung createSession từ messageService, không duplicate nữa
export const createSession = messageService.createSession;

export async function chatMessageAi(message: Message) {
  try {
    const respone = await axiosInstance.post(API.MESSAGE, message)
    return respone.data
  } catch (error) {
    console.error('Lỗi khi gọi Chatbot từ Server:', error)
    throw error
  }
}
