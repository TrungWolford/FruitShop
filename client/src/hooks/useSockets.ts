import { useEffect, useState } from 'react';
import { socketService } from '@/services/socketService/socketService';
import { Socket } from 'socket.io-client';

export const useSocket = (roomId: string | null) => {
  const [socket, setSocket] = useState<Socket | null> (null)
  
  useEffect(() => {
    if (!roomId) return;

    const activeSocket = socketService.connect(roomId);
    setSocket(activeSocket)

    return () => {socketService.disconnect()}
  }, [roomId])

  return { socket };
}