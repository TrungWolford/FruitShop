
import images from '@/assets'
import ChatMessageHuman from './ChatMessageHuman/ChatMessageHuman'

interface AgentChatBotProps {
className?: string;
isOpen: boolean;
onOpen: () => void;
onClose: () => void;
}


const AgentChatBot: React.FC<AgentChatBotProps> = ({ className, isOpen, onOpen, onClose }) => {
  
  return (
    <div className={'bottom-0 ' + (className ?? '') }>
      {isOpen ? (
        <ChatMessageHuman onClose={onClose} /> ) : (
        <button className="cursor-pointer" onClick={onOpen}>
          <img src={images.humanSupport} alt="ảnh human" className="w-16 h-16 object-cover shadow-lg rounded-full mb-7" />
        </button>
      )}
    </div>
    
  )
}

export default AgentChatBot