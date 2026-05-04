import React from 'react';
import { Badge } from '../../ui/badge';

interface ChatBubbleProps {
  role: 'user' | 'assistant';
  message: string;
  metadata?: {
    tools_called?: string[];
    rag_sources_used?: string[];
    latency_ms?: number;
  };
}

const ChatBubble: React.FC<ChatBubbleProps> = ({ role, message, metadata }) => {
  const isUser = role === 'user';

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`max-w-[80%] rounded-lg p-3 ${isUser ? 'bg-amber-500 text-white' : 'bg-white border text-gray-800'}`}>
        <p className="text-sm whitespace-pre-wrap">{message}</p>
        {!isUser && metadata && (
          <div className="mt-2 space-y-1 text-xs text-gray-500">
            <div className="flex flex-wrap gap-2">
              {(metadata.tools_called || []).map((tool) => (
                <Badge key={tool} variant="secondary">
                  {tool}
                </Badge>
              ))}
            </div>
            <div className="flex flex-wrap gap-2">
              {(metadata.rag_sources_used || []).map((source) => (
                <Badge key={source} className="bg-slate-700 text-white">
                  {source}
                </Badge>
              ))}
            </div>
            {typeof metadata.latency_ms === 'number' && (
              <p>Latency: {metadata.latency_ms}ms</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatBubble;
