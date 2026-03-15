import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";

import { useSocket } from "@/hooks/useSockets"; 
import { useAppSelector } from "@/hooks/redux";
import { adminChatService } from "@/services/adminChatService/adminChatService";

interface ChatMessageProps {
  onClose: () => void;
  sessionId: string;
  customerName?: string;
  className?: string
}
type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'ADMIN' }

const ChatMessage: React.FC<ChatMessageProps> = ({ onClose, sessionId, customerName, className }) => {
  const roomId = sessionId;
  const { user: adminUser } = useAppSelector((state) => state.adminAuth)

  const { socket } = useSocket(roomId)

  const [inputValue, setInputValue] = useState("");
  const [messages, setMessages] = useState<DisplayMessage[]>([])
  const messageEndRef = useRef<HTMLDivElement>(null)

  // Load lịch sử chat theo session được chọn
  useEffect(() => {
    if (!sessionId) return;

    adminChatService.getTicketMessages(sessionId)
      .then(history => {
        const historyList = Array.isArray(history) ? history : [];
        const mapped: DisplayMessage[] = historyList.map((msg: any) => ({
          content: msg.content,
          senderRole: msg.senderRole
        }))
        setMessages(mapped)
      })
      .catch(console.error)
  }, [sessionId])

  // Lắng nghe tin nhắn trả về
  useEffect(() => {
    if (!socket) return;

    socket.on('receiveMessage', (newMessage: DisplayMessage) => {
      // Tranh duplicate tin cua admin local: UI da optimistic append trong handleSend
      if (newMessage.senderRole !== 'ADMIN') {
        setMessages(prev => [...prev, newMessage]);
      }
    })

    return () => {
      socket.off('receiveMessage')
    }
  }, [socket])

  const handleSend = async () => {
    if (!inputValue.trim()) return;

    const newAdminMessage: DisplayMessage = { senderRole: 'ADMIN', content: inputValue.trim() }
    setMessages(prev => [...prev, newAdminMessage]);
    setInputValue("");

    if (socket) {
      socket.emit('sendMessage', {
        roomId: roomId,
        sessionId: sessionId,
        message: newAdminMessage
      });
      return;
    }

    try {
      await adminChatService.replyTicket(sessionId, {
        senderId: adminUser?.accountId,
        content: newAdminMessage.content,
        messageType: 'TEXT'
      })
    } catch (error) {
      console.error(error)
    }
  }

  const handleEnter = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter')
      handleSend()
  }

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages])

  return (
    <div className={`flex flex-col w-[325px] h-[454px] bg-white rounded-t-md shadow-xl ${className ?? ''}`}>
      {/* Header chatbot */}
      <div className="h-[50px] shadow-sm p-2 border-b-2 flex relative bottom-0 rounded-t-lg">
        <div className="w-full h-full flex items-center">
          <img
            src={images?.humanSupport}
            className="w-10 h-10 rounded-full object-cover mr-2"
          />
          <div className="flex flex-col">
            <span className="text-sm font-semibold">{customerName || 'Khách hàng'}</span>
            <span className="text-xs text-gray-500">Session: {sessionId}</span>
          </div>
        </div>
        <button
          onClick={onClose}
          className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2]"
        >
          <X />
        </button>
      </div>

      {/* Hiển thị thông tin chat */}
      <div className="flex-1 p-3 overflow-y-auto space-y-3">
        {messages.map((msg, index) => (
          <div key={index} className={`flex ${msg.senderRole === 'CUSTOMER' ? 'justify-start' : 'justify-end'}`}>
            <span className={`max-w-[80%] rounded-lg px-3 py-2 ${msg.senderRole === 'CUSTOMER' ? 'bg-gray-200' : 'bg-[#FB923C] text-white'}`}>
              {msg.content}
            </span>
          </div>
        ))}
        <div ref={messageEndRef}></div>
      </div>

      {/* Chat input */}
      <div className="flex items-center gap-2 p-2 border-t h-[60px] bottom-0 text-[#FB923C]">
        <input
          onKeyDown={handleEnter}
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Nhập tin nhắn..."
          className="flex-1 outline-none px-3 py-2 bg-gray-100 rounded-full text-[#111113] text-sm disabled:opacity-50"
        />
        <button
          className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2] disabled:opacity-30 transition-opacity"
          onClick={handleSend} disabled={ !inputValue}
        >
          <SendHorizontal />
        </button>
      </div>
    </div>
  );
};

export default ChatMessage;