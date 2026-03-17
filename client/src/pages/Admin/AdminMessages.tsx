import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector } from '@/hooks/redux';
import { useStompChat } from '@/hooks/useStompChat';
import { adminChatService, AdminTicket } from '@/services/adminChatService/adminChatService';
import LeftTaskbar from '@/components/Admin/LeftTaskbar/LeftTaskbar';
import Header from '@/components/Admin/Header';
import Container from '@/components/Admin/Container';
import ChatBox from '@/components/Admin/Messages/ChatBox';
import images from '@/assets';
import { MessageSquare, RefreshCw } from 'lucide-react';

const TASKBAR_MARGIN_TOP = 'mt-[60px]';

const AdminMessages: React.FC = () => {
  const navigate = useNavigate();
  const { user: adminUser, isAuthenticated } = useAppSelector(state => state.adminAuth);

  const [tickets, setTickets] = useState<AdminTicket[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/admin/login');
    }
  }, [isAuthenticated, navigate]);

  const loadTickets = useCallback(async () => {
    try {
      setLoading(true);
      const data = await adminChatService.getAdminSessions(0, 50);
      setTickets(data.content ?? []);
    } catch (error) {
      console.error('Lỗi tải danh sách hội thoại:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTickets();
  }, [loadTickets]);

  // Nhận ticket mới real-time qua STOMP
  const { connected } = useStompChat('/topic/admin/new-ticket', (newSession: AdminTicket) => {
    setTickets(prev => {
      const exists = prev.some(t => t.sessionId === newSession.sessionId);
      if (exists) {
        return prev.map(t => t.sessionId === newSession.sessionId ? { ...t, ...newSession } : t);
      }
      return [newSession, ...prev];
    });
  });

  const selectedTicket = tickets.find(t => t.sessionId === selectedSessionId);

  const handleSelectTicket = async (ticket: AdminTicket) => {
    setSelectedSessionId(ticket.sessionId);
    try {
      await adminChatService.markAsRead(ticket.sessionId);
      setTickets(prev => prev.map(t =>
        t.sessionId === ticket.sessionId ? { ...t, unreadCount: 0 } : t
      ));
    } catch (_) { /* đọc tin thất bại không critical */ }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <LeftTaskbar className={TASKBAR_MARGIN_TOP} />
      <Container className="h-[calc(100vh-68px)]">
        <div className="flex h-full gap-0 bg-white rounded-lg shadow overflow-hidden">

          {/* Panel trái: danh sách hội thoại */}
          <div className="w-[300px] shrink-0 border-r flex flex-col">
            {/* Header panel trái */}
            <div className="h-14 px-4 border-b flex items-center justify-between shrink-0">
              <span className="font-semibold text-gray-800">Hội thoại</span>
              <button
                onClick={loadTickets}
                className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-500"
                title="Làm mới"
              >
                <RefreshCw className="w-4 h-4" />
              </button>
            </div>

            {/* Danh sách */}
            <div className="flex-1 overflow-y-auto">
              {loading ? (
                <div className="flex flex-col gap-2 p-3">
                  {[1, 2, 3, 4].map(i => (
                    <div key={i} className="h-16 bg-gray-100 rounded-lg animate-pulse" />
                  ))}
                </div>
              ) : tickets.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-gray-400 gap-2">
                  <MessageSquare className="w-10 h-10" />
                  <span className="text-sm">Chưa có hội thoại</span>
                </div>
              ) : (
                tickets.map(ticket => (
                  <button
                    key={ticket.sessionId}
                    onClick={() => handleSelectTicket(ticket)}
                    className={`w-full flex items-start gap-3 px-4 py-3 text-left hover:bg-orange-50 transition-colors border-b ${
                      selectedSessionId === ticket.sessionId ? 'bg-orange-50 border-l-2 border-l-orange-400' : ''
                    }`}
                  >
                    <img
                      src={images.humanSupport}
                      className="w-9 h-9 rounded-full object-cover shrink-0 mt-0.5"
                    />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1">
                        <span className="text-sm font-medium truncate">
                          {ticket.accountName || 'Khách vãng lai'}
                        </span>
                        {(ticket.unreadCount ?? 0) > 0 && (
                          <span className="shrink-0 text-[10px] bg-orange-400 text-white rounded-full px-1.5 py-0.5 font-semibold">
                            {ticket.unreadCount}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-gray-500 truncate mt-0.5">
                        {ticket.lastMessage || 'Chưa có tin nhắn'}
                      </p>
                      <div className="flex items-center gap-1 mt-0.5">
                        <StatusBadge status={ticket.status} />
                      </div>
                    </div>
                  </button>
                ))
              )}
            </div>
          </div>

          {/* Panel phải: chat */}
          <div className="flex-1 flex flex-col">
            {selectedSessionId && selectedTicket ? (
              <ChatBox
                sessionId={selectedSessionId}
                customerName={selectedTicket.accountName}
                onClose={() => setSelectedSessionId(null)}
                className="h-full"
              />
            ) : (
              <div className="flex flex-col items-center justify-center h-full text-gray-400 gap-3">
                <MessageSquare className="w-14 h-14 text-gray-300" />
                <span className="text-sm">Chọn một hội thoại để bắt đầu</span>
              </div>
            )}
          </div>

        </div>
      </Container>
    </div>
  );
};

const StatusBadge: React.FC<{ status?: number }> = ({ status }) => {
  const map: Record<number, { label: string; cls: string }> = {
    0: { label: 'Đã đóng', cls: 'bg-gray-100 text-gray-500' },
    1: { label: 'Đang mở', cls: 'bg-green-100 text-green-600' },
    2: { label: 'Chờ phản hồi', cls: 'bg-orange-100 text-orange-600' },
  };
  const s = map[status ?? -1] ?? { label: 'Không rõ', cls: 'bg-gray-100 text-gray-400' };
  return (
    <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium ${s.cls}`}>
      {s.label}
    </span>
  );
};

export default AdminMessages;
