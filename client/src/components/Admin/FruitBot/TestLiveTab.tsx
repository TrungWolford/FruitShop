import React, { useState } from 'react';
import { Button } from '../../ui/Button/Button';
import { Input } from '../../ui/input';
import { adminApi } from '../../../api/adminApi';
import ChatBubble from './ChatBubble';
import { Badge } from '../../ui/badge';

interface TestLiveTabProps {
  configName: string;
}

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  message: string;
  metadata?: {
    tools_called?: string[];
    rag_sources_used?: string[];
    latency_ms?: number;
  };
}

const TestLiveTab: React.FC<TestLiveTabProps> = ({ configName }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);

  const handleSend = async () => {
    if (!input.trim()) return;
    const userMessage: ChatMessage = {
      id: `${Date.now()}-user`,
      role: 'user',
      message: input
    };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');

    setSending(true);
    try {
      const response = await adminApi.testChat(userMessage.message, true);
      const aiMessage: ChatMessage = {
        id: `${Date.now()}-assistant`,
        role: 'assistant',
        message: response.reply || 'Khong co phan hoi',
        metadata: {
          tools_called: response.tools_called || [],
          rag_sources_used: response.rag_sources_used || [],
          latency_ms: response.latency_ms
        }
      };
      setMessages((prev) => [...prev, aiMessage]);
    } catch (error) {
      const failMessage: ChatMessage = {
        id: `${Date.now()}-assistant-error`,
        role: 'assistant',
        message: 'Khong the goi test-chat. Vui long thu lai.'
      };
      setMessages((prev) => [...prev, failMessage]);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium text-gray-700">Config dang dung</h3>
        <Badge className="bg-amber-500 text-white">{configName || 'FruitBot'}</Badge>
      </div>

      <div className="bg-slate-100 border rounded-lg p-4 space-y-3 min-h-[280px]">
        {messages.length === 0 && (
          <p className="text-sm text-gray-500">Nhap tin nhan de test nhanh.</p>
        )}
        {messages.map((msg) => (
          <ChatBubble key={msg.id} role={msg.role} message={msg.message} metadata={msg.metadata} />
        ))}
      </div>

      <div className="flex gap-2">
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Nhap tin nhan test"
        />
        <Button onClick={handleSend} disabled={sending} className="bg-amber-500 hover:bg-amber-600 text-white">
          {sending ? 'Dang gui...' : 'Gui'}
        </Button>
      </div>
    </div>
  );
};

export default TestLiveTab;
