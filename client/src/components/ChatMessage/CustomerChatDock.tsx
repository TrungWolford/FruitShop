import React, { useState } from 'react'
import AgentChatBot from '@/components/ChatMessage/AiAgentic'
import HumanSupport from '@/components/ChatMessage/HumanSupport'

type ActiveChat = 'ai' | 'human' | null

interface CustomerChatDockProps {
  className?: string
}

const CustomerChatDock: React.FC<CustomerChatDockProps> = ({ className }) => {
  const [activeChat, setActiveChat] = useState<ActiveChat>(null)

  return (
    <div className={`fixed z-50 bottom-0 right-5 flex flex-col items-center ${className ?? ''}`}>
      <AgentChatBot
        className={`${activeChat === 'human' ? 'hidden' : ''}`}
        isOpen={activeChat === 'ai'}
        onOpen={() => setActiveChat('ai')}
        onClose={() => setActiveChat(null)}
      />
      <HumanSupport
        className={`${activeChat === 'ai' ? 'hidden' : ''}`}
        isOpen={activeChat === 'human'}
        onOpen={() => setActiveChat('human')}
        onClose={() => setActiveChat(null)}
      />
    </div>
  )
}

export default CustomerChatDock