import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import AppRoutes from './routes';
import AgentChatBot from './components/ChatMessage/AiAgentic';

const App: React.FC = () => {
  const [isChatOpen, setIsChatOpen] = useState(false);
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith('/admin');

  return (
    <>
      <AppRoutes />
      {!isAdminRoute && (
        <div className="fixed z-50 bottom-0 right-5 flex flex-col items-center">
          <AgentChatBot
            isOpen={isChatOpen}
            onOpen={() => setIsChatOpen(true)}
            onClose={() => setIsChatOpen(false)}
          />
        </div>
      )}
    </>
  );
};

export default App
