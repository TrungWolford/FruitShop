import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";

import { useStompChat } from "@/hooks/useStompChat";
import { useAppSelector } from "@/hooks/redux";
import { messageService } from "@/services/socketService/messageService";

interface ChatMessageProps {
  onClose: () => void;
}

type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'ADMIN' | 'SYSTEM' }

const mapToDisplayMessages = (history: any[]): DisplayMessage[] =>
  history.map((msg) => ({ content: msg.content, senderRole: msg.senderRole }))

// Key lưu sessionId theo từng user (hoặc guest) - HUMAN CHAT dùng key riêng
const getSessionKey = (accountId?: string | null) =>
  `chat_session_human_${accountId || 'guest'}`

const ChatMessageHuman: React.FC<ChatMessageProps> = ({ onClose }) => {
  const { user } = useAppSelector(state => state.auth);

  const [inputValue, setInputValue] = useState("");
  const [messages, setMessages] = useState<DisplayMessage[]>([])
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [isSending, setIsSending] = useState(false)
  const messageEndRef = useRef<HTMLDivElement>(null)
  const hasInitialized = useRef(false) // Chặn duplicate init - KHÔNG reset trong cleanup
  const currentUserIdRef = useRef<string | null>(null) // Track user ID để detect thay đổi

  // Khởi tạo session: ưu tiên reuse session cũ từ localStorage để tránh mất liên lạc với admin
  // HUMAN CHAT dùng key riêng: chat_session_human_{accountId}
  useEffect(() => {
    const currentUserId = user?.accountId ?? null;

    // Nếu user thay đổi (login/logout), reset state và cho phép init lại
    if (currentUserIdRef.current !== currentUserId) {
      console.log('[ChatMessageHuman] User changed, resetting...', { from: currentUserIdRef.current, to: currentUserId });
      currentUserIdRef.current = currentUserId;
      hasInitialized.current = false;
      setSessionId(null);
      setMessages([]);
    }

    // Kiểm tra xem đã init chưa, hoặc đã có sessionId rồi
    if (hasInitialized.current || sessionId !== null) {
      console.log('[ChatMessageHuman] Already initialized, skipping...', { hasInitialized: hasInitialized.current, sessionId });
      return;
    }
    hasInitialized.current = true;
    console.log('[ChatMessageHuman] Starting initialization...');
    const initSession = async () => {
      try {
        const sessionKey = getSessionKey(user?.accountId)
        const storedSessionId = localStorage.getItem(sessionKey)

        if (storedSessionId) {
          try {
            // Validate session còn tồn tại và load lịch sử
            const history = await messageService.getHistoryChat(storedSessionId)
            setSessionId(storedSessionId)
            setMessages(mapToDisplayMessages(history))
            return
          } catch {
            // Session hết hạn/không hợp lệ → xoá và tạo mới
            localStorage.removeItem(sessionKey)
          }
        }

        // Tạo session mới cho Human chat
        const data = await messageService.createSession(user?.accountId ?? null)
        setSessionId(data.sessionId)
        localStorage.setItem(sessionKey, data.sessionId)

        // Gửi tin tự động để kích hoạt Human Support mode ngay lập tức
        // Backend sẽ phản hồi với bot-ack: "Nhân viên FruitShop sẽ sớm liên hệ..."
        await messageService.sendMessage({
          sessionId: data.sessionId,
          senderId: user?.accountId ?? null,
          content: 'Tôi cần hỗ trợ từ nhân viên',
          senderRole: 'CUSTOMER',
          messageType: 'TEXT',
          intent: 'HUMAN_SUPPORT'
        })

        // Load lịch sử sau khi gửi tin tự động (sẽ có tin user + bot-ack)
        const history = await messageService.getHistoryChat(data.sessionId)
        setMessages(mapToDisplayMessages(history))
        console.log('[ChatMessageHuman] Initialization completed, sessionId:', data.sessionId)
      } catch (error) {
        console.error('[ChatMessageHuman] Initialization failed:', error)
        hasInitialized.current = false // Cho phép retry nếu lỗi
      }
    }
    void initSession()
  }, [user?.accountId, sessionId]) // Thêm sessionId vào dependency

  // Lắng nghe tin nhắn push từ BE qua STOMP (ADMIN + SYSTEM)
  const { connected, disconnect } = useStompChat(
    sessionId ? `/topic/session/${sessionId}` : null,
    (newMessage: DisplayMessage) => {
      // Hiển thị tin từ ADMIN và SYSTEM (bot-ack); tin CUSTOMER đã optimistic append
      if (newMessage.senderRole === 'ADMIN' || newMessage.senderRole === 'SYSTEM') {
        setMessages(prev => [...prev, newMessage]);
      }
    }
  )

  const handleSend = async () => {
    if (isSending || !inputValue.trim()) return;
    const content = inputValue.trim()

    const newUserMessage: DisplayMessage = { senderRole: 'CUSTOMER', content }
    setMessages(prev => [...prev, newUserMessage]);
    setInputValue("");
    setIsSending(true)

    let activeSessionId = sessionId

    try {
      if (!activeSessionId) {
        const sessionKey = getSessionKey(user?.accountId)
        const data = await messageService.createSession(user?.accountId ?? null)
        activeSessionId = data.sessionId
        setSessionId(data.sessionId)
        localStorage.setItem(sessionKey, data.sessionId)
      }

      await messageService.sendMessage({
        sessionId: activeSessionId!,
        senderId: user?.accountId ?? null,
        content,
        senderRole: 'CUSTOMER',
        messageType: 'TEXT',
        intent: 'HUMAN_SUPPORT'
      });

      // Sync messages sau khi gửi thành công để đảm bảo nhận được admin reply
      // (phòng trường hợp WebSocket bị disconnect/miss messages)
      try {
        const history = await messageService.getHistoryChat(activeSessionId!)
        const historyMessages = mapToDisplayMessages(history)

        // Chỉ update nếu có tin nhắn mới từ server (admin reply, bot ack, etc.)
        setMessages(prev => {
          // So sánh số lượng tin nhắn - nếu server có nhiều hơn local, update
          if (historyMessages.length > prev.length) {
            console.log('[ChatMessageHuman] Synced new messages from server:', historyMessages.length - prev.length)
            return historyMessages
          }
          return prev
        })
      } catch (syncError) {
        console.error("Failed to sync messages after send:", syncError)
        // Không throw - tin nhắn user vẫn đã được gửi thành công
      }
    } catch (error) {
      console.error("Lỗi gửi tin nhắn:", error)
      // Nếu gửi tin nhắn thất bại, reload toàn bộ history
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
    if (e.key === 'Enter') handleSend()
  }

  const handleClose = async () => {
    console.log('[ChatMessageHuman] Closing - disconnecting WebSocket for session:', sessionId)
    disconnect() // Ngắt kết nối WebSocket trước khi đóng

    // ✅ Chỉ đóng session nếu user đã thực sự chat (không tính tin nhắn tự động)
    // Tin nhắn tự động: "Tôi cần hỗ trợ từ nhân viên" được gửi khi tạo session mới
    const userMessages = messages.filter(msg => msg.senderRole === 'CUSTOMER')
    const hasRealUserMessages = userMessages.length > 1 ||
      (userMessages.length === 1 && userMessages[0].content !== 'Tôi cần hỗ trợ từ nhân viên')

    if (sessionId && hasRealUserMessages) {
      try {
        console.log('[ChatMessageHuman] Closing session:', sessionId)
        // Không cần await - fire and forget
        fetch(`http://localhost:8080/api/chat/sessions/${sessionId}/close`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' }
        }).catch(err => console.warn('[ChatMessageHuman] Failed to close session:', err))

        // Xóa sessionId khỏi localStorage
        const sessionKey = getSessionKey(user?.accountId)
        localStorage.removeItem(sessionKey)
      } catch (error) {
        console.warn('[ChatMessageHuman] Error closing session:', error)
      }
    } else if (sessionId && !hasRealUserMessages) {
      console.log('[ChatMessageHuman] Session not closed - no real user messages:', sessionId)
    }

    onClose()
  }

  useEffect(() => {
    messageEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages])

  return (
    <div className="flex flex-col w-[325px] h-[454px] bg-white rounded-t-md shadow-xl">
      {/* Header */}
      <div className="h-[50px] shadow-sm p-2 border-b-2 flex relative bottom-0 rounded-t-lg">
        <div className="w-full h-full flex items-center">
          <img src={images?.humanSupport} className="w-10 h-10 rounded-full object-cover mr-2" />
          <div className="flex flex-col">
            <span className="text-sm font-semibold">Nhân viên chăm sóc khách hàng</span>
            <span className={`text-[10px] font-medium ${connected ? 'text-green-500' : 'text-gray-400'}`}>
              {connected ? '● Đã kết nối' : '○ Đang kết nối...'}
            </span>
          </div>
        </div>
        <button
          onClick={handleClose}
          className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2]"
        >
          <X />
        </button>
      </div>

      {/* Tin nhắn */}
      <div className="flex-1 p-3 overflow-y-auto space-y-3">
        {messages.map((msg, index) => (
          <div key={index} className={`flex ${msg.senderRole === 'CUSTOMER' ? 'justify-end' : 'justify-start'}`}>
            <span className={`max-w-[80%] rounded-lg px-3 py-2 text-sm ${msg.senderRole === 'CUSTOMER' ? 'bg-[#FB923C] text-white' : 'bg-gray-200 text-[#111113]'}`}>
              {msg.content}
            </span>
          </div>
        ))}
        <div ref={messageEndRef} />
      </div>

      {/* Input */}
      <div className="flex items-center gap-2 p-2 border-t h-[60px] text-[#FB923C]">
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
