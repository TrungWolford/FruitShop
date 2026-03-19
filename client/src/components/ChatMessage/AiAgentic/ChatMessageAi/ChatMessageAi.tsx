import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";
import { chatMessageAi, createSession, Message } from "@/services/geminiService/geminiService";
import { useAppSelector } from "@/hooks/redux";
import axiosInstance from "@/libs/axios";
import { API } from "@/config/constants";


interface ChatMessageProps {
  onClose: () => void;
}
type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'SYSTEM' }

// Key lưu sessionId theo từng user - AI CHAT dùng key riêng theo accountId
const getAiSessionKey = (accountId?: string | null) =>
  `chat_session_ai_${accountId || 'guest'}`

const ChatMessage: React.FC<ChatMessageProps> = ({ onClose }) => {
  const { user } = useAppSelector(state => state.auth); // Lấy user từ Redux
  const [inputValue, setInputValue] = useState("");
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [messages, setMessages] = useState<DisplayMessage[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const messageEndRef = useRef<HTMLDivElement>(null)
  const hasInitialized = useRef(false) // Chặn duplicate init từ StrictMode
  const currentUserIdRef = useRef<string | null>(null) // Track user ID để detect thay đổi

  // AI chat: dùng key riêng theo accountId để tránh conflict giữa users
  useEffect(() => {
    const currentUserId = user?.accountId ?? null;

    // Nếu user thay đổi (login/logout), reset state và cho phép init lại
    if (currentUserIdRef.current !== currentUserId) {
      console.log('[AI Chat] User changed, resetting...', { from: currentUserIdRef.current, to: currentUserId });
      currentUserIdRef.current = currentUserId;
      hasInitialized.current = false;
      setSessionId(null);
      setMessages([]);
    }

    // Kiểm tra xem đã init chưa, hoặc đã có sessionId rồi
    if (hasInitialized.current || sessionId !== null) {
      console.log('[AI Chat] Already initialized, skipping...', { hasInitialized: hasInitialized.current, sessionId });
      return;
    }
    hasInitialized.current = true;
    console.log('[AI Chat] Starting initialization...');

    const initAiSession = async () => {
      try {
        const sessionKey = getAiSessionKey(user?.accountId);
        const storedSessionId = localStorage.getItem(sessionKey);

        // ✅ Ưu tiên reuse session cũ nếu chưa được đóng (user mở-đóng mà chưa chat)
        if (storedSessionId) {
          try {
            // Đơn giản reuse session cũ - AI chat không cần validate phức tạp
            setSessionId(storedSessionId);
            console.log('[AI Chat] Reusing existing session:', storedSessionId);
            return;
          } catch {
            // Session hết hạn/không hợp lệ → xóa và tạo mới
            localStorage.removeItem(sessionKey);
          }
        }

        // Tạo session mới nếu không có session cũ
        const data = await createSession(user?.accountId ?? null);
        setSessionId(data.sessionId);
        localStorage.setItem(sessionKey, data.sessionId);
        console.log('[AI Chat] Created new session:', data.sessionId);
      } catch (error) {
        console.error('[AI Chat] Initialization failed:', error);
        hasInitialized.current = false; // Cho phép retry nếu lỗi
      }
    };

    void initAiSession();
  }, [user?.accountId, sessionId]) // Re-init nếu user thay đổi

  const handleSend = async () => {
    if (!inputValue.trim() || isLoading) return;

    const newUserMessage: DisplayMessage = { senderRole: 'CUSTOMER', content: inputValue.trim() }
    setMessages(prev => [...prev, newUserMessage])
    setInputValue("")
    setIsLoading(true)

    try {
      if (!sessionId) throw new Error('Session not ready')
      const payload: Message = {
        sessionId,
        content: newUserMessage.content,
        senderRole: 'CUSTOMER',
        messageType: 'TEXT',
        senderId: user?.accountId ?? null, // ✅ Gửi kèm accountId
      }
      const aiResponeText = await chatMessageAi(payload)

      const aiMessage: DisplayMessage = { content: aiResponeText.content, senderRole: 'SYSTEM' }
      setMessages(prev => [...prev, aiMessage])
    } catch (error) {
      console.error("Xin lỗi, vui lòng thử lại sau nhé!", error)
    } finally {
      setIsLoading(false);
    }
  }

  const handleEnter = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter')
      handleSend()
  }

  const handleClose = async () => {
    // ✅ Chỉ đóng session nếu đã có conversation (người dùng đã chat)
    if (sessionId && messages.length > 0) {
      try {
        console.log('[AI Chat] Closing session:', sessionId);
        // Không cần await - fire and forget
        fetch(`http://localhost:8080/api/chat/sessions/${sessionId}/close`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' }
        }).catch(err => console.warn('[AI Chat] Failed to close session:', err));

        // Xóa sessionId khỏi localStorage khi đóng session
        const sessionKey = getAiSessionKey(user?.accountId);
        localStorage.removeItem(sessionKey);
      } catch (error) {
        console.warn('[AI Chat] Error closing session:', error);
      }
    } else if (sessionId && messages.length === 0) {
      console.log('[AI Chat] Session not closed - no messages sent:', sessionId);
    }
    onClose(); // Đóng popup
  }

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isLoading])

  return (
    <div className="flex flex-col w-[325px] h-[454px] bg-white rounded-t-md shadow-xl">
      {/* Header chatbot */}
      <div className="h-[50px] shadow-sm p-2 border-b-2 flex relative bottom-0 rounded-t-lg">
        <div className="w-full h-full flex items-center">
          <img
            src={images?.chatBot}
            className="w-10 h-10 rounded-full object-cover mr-2"
          />
          <div className="flex flex-col">
            <span className="text-sm font-semibold">AI hỗ trợ</span>
          </div>
        </div>
        <button
          onClick={handleClose}
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
        {isLoading && (
          <div className="flex justify-start">
            <span className="inline-flex items-center gap-1 rounded-lg bg-gray-200 px-4 py-3">
              <span className="w-2 h-2 bg-gray-500 rounded-full animate-bounce [animation-delay:0ms]" />
              <span className="w-2 h-2 bg-gray-500 rounded-full animate-bounce [animation-delay:150ms]" />
              <span className="w-2 h-2 bg-gray-500 rounded-full animate-bounce [animation-delay:300ms]" />
            </span>
          </div>
        )}
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
          onClick={handleSend} disabled={isLoading || !inputValue}
        >
          <SendHorizontal />
        </button>
      </div>
    </div>
  );
};

export default ChatMessage;