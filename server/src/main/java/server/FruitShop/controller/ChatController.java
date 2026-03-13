package server.FruitShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.ChatBot.ChatRequest;
import server.FruitShop.dto.request.ChatBot.CreateSessionRequest;
import server.FruitShop.dto.request.ChatBot.UpdateSessionRequest;
import server.FruitShop.dto.response.ChatBot.ChatResponse;
import server.FruitShop.dto.response.ChatBot.ChatSessionDetailResponse;
import server.FruitShop.dto.response.ChatBot.ChatSessionResponse;
import server.FruitShop.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ================================================================
    // SESSION ENDPOINTS
    // ================================================================

    /**
     * POST /api/chat/sessions
     * Tạo session mới (customer / guest)
     * Body: { accountId, title }
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        ChatSessionResponse response = chatService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/chat/sessions/account/{accountId}
     * Lấy tất cả sessions của 1 account (customer xem lịch sử chat)
     */
    @GetMapping("/sessions/account/{accountId}")
    public ResponseEntity<List<ChatSessionResponse>> getSessionsByAccount(@PathVariable String accountId) {
        List<ChatSessionResponse> sessions = chatService.getSessionsByAccount(accountId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * GET /api/chat/sessions/{sessionId}
     * Lấy chi tiết session kèm toàn bộ messages
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionDetailResponse> getSessionDetail(@PathVariable String sessionId) {
        ChatSessionDetailResponse session = chatService.getSessionDetail(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * PUT /api/chat/sessions/{sessionId}
     * Cập nhật session (đổi title, trạng thái, reset unread)
     * Body: { title, status, resetUnread }
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponse> updateSession(@PathVariable String sessionId,
                                                              @RequestBody UpdateSessionRequest request) {
        ChatSessionResponse response = chatService.updateSession(sessionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/chat/sessions/{sessionId}/close
     * Đóng session (status = 0)
     */
    @PatchMapping("/sessions/{sessionId}/close")
    public ResponseEntity<ChatSessionResponse> closeSession(@PathVariable String sessionId) {
        ChatSessionResponse response = chatService.closeSession(sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/chat/sessions/{sessionId}
     * Xóa session (soft delete tất cả messages)
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        chatService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // MESSAGE ENDPOINTS
    // ================================================================

    /**
     * POST /api/chat/messages
     * Gửi tin nhắn — bot tự detect intent và sinh reply
     * Body: { sessionId, senderId, content, messageType, senderRole, intent, metadata }
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/chat/messages/{sessionId}
     * Lấy tất cả messages của 1 session
     */
    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<ChatResponse>> getMessages(@PathVariable String sessionId) {
        List<ChatResponse> messages = chatService.getMessagesBySession(sessionId);
        return ResponseEntity.ok(messages);
    }

    /**
     * DELETE /api/chat/messages/{messageId}
     * Thu hồi tin nhắn (soft delete)
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ChatResponse> deleteMessage(@PathVariable String messageId) {
        ChatResponse response = chatService.deleteMessage(messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/chat/sessions/{sessionId}/read
     * Đánh dấu tất cả tin nhắn trong session là đã đọc
     */
    @PatchMapping("/sessions/{sessionId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String sessionId) {
        chatService.markSessionAsRead(sessionId);
        return ResponseEntity.ok().build();
    }

    // ================================================================
    // ADMIN ENDPOINTS
    // ================================================================

    /**
     * GET /api/chat/admin/sessions
     * Admin lấy tất cả sessions (phân trang)
     * Query: ?page=0&size=20&status=2 (status optional)
     */
    @GetMapping("/admin/sessions")
    public ResponseEntity<Page<ChatSessionResponse>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatSessionResponse> sessions = (status != null)
                ? chatService.getSessionsByStatus(status, pageable)
                : chatService.getAllSessions(pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * POST /api/chat/admin/reply
     * Admin gửi reply vào session của customer
     * Body: { sessionId, senderId, content, messageType }
     */
    @PostMapping("/admin/reply")
    public ResponseEntity<ChatResponse> adminReply(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.adminReply(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/chat/admin/customer-care/tickets
     * Danh sách ticket đang chờ nhân viên trả lời trực tiếp (status=2)
     */
    @GetMapping("/admin/customer-care/tickets")
    public ResponseEntity<List<ChatSessionResponse>> getPendingCustomerCareTickets() {
        return ResponseEntity.ok(chatService.getPendingTickets());
    }

    /**
     * GET /api/chat/admin/customer-care/tickets/{sessionId}/messages
     * Lấy lịch sử tin nhắn của ticket để mở dialog chat
     */
    @GetMapping("/admin/customer-care/tickets/{sessionId}/messages")
    public ResponseEntity<List<ChatResponse>> getCustomerCareTicketMessages(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getTicketMessages(sessionId));
    }

    /**
     * POST /api/chat/admin/customer-care/tickets/{sessionId}/reply
     * Trả lời trực tiếp ticket của khách hàng
     * Body: { senderId, content, messageType }
     */
    @PostMapping("/admin/customer-care/tickets/{sessionId}/reply")
    public ResponseEntity<ChatResponse> replyCustomerCareTicket(@PathVariable String sessionId,
                                                                @RequestBody ChatRequest request) {
        request.setSessionId(sessionId);
        request.setSenderRole("ADMIN");
        ChatResponse response = chatService.adminReply(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
