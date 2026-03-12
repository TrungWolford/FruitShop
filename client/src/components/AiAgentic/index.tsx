import React from 'react'
import images from '../../assets'
import ChatMessage from './ChatMessage'
import { useState } from 'react'

const AgentChatBot: React.FC = () => {
  const [chatbotMessage, setChatbotMessage] = useState(true)
  return (
    <div className="fixed bottom-0 right-6 z-50 cursor-pointer">
      {chatbotMessage ?
        <button onClick={() => { setChatbotMessage(!chatbotMessage) }}>
          <img
            src={images.chatBot}
            alt="ảnh chatbot"
            className="w-16 h-16 object-cover shadow-lg rounded-full mb-7"
          />
        </button>
        : <ChatMessage onClose={() => setChatbotMessage(true)} />
      }
    </div>
  )
}

export default AgentChatBot