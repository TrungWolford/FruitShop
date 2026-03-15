import { API } from '@/config/constants';
import axiosInstance from '@/libs/axios';

export type SenderRole = 'CUSTOMER' | 'ADMIN' | 'SYSTEM';
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE';

export interface AdminTicket {
  sessionId: string;
  accountId?: string;
  accountName?: string;
  title?: string;
  status?: number;
  lastMessage?: string;
  unreadCount?: number;
  updatedAt?: string;
}

export interface AdminChatMessage {
  messageId?: string;
  sessionId: string;
  senderId?: string | null;
  senderName?: string;
  senderRole: SenderRole;
  content: string;
  messageType?: MessageType;
  createdAt?: string;
}

export interface ReplyTicketPayload {
  senderId?: string | null;
  content: string;
  messageType?: MessageType;
}

export interface AdminSessionsPage {
  content: AdminTicket[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const adminChatService = {
  getAdminSessions: async (page = 0, size = 20, status?: number) => {
    try {
      const response = await axiosInstance.get<AdminSessionsPage>(API.GET_ADMIN_SESSIONS, {
        params: { page, size, status }
      });
      return response.data;
    } catch (error) {
      console.error('Loi khi lay danh sach sessions cho admin:', error);
      throw error;
    }
  },

  getPendingTickets: async () => {
    try {
      const response = await axiosInstance.get<AdminTicket[]>(API.GET_PENDING_TICKETS);
      return response.data;
    } catch (error) {
      console.error('Loi khi lay danh sach ticket dang cho:', error);
      throw error;
    }
  },

  getTicketMessages: async (sessionId: string) => {
    try {
      const response = await axiosInstance.get<AdminChatMessage[]>(API.GET_TICKET_MESSAGES(sessionId));
      return response.data;
    } catch (error) {
      console.error('Loi khi lay lich su tin nhan ticket:', error);
      throw error;
    }
  },

  replyTicket: async (sessionId: string, payload: ReplyTicketPayload) => {
    try {
      const response = await axiosInstance.post<AdminChatMessage>(API.REPLY_TICKET(sessionId), {
        ...payload,
        senderRole: 'ADMIN',
        messageType: payload.messageType ?? 'TEXT'
      });
      return response.data;
    } catch (error) {
      console.error('Loi khi admin tra loi ticket:', error);
      throw error;
    }
  },

  markAsRead: async (sessionId: string) => {
    try {
      await axiosInstance.patch(API.MARK_SESSION_AS_READ(sessionId));
    } catch (error) {
      console.error('Loi khi danh dau da doc session:', error);
      throw error;
    }
  }
};

export default adminChatService;
