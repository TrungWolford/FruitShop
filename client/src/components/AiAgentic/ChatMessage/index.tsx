import React, { useState, useEffect, useRef } from "react";
import images from "@/assets";
import { X, SendHorizontal } from "lucide-react";
import { chatMessageAi, Message } from "@/services/geminiService/geminiService";


interface ChatMessageProps {
  onClose: () => void;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ onClose }) => {
  const [inputValue, setInputValue] = useState("");
  const [messages, setMessages] = useState<Message[]>([
    {
      role: 'model',
      text: 'Chào bạn! Mình là nhân viên của FruitShop. Mình có thể giúp gì cho bạn hôm nay? 🍎'
    }
  ])
  const [isLoading, setIsLoading] = useState(false)
  const messageEndRef = useRef<HTMLDivElement>(null)

  const handleSend = async () => {
    if (!inputValue.trim() || isLoading) return;

    const newUserMessage: Message = { role: 'user', text: inputValue }
    const updateMessages = [...messages, newUserMessage];
    setMessages(updateMessages)
    setInputValue("")
    setIsLoading(true)

    try {
      const aiResponeText = await chatMessageAi(updateMessages)

      setMessages(prev => [...prev, {
        role: 'model',
        text: aiResponeText
      }])
    } catch (error) {
      console.error("Xin lỗi, vui lòng thử lại sau nhé!", error)
    } finally {
      setIsLoading(false);
    }
  }

  const handleEnter = (e: any) => {
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
          <div key={index} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <span className={`max-w-[80%] rounded-lg px-3 py-2 ${msg.role === 'user' ? 'bg-[#FB923C]' : 'bg-gray-200'}`}>
              {msg.text}
            </span>
          </div>
        ))}
        {isLoading && <div className="text-gray-200 text-sm">Loading...</div>}
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