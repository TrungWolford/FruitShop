import { io, Socket } from 'socket.io-client'

const SOCKET_SERVER_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000';

let socketInstance: Socket | null = null;
let currentRoomId: string | null = null;


export const socketService = {
  connect: (roomId: string) => {
    // Nếu roomId thay đổi (vd: đổi user), disconnect socket cũ rồi tạo mới
    if (socketInstance && currentRoomId !== roomId) {
      socketInstance.disconnect();
      socketInstance = null;
      currentRoomId = null;
    }

    if (!socketInstance) {
      socketInstance = io(SOCKET_SERVER_URL, {
        transports: ['websocket'],
        query: { roomId }
      });

      socketInstance.on('connect', () => {
        console.log(`Đã kết nối socket, room: ${roomId}`);
        socketInstance?.emit('join room', roomId)
      });

      socketInstance.on('disconnect', () => {
        console.log('Đã ngắt kết nối Socket');
      });

      currentRoomId = roomId;
    }
    return socketInstance
  },

  disconnect: () => {
    if (socketInstance) {
      socketInstance.disconnect();
      socketInstance=null;
    }
  },

  getSocket: () => {
    return socketInstance;
  }
}