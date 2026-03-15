import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";
import { chatMessageAi, createSession, Message } from "@/services/geminiService/geminiService";


interface ChatMessageProps {
  onClose: () => void;
}
type DisplayMessage = { content: string; senderRole: 'CUSTOMER' | 'SYSTEM' }

const ChatMessage: React.FC<ChatMessageProps> = ({ onClose }) => {
  const [inputValue, setInputValue] = useState("");
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [messages, setMessages] = useState<DisplayMessage[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const messageEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    createSession()
      .then(data => setSessionId(data.sessionId))
      .catch(console.error)
  }, [])

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
        senderId: null,
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