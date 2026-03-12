import axiosInstance from '@/libs/axios';
import { API, CONFIG } from '@/config/constants';

export interface Message {
  sessionId: string,
  content: string,
  senderRole: 'CUSTOMER' | 'SYSTEM',
  messageType: 'TEXT'
  senderId: string | null,
}

export async function createSession() {
  const fullUrl = CONFIG.API_GATEWAY + API.SESSION;
  // #region agent log
  fetch('http://127.0.0.1:7556/ingest/00f57398-5cb8-4674-bcf1-2065b03c60ae',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'268d8b'},body:JSON.stringify({sessionId:'268d8b',location:'geminiService.ts:createSession',message:'createSession request',data:{fullUrl,baseUrl:CONFIG.API_GATEWAY,path:API.SESSION},timestamp:Date.now(),hypothesisId:'H3'})}).catch(()=>{});
  // #endregion
  try {
    const response = await axiosInstance.post(API.SESSION, {
      accoundId: null,
      title: null,
    });
    return response.data;
  } catch (error: unknown) {
    console.error('Lỗi khi tạo session chatbot:', error)
    throw error
  }
}


export async function chatMessageAi(message: Message) {
  try {
    const respone = await axiosInstance.post(API.MESSAGE, message)
    return respone.data
  } catch (error) {
    console.error('Lỗi khi gọi Chatbot từ Server:', error)
    throw error
  }
};



