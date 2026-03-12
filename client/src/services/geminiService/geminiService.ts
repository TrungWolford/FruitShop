import axiosInstance from '@/libs/axios';
import { API } from '@/config/constants';


export interface Message {
  role: 'user' | 'model'
  text: string,
}


export async function chatMessageAi(history: Message[]) {
  try {
    const respone = await axiosInstance.post(API.MESSAGE, {
      messages: history,
    })

    return respone.data
  } catch (error) {
    console.error('Lỗi khi gọi Chatbot từ Server:', error)
    throw error
  }
};



