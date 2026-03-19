import React, { useCallback, useEffect, useState } from 'react'
import { BarChart } from 'lucide-react'
import * as DropdownMenu from '@radix-ui/react-dropdown-menu'
import { useNavigate } from 'react-router-dom'
import images from '@/assets'
import { Settings, LogOut, Mail, Bell } from 'lucide-react'
import { useAppDispatch } from '@/hooks/redux'
import { adminLogout } from '@/store/slices/adminAuthSlice'
import ChatMessage from '../Messages/ChatBox'
import { adminChatService, type AdminTicket } from '@/services/adminChatService/adminChatService'


interface Header {
  className?: string
}

const Header: React.FC<Header> = ({ className }) => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const [activeChat, setActiveChat] = useState<{sessionId: string, name:string} | null>(null)
  const [tickets, setTickets] = useState<AdminTicket[]>([])
  const [loadingTickets, setLoadingTickets] = useState(false)


  const fetchPendingTickets = useCallback(async () => {
    try {
      setLoadingTickets(true)
      const data = await adminChatService.getPendingTickets()
      setTickets(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error(error)
      setTickets([])
    } finally {
      setLoadingTickets(false)
    }
  }, [])

  useEffect(() => {
    void fetchPendingTickets()
  }, [fetchPendingTickets])

  const handleOpenChat = async (ticket: AdminTicket) => {
    setActiveChat({
      sessionId: ticket.sessionId,
      name: ticket.accountName || ticket.title || 'Khach hang'
    })

    setTickets((prev) => prev.map((item) => {
      if (item.sessionId !== ticket.sessionId) return item
      return { ...item, unreadCount: 0 }
    }))

    try {
      await adminChatService.markAsRead(ticket.sessionId)
    } catch (error) {
      console.error(error)
    }
  }

  const handleAdminLogout = () => {
    dispatch(adminLogout())
    setActiveChat(null)
    navigate('/admin/login', { replace: true })
  }



  return (
    <>
      <div className={`flex items-center justify-between mb-3 shadow-md h-[60px] px-[15px] fixed top-0 left-0 w-full bg-white z-40 ${className ?? ''}`}>
        <div>
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <BarChart className="w-5 h-5 text-amber-500" />
            Dashboard 
          </h1>
          <p className="text-gray-600 mt-0.5 text-base">Tổng quan hệ thống quản lý Vựa trái cây</p>
        </div>

        <div className="flex gap-3 px-3">
          {/* //Tin nhắn */}
          <DropdownMenu.Root
            modal={false}
            onOpenChange={(open) => {
              if (open) void fetchPendingTickets()
            }}
          >
            <DropdownMenu.Trigger asChild>
              <button className="w-10 h-10 relative rounded-full p-2 bg-[#D6D9DD] hover:bg-[#c4c6c9] data-[state=open]:bg-[#DCE5EE]  flex items-center justify-center transition-colors">
                <Mail className="text-[#8E9093]"/>
                <span className={`absolute text-white w-5 h-5 -right-2 -top-2 text-xs font-bold p-2 bg-red-600 rounded-full flex items-center justify-center ${tickets.length <= 0 ? 'hidden' : ''}`}>
                  {tickets.length}
                </span>
              </button>
            </DropdownMenu.Trigger>
            <DropdownMenu.Content 
              className="bg-white border rounded-md shadow-lg p-2 mt-2 mr-3 w-[357px] h-[500px] z-50 overflow-y-auto">
              <DropdownMenu.Item className="flex items-center gap-2 px-1 py-2 outline-none cursor-pointer hover:bg-gray-100 rounded">
                <h1 className="font-bold text-xl">Tin nhắn</h1>
              </DropdownMenu.Item >

              <DropdownMenu.Separator className="h-px bg-gray-200 my-1" />

              {loadingTickets && (
                <div className="px-2 py-3 text-sm text-gray-500">Đang tải danh sách đoạn hội thoại...</div>
              )}

              {!loadingTickets && tickets.length === 0 && (
                <div className="px-2 py-3 text-sm text-gray-500">Chưa có ticket cần xử lý</div>
              )}

              {!loadingTickets && tickets.map((ticket) => (
                <DropdownMenu.Item
                  key={ticket.sessionId}
                  className="flex items-center gap-3 px-1 py-2 outline-none cursor-pointer hover:bg-gray-100 rounded"
                  onSelect={() => {
                    void handleOpenChat(ticket)
                  }}
                >
                  <img src={images.avatarDefault} className="w-10 h-10 rounded-full"/>
                  <div className="min-w-0">
                    <p className="font-bold text-sm truncate">{ticket.accountName || ticket.title || 'Khach hang'}</p>
                    <p className="text-xs truncate">{ticket.lastMessage}</p>
                  </div>
                  {(ticket.unreadCount || 0) > 0 && (
                    <span className="ml-auto min-w-5 h-5 px-1 flex items-center justify-center rounded-full bg-red-500 text-white text-xs">
                      {ticket.unreadCount}
                    </span>
                  )}
                </DropdownMenu.Item>
              ))}
            </DropdownMenu.Content>
          </DropdownMenu.Root>

          {/* // Thông báo  */}
          <DropdownMenu.Root modal={false}>
            <DropdownMenu.Trigger asChild>
              <button className="w-10 h-10 rounded-full p-2 bg-[#D6D9DD] hover:bg-[#c4c6c9] data-[state=open]:bg-[#DCE5EE] flex items-center justify-center transition-colors">
                <Bell className="text-[#8E9093]"/>
              </button>
            </DropdownMenu.Trigger>
            <DropdownMenu.Content 
              className="bg-white border rounded-md shadow-lg p-2 mt-2 mr-3 w-[357px] h-[500px] z-50">
              <DropdownMenu.Item className="flex items-center gap-2 px-1 py-2 outline-none cursor-pointer  rounded">
                <h1 className="font-bold text-xl">Thông báo</h1>
              </DropdownMenu.Item >
            

              <DropdownMenu.Separator className="h-px bg-gray-200 my-1" />
              <DropdownMenu.Item className="flex items-center gap-2 px-1 py-2 outline-none cursor-pointer hover:bg-gray-100 rounded">
              
              </DropdownMenu.Item>
            </DropdownMenu.Content>
          </DropdownMenu.Root>

          {/* //Trang cá nhân */}
          <DropdownMenu.Root modal={false}>
            <DropdownMenu.Trigger asChild>
              <button className="rounded-full">
                <img src={images.avatarDefault} className="rounded-full w-10 h-10" />
              </button>
            </DropdownMenu.Trigger>
            <DropdownMenu.Content 
              className="bg-white border rounded-md shadow-lg p-2 mt-2 mr-3 w-40 z-50">
              <DropdownMenu.Item className="flex items-center gap-2 px-1 py-2 outline-none cursor-pointer hover:bg-gray-100 rounded">
                <img src={images.avatarDefault} className="w-5 h-5 rounded-full"/>
                <span>Trang cá nhân</span>
              </DropdownMenu.Item >
              <DropdownMenu.Item className="flex items-center gap-2 px-1 py-2 outline-none cursor-pointer hover:bg-gray-100 rounded">
                <Settings className="w-5 h-5"/>
                Cài đặt
              </DropdownMenu.Item>

              <DropdownMenu.Separator className="h-px bg-gray-200 my-1" />
              <DropdownMenu.Item
                className="px-1 py-2 gap-2 flex items-center outline-none cursor-pointer hover:bg-red-50 text-red-600 rounded"
                onSelect={handleAdminLogout}
              >
                <LogOut className="w-5 h-5"/>
                Đăng xuất
              </DropdownMenu.Item>
            </DropdownMenu.Content>
          </DropdownMenu.Root>
        </div>
      </div>

      {activeChat && (
        <ChatMessage
          onClose={() => setActiveChat(null)}
          sessionId={activeChat.sessionId}
          customerName={activeChat.name}
          className="fixed bottom-0 right-5 z-[70]"
        />
      )}

    </>
  )
}

export default Header