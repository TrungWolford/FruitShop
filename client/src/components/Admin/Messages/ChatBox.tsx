import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";

import { useStompChat } from "@/hooks/useStompChat";
import { useAppSelector } from "@/hooks/redux";
import { adminChatService } from "@/services/adminChatService/adminChatService";

interface ChatBoxProps {
  onClose: () => void;
  sessionId: string;
  customerName?: string;
  className?: string;
}

type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'ADMIN' }

const ChatBox: React.FC<ChatBoxProps> = ({ onClose, sessionId, customerName, className }) => {
  const { user: adminUser } = useAppSelector((state) => state.adminAuth)

  const [inputValue, setInputValue] = useState("");
  const [messages, setMessages] = useState<DisplayMessage[]>([])
  const [isSending, setIsSending] = useState(false)
  const messageEndRef = useRef<HTMLDivElement>(null)

  // Load lịch sử chat theo session
  useEffect(() => {
    if (!sessionId) return;
    adminChatService.getTicketMessages(sessionId)
      .then(history => {
        const historyList = Array.isArray(history) ? history : [];
        setMessages(historyList.map((msg: any) => ({
          content: msg.content,
          senderRole: msg.senderRole
        })))
      })
      .catch(console.error)
  }, [sessionId])

  // Lắng nghe tin nhắn mới từ customer qua STOMP
  const { connected, disconnect } = useStompChat(
    `/topic/session/${sessionId}`,
    (newMessage: DisplayMessage) => {
      // Tránh duplicate: tin ADMIN đã optimistic append trong handleSend
      if (newMessage.senderRole !== 'ADMIN') {
        setMessages(prev => [...prev, newMessage]);
      }
    }
  )

  const handleSend = async () => {
    if (!inputValue.trim() || isSending) return;
    const content = inputValue.trim()

    const newAdminMessage: DisplayMessage = { senderRole: 'ADMIN', content }
    setMessages(prev => [...prev, newAdminMessage]);
    setInputValue("");
    setIsSending(true)

    try {
      await adminChatService.replyTicket(sessionId, {
        senderId: adminUser?.accountId,
        content,
        messageType: 'TEXT'
      })
    } catch (error) {
      console.error("Lỗi gửi phản hồi:", error)
    } finally {
      setIsSending(false)
    }
  }

  // Kết thúc hỗ trợ: trả session về chế độ bot trước khi đóng UI
  const handleClose = async () => {
    console.log('[ChatBox] Closing - disconnecting WebSocket for session:', sessionId)
    disconnect() // Ngắt kết nối WebSocket trước khi đóng
    await adminChatService.resolveTicket(sessionId)
    onClose()
  }

  const handleEnter = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSend()
  }

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages])

  return (
    <div className={`flex flex-col w-full h-full bg-white ${className ?? ''}`}>
      {/* Header */}
      <div className="h-[56px] shrink-0 px-4 border-b flex items-center gap-3">
        <img src={images?.humanSupport} className="w-9 h-9 rounded-full object-cover" />
        <div className="flex flex-col flex-1 min-w-0">
          <span className="text-sm font-semibold truncate">{customerName || 'Khách hàng'}</span>
          <span className="text-xs text-gray-400 truncate">Session: {sessionId}</span>
        </div>
        <button
          onClick={handleClose}
          className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Tin nhắn */}
      <div className="flex-1 p-4 overflow-y-auto space-y-3">
        {messages.map((msg, index) => (
          <div key={index} className={`flex ${msg.senderRole === 'CUSTOMER' ? 'justify-start' : 'justify-end'}`}>
            <span className={`max-w-[80%] rounded-lg px-3 py-2 text-sm ${msg.senderRole === 'CUSTOMER' ? 'bg-gray-100 text-[#111113]' : 'bg-[#FB923C] text-white'}`}>
              {msg.content}
            </span>
          </div>
        ))}
        <div ref={messageEndRef} />
      </div>

      {/* Input */}
      <div className="shrink-0 flex items-center gap-2 px-3 py-2 border-t text-[#FB923C]">
        <input
          onKeyDown={handleEnter}
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Nhập tin nhắn..."
          className="flex-1 outline-none px-3 py-2 bg-gray-100 rounded-full text-[#111113] text-sm"
        />
        <button
          className="w-9 h-9 flex items-center justify-center rounded-full hover:bg-gray-100 disabled:opacity-30 transition-opacity"
          onClick={handleSend}
          disabled={!inputValue.trim() || isSending}
        >
          <SendHorizontal className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};

export default ChatBox;
