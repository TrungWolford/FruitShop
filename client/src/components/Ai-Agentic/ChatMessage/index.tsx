import React from "react";
import images from '@/assets'
import { X, FileImage, SendHorizontal } from 'lucide-react'

interface ChatMessageProps {
  onClose: () => void
}

const ChatMessage: React.FC<ChatMessageProps> = ({ onClose }) => {
  return (
    <div className="flex flex-col w-[325px] h-[454px] bg-white rounded-t-md">
      {/* Header chatbot */}
      <div className="h-[50px] shadow-sm p-2 border-b-2 flex relative bottom-0 rounded-t-lg">
        <div className="w-full h-full flex items-center">
          <img src={images?.chatBot} className="w-10 h-10 rounded-full object-cover mr-2"></img>
          AI hỗ trợ
        </div>
        <button onClick={onClose} className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2]"><X /></button>
      </div>

      {/* Hiển thị thông tin chat */}
      <div className="flex-1 p-2">
        hello
      </div>

      {/*  Chat text*/}
      <div className="flex items-center gap-2 p-2 border-t h-[60px] bottom-0 text-[#FB923C]">
        <label htmlFor="upload-image" className="cursor-pointe w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2]">
          <FileImage />
        </label>
        <input id="upload-image" type="file" accept="image/*" className="hidden" />
        <input type="text" name="" id="" placeholder="Nhập tin nhắn..." className="flex-1 outline-none px-2 py-1 bg-gray-100 rounded-md text-[#111113]" />
        <button className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-[#F2F2F2]" type="submit"><SendHorizontal /></button>
      </div>
    </div >
  )
}

export default ChatMessage