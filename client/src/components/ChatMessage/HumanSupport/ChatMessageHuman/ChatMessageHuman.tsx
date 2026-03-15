import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";

import { useSocket } from "@/hooks/useSockets"; 
import { useAppSelector } from "@/hooks/redux";
import { messageService } from "@/services/socketService/messageService";

interface ChatMessageProps {
  onClose: () => void;
}
type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'ADMIN' | 'SYSTEM' }

const mapToDisplayMessages = (history: any[]): DisplayMessage[] => history.map((msg) => ({
  content: msg.content,
  senderRole: msg.senderRole
}))

const ChatMessageHuman: React.FC<ChatMessageProps> = ({ onClose }) => {
  const { user } = useAppSelector(state => state.auth);
  const roomId = user?.accountId || 'guest-123';

  const { socket } = useSocket(roomId)

  const [inputValue, setInputValue] = useState("");
  const [messages, setMessages] = useState<DisplayMessage[]>([])
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [isSending, setIsSending] = useState(false)
  const messageEndRef = useRef<HTMLDivElement>(null)

  // Tạo session + load lịch sử chat khi mount
  useEffect(() => {
    const initSession = async () => {
      try {
        const data = await messageService.createSession(user?.accountId ?? null)
        setSessionId(data.sessionId)
        const history = await messageService.getHistoryChat(data.sessionId)
        setMessages(mapToDisplayMessages(history))
      } catch (error) {
        console.error(error)
      }
    }

    void initSession()
  }, [user?.accountId])



  // Lắng nghe tin nhắn trả về
  useEffect(() => {
    if (!socket) return;

    socket.on('receiveMessage', (newMessage: DisplayMessage) => {
      // Chỉ hiển thị tin nhắn từ ADMIN, vì tin CUSTOMER đã được hiển thị ngay trong handleSend
      if (newMessage.senderRole === 'ADMIN') {
        setMessages(prev => [...prev, newMessage]);
      }
    })

    return () => {
      socket.off('receiveMessage')
    }
  }, [socket])

  const handleSend = async () => {
    if (isSending) return;
    if (!inputValue.trim()) return;
    const content = inputValue.trim()
    if (!content) return;

    const newUserMessage: DisplayMessage = { senderRole: 'CUSTOMER', content: content }
    setMessages(prev => [...prev, newUserMessage]);
    setInputValue("");
    setIsSending(true)

    let activeSessionId = sessionId

    try {
      if (!activeSessionId) {
        const data = await messageService.createSession(user?.accountId ?? null)
        activeSessionId = data.sessionId
        setSessionId(data.sessionId)
      }

      if (!activeSessionId) {
        throw new Error('Khong tao duoc phien chat')
      }

      const savedMessage = await messageService.sendMessage({
        sessionId: activeSessionId,
        senderId: user?.accountId ?? null,
        content,
        senderRole: 'CUSTOMER',
        messageType: 'TEXT',
        intent: 'HUMAN_SUPPORT'
      });
      if (socket) {
        socket.emit('sendMessage', {
          roomId: roomId,
          sessionId: activeSessionId,
          message: savedMessage
        });
      }
    } catch (error) {
      console.error("Loi gui tin nhan:", error)
      if (activeSessionId) {
        try {
          const history = await messageService.getHistoryChat(activeSessionId)
          setMessages(mapToDisplayMessages(history))
        } catch (syncError) {
          console.error(syncError)
        }
      }
    } finally {
      setIsSending(false)
    }

  }

  const handleEnter = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter')
      handleSend()
  }

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages])

  useEffect(() => {
    if (!sessionId) return;

    const timer = setInterval(async () => {
      try {
        const history = await messageService.getHistoryChat(sessionId);
        setMessages(mapToDisplayMessages(history));
      } catch (error) {
        console.error(error);
      }
    }, 2500);

    return () => clearInterval(timer);
  }, [sessionId]);

  return (
    <div className="flex flex-col w-[325px] h-[454px] bg-white rounded-t-md shadow-xl">
      {/* Header chatbot */}
      <div className="h-[50px] shadow-sm p-2 border-b-2 flex relative bottom-0 rounded-t-lg">
        <div className="w-full h-full flex items-center">
          <img
            src={images?.humanSupport}
            className="w-10 h-10 rounded-full object-cover mr-2"
          />
          <div className="flex flex-col">
            <span className="text-sm font-semibold">Nhân viên chăm sóc khách hàng</span>
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
          <div key={index} className={`flex ${msg.senderRole === 'CUSTOMER' ? 'justify-end' : 'justify-start'}`}>
            <span className={`max-w-[80%] rounded-lg px-3 py-2 ${msg.senderRole === 'CUSTOMER' ? 'bg-[#FB923C]' : 'bg-gray-200'}`}>
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
          onClick={handleSend}
          disabled={!inputValue.trim() || isSending}
        >
          <SendHorizontal />
        </button>
      </div>
    </div>
  );
};

export default ChatMessageHuman;