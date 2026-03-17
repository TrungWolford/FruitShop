import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const getWsUrl = () => {
  const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  return `${base}/ws`;
};

/**
 * Hook kết nối STOMP WebSocket tới Spring Boot backend.
 * @param topic  STOMP topic cần subscribe, ví dụ: /topic/session/{id} | /topic/admin/new-ticket
 * @param onMessage  Callback nhận payload đã parse (object)
 */
export const useStompChat = (
  topic: string | null,
  onMessage: (msg: any) => void
) => {
  const [connected, setConnected] = useState(false);
  // Giữ callback trong ref để tránh re-subscribe khi hàm thay đổi
  const onMessageRef = useRef(onMessage);
  onMessageRef.current = onMessage;

  // Ref để lưu client instance, cho phép disconnect từ bên ngoài
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!topic) {
      console.log('[STOMP] topic is null, skip connect');
      return;
    }

    console.log('[STOMP] Connecting to', getWsUrl(), '| topic:', topic);

    const client = new Client({
      webSocketFactory: () => new SockJS(getWsUrl()),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('[STOMP] Connected ✓ | subscribing to', topic);
        setConnected(true);
        client.subscribe(topic, (frame) => {
          console.log('[STOMP] Message received on', topic, ':', frame.body);
          try {
            onMessageRef.current(JSON.parse(frame.body));
          } catch (e) {
            console.error('[STOMP] parse error:', e);
          }
        });
      },
      onDisconnect: () => {
        console.warn('[STOMP] Disconnected');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('[STOMP] STOMP error:', frame.headers['message'], frame);
      },
      onWebSocketError: (event) => {
        console.error('[STOMP] WebSocket error:', event);
      },
      onWebSocketClose: (event) => {
        console.warn('[STOMP] WebSocket closed:', event.code, event.reason);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      console.log('[STOMP] Deactivating client for topic:', topic);
      client.deactivate();
      clientRef.current = null;
    };
  }, [topic]);

  // Method để ngắt kết nối chủ động từ bên ngoài
  const disconnect = () => {
    if (clientRef.current?.connected) {
      console.log('[STOMP] Manual disconnect triggered');
      clientRef.current.deactivate();
      clientRef.current = null;
      setConnected(false);
    }
  };

  return { connected, disconnect };
};
