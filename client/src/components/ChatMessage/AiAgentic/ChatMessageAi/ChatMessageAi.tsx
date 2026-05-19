import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";
import { chatMessageAi, createSession, Message } from "@/services/geminiService/geminiService";
import { useAppSelector } from "@/hooks/redux";
import axiosInstance from "@/libs/axios";
import { API } from "@/config/constants";
import { useNavigate } from "react-router-dom";
import { cartService } from "@/services/cartService";
import type { CartItem as CartItemType } from "@/types/cart";


interface ChatMessageProps {
  onClose: () => void;
}
type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'SYSTEM'; timestamp: string }

const ACTION_REGEX = /\[ACTION:ADD_TO_CART_AND_CHECKOUT\|([^|]+)\|(\d+)\]/;

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
  const navigate = useNavigate();

  const formatTimestamp = (value: string) => {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    const pad = (num: number) => String(num).padStart(2, '0');
    return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  };

  const formatDateLabel = (value: string) => {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    const pad = (num: number) => String(num).padStart(2, '0');
    return `${pad(date.getDate())}/${pad(date.getMonth() + 1)}/${date.getFullYear()}`;
  };

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
            // Tải lịch sử tin nhắn cũ
            const response = await axiosInstance.get(`${API.MESSAGE}/${storedSessionId}`);
            const normalizedMessages = Array.isArray(response.data)
              ? response.data.map((msg: DisplayMessage) => {
                  const nextTimestamp = msg.timestamp ?? new Date().toISOString();
                  if (msg.senderRole !== 'SYSTEM' || typeof msg.content !== 'string') {
                    return { ...msg, timestamp: nextTimestamp };
                  }
                  const match = msg.content.match(ACTION_REGEX);
                  if (!match) return { ...msg, timestamp: nextTimestamp };
                  const quantity = parseInt(match[2], 10);
                  return {
                    ...msg,
                    timestamp: nextTimestamp,
                    content: `Đã thêm ${quantity} sản phẩm vào giỏ hàng. Mình sẽ chuyển bạn sang trang thanh toán nhé.`
                  };
                })
              : response.data;
            setMessages(normalizedMessages);
            setSessionId(storedSessionId);
            console.log('[AI Chat] Reusing existing session and loaded history:', storedSessionId);
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

    const newUserMessage: DisplayMessage = {
      senderRole: 'CUSTOMER',
      content: inputValue.trim(),
      timestamp: new Date().toISOString()
    }
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
      const aiResponeText = await chatMessageAi(payload);
      let responseText = aiResponeText.content;
      
      // Xử lý action từ AI Agent
      const actionMatch = responseText.match(ACTION_REGEX);
      if (actionMatch) {
        const productId = actionMatch[1];
        const quantity = parseInt(actionMatch[2], 10);
        
        // Cắt bỏ chuỗi ACTION khỏi tin nhắn hiển thị
        responseText = responseText.replace(actionMatch[0], '').trim();
        if (!responseText) {
          responseText = `Đã thêm ${quantity} sản phẩm vào giỏ hàng. Mình sẽ chuyển bạn sang trang thanh toán nhé.`;
        }

        // Gọi cart API để thêm/cập nhật vào giỏ hàng
        if (user?.accountId) {
          try {
            // Xóa giỏ hàng cũ để đảm bảo chỉ mua đúng số lượng/sản phẩm user vừa chat
            await cartService.clearCart(user.accountId);

            const cartItemsResponse = await cartService.getCartItems(user.accountId);
            let items: CartItemType[] = [];
            if (Array.isArray(cartItemsResponse.data)) {
              items = cartItemsResponse.data as CartItemType[];
            } else if (cartItemsResponse.data && typeof cartItemsResponse.data === 'object' && 'items' in cartItemsResponse.data) {
              const data = cartItemsResponse.data as { items?: CartItemType[] };
              items = data.items ?? [];
            }

            const existingItem = items.find((item) => item.productId === productId);

            if (existingItem?.cartItemId) {
              await cartService.updateCartItem({
                cartItemId: existingItem.cartItemId,
                quantity
              });
            } else {
              await cartService.addToCart({
                accountId: user.accountId,
                productId,
                quantity
              });
            }

            const productName = existingItem?.productName ?? 'sản phẩm';
            responseText = `Đã thêm ${quantity} quả ${productName} vào giỏ hàng. Mình sẽ chuyển bạn sang trang thanh toán nhé.`;
            console.log('[AI Chat] Added item to cart, navigating to checkout...');
            // Điều hướng đến trang thanh toán
            navigate('/checkout');
          } catch (e) {
            console.error('[AI Chat] Failed to add to cart:', e);
            responseText = "Xin lỗi, đã xảy ra lỗi khi thêm vào giỏ hàng.";
          }
        } else {
          responseText = "Vui lòng đăng nhập để đặt hàng nhé!";
        }
      }

      const aiMessage: DisplayMessage = {
        content: responseText,
        senderRole: 'SYSTEM',
        timestamp: new Date().toISOString()
      }
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

        // Không xóa sessionId để lần sau mở lại vẫn thấy lịch sử
        console.log('[AI Chat] Session kept in background:', sessionId);
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
        {messages.map((msg, index) => {
          const currentDateLabel = formatDateLabel(msg.timestamp);
          const previousDateLabel = index > 0 ? formatDateLabel(messages[index - 1].timestamp) : null;
          const showDateLabel = index === 0 || currentDateLabel !== previousDateLabel;

          return (
            <React.Fragment key={index}>
              {showDateLabel && (
                <div className="flex justify-center">
                  <span className="text-[11px] text-gray-400 bg-gray-100 px-3 py-1 rounded-full">
                    {currentDateLabel}
                  </span>
                </div>
              )}
              <div
                className={`flex flex-col ${msg.senderRole === 'CUSTOMER' ? 'items-end' : 'items-start'}`}
              >
                <span className={`max-w-[80%] rounded-lg px-3 py-2 text-sm whitespace-pre-wrap break-words ${msg.senderRole === 'CUSTOMER' ? 'bg-[#FB923C] text-white' : 'bg-gray-200 text-[#111113]'}`}>
                  {msg.content}
                </span>
                <span className="mt-1 text-[10px] text-gray-400">
                  {formatTimestamp(msg.timestamp)}
                </span>
              </div>
            </React.Fragment>
          );
        })}
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