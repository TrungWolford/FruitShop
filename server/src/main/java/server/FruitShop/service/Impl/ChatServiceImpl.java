package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.ChatBot.ChatRequest;
import server.FruitShop.dto.request.ChatBot.CreateSessionRequest;
import server.FruitShop.dto.request.ChatBot.UpdateSessionRequest;
import server.FruitShop.dto.response.ChatBot.ChatResponse;
import server.FruitShop.dto.response.ChatBot.ChatSessionDetailResponse;
import server.FruitShop.dto.response.ChatBot.ChatSessionResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.ChatMessage;
import server.FruitShop.entity.ChatSession;
import server.FruitShop.exception.ResourceNotFoundException;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.ChatMessageRepository;
import server.FruitShop.repository.ChatSessionRepository;
import server.FruitShop.service.ChatService;
import server.FruitShop.service.GeminiService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;
    private final GeminiService geminiService;

    @Autowired
    public ChatServiceImpl(ChatSessionRepository chatSessionRepository,
                           ChatMessageRepository chatMessageRepository,
                           AccountRepository accountRepository,
                           GeminiService geminiService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.accountRepository = accountRepository;
        this.geminiService = geminiService;
    }

    // ================================================================
    // SESSION
    // ================================================================

    @Override
    @Transactional
    public ChatSessionResponse createSession(CreateSessionRequest request) {
        ChatSession session = new ChatSession();

        // Gắn account nếu không phải guest
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));
            session.setAccount(account);
        }

        // Tiêu đề mặc định nếu không truyền
        String title = (request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle()
                : "Cuộc hội thoại mới";
        session.setTitle(title);
        session.setStatus(1); // 1 = Đang mở

        chatSessionRepository.save(session);
        return ChatSessionResponse.fromEntity(session);
    }

    @Override
    public List<ChatSessionResponse> getSessionsByAccount(String accountId) {
        return chatSessionRepository
                .findByAccount_AccountIdOrderByUpdatedAtDesc(accountId)
                .stream()
                .map(ChatSessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ChatSessionDetailResponse getSessionDetail(String sessionId) {
        ChatSession session = chatSessionRepository.findByIdWithMessages(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        return ChatSessionDetailResponse.fromEntity(session);
    }

    @Override
    @Transactional
    public ChatSessionResponse updateSession(String sessionId, UpdateSessionRequest request) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            session.setTitle(request.getTitle());
        }
        if (request.getStatus() != null) {
            session.setStatus(request.getStatus());
        }
        if (request.isResetUnread()) {
            session.setUnreadCount(0);
            chatMessageRepository.markAllAsReadBySessionId(sessionId);
        }

        chatSessionRepository.save(session);
        return ChatSessionResponse.fromEntity(session);
    }

    @Override
    @Transactional
    public ChatSessionResponse closeSession(String sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        session.setStatus(0); // 0 = Đóng
        chatSessionRepository.save(session);
        return ChatSessionResponse.fromEntity(session);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        chatMessageRepository.softDeleteAllBySessionId(sessionId);
        chatSessionRepository.delete(session);
    }

    // ================================================================
    // MESSAGES
    // ================================================================

    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        // 1. Lấy hoặc tạo session
        ChatSession session;
        if (request.getSessionId() != null) {
            session = chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + request.getSessionId()));
        } else {
            // Tự tạo session mới
            session = new ChatSession();
            if (request.getSenderId() != null) {
                Account account = accountRepository.findById(request.getSenderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getSenderId()));
                session.setAccount(account);
            }
            session.setTitle("Cuộc hội thoại mới");
            session.setStatus(1);
            chatSessionRepository.save(session);
        }

        // 2. Lưu tin nhắn của user
        Account sender = null;
        if (request.getSenderId() != null) {
            sender = accountRepository.findById(request.getSenderId()).orElse(null);
        }

        ChatMessage userMessage = buildMessage(session, sender, request.getContent(),
                request.getSenderRole() != null ? request.getSenderRole() : "CUSTOMER",
                request.getMessageType() != null ? request.getMessageType() : "TEXT",
                request.getIntent(), request.getMetadata());
        chatMessageRepository.save(userMessage);

        // 3. Detect intent bằng Gemini (hoặc dùng hint từ frontend)
        String detectedIntent = (request.getIntent() != null && !request.getIntent().isBlank())
                ? request.getIntent()
                : geminiService.detectIntent(request.getContent());

        // 4. Sinh bot reply bằng Gemini (dataContext = null, sẽ mở rộng sau với dữ liệu DB thực)
        String botReply = geminiService.generateReply(detectedIntent, request.getContent(), request.getMetadata());
        String botMetadata = buildBotMetadata(detectedIntent, request.getContent());

        ChatMessage botMessage = buildMessage(session, null, botReply, "SYSTEM", "TEXT", detectedIntent, botMetadata);
        chatMessageRepository.save(botMessage);

        // 5. Cập nhật session context
        session.setLastMessage(botReply);
        session.setLastIntent(detectedIntent);
        session.setUnreadCount(session.getUnreadCount() + 1);

        // Cập nhật orderFlowState nếu là flow đặt hàng
        if ("ORDER_PLACE".equals(detectedIntent)) {
            updateOrderFlowState(session, request.getMetadata());
        }

        chatSessionRepository.save(session);

        return ChatResponse.fromEntity(botMessage);
    }

    @Override
    public List<ChatResponse> getMessagesBySession(String sessionId) {
        return chatMessageRepository
                .findByChatSession_SessionIdAndDeletedFalseOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(ChatResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatResponse deleteMessage(String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        message.setDeleted(true);
        chatMessageRepository.save(message);
        return ChatResponse.fromEntity(message);
    }

    @Override
    @Transactional
    public void markSessionAsRead(String sessionId) {
        chatMessageRepository.markAllAsReadBySessionId(sessionId);
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
        session.setUnreadCount(0);
        chatSessionRepository.save(session);
    }

    // ================================================================
    // ADMIN
    // ================================================================

    @Override
    public Page<ChatSessionResponse> getAllSessions(Pageable pageable) {
        return chatSessionRepository.findAllByOrderByUpdatedAtDesc(pageable)
                .map(ChatSessionResponse::fromEntity);
    }

    @Override
    public Page<ChatSessionResponse> getSessionsByStatus(int status, Pageable pageable) {
        return chatSessionRepository.findByStatusOrderByUpdatedAtDesc(status, pageable)
                .map(ChatSessionResponse::fromEntity);
    }

    @Override
    @Transactional
    public ChatResponse adminReply(ChatRequest request) {
        ChatSession session = chatSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + request.getSessionId()));

        Account adminSender = null;
        if (request.getSenderId() != null) {
            adminSender = accountRepository.findById(request.getSenderId()).orElse(null);
        }

        ChatMessage adminMessage = buildMessage(session, adminSender, request.getContent(),
                "ADMIN", request.getMessageType() != null ? request.getMessageType() : "TEXT",
                null, null);
        chatMessageRepository.save(adminMessage);

        // Cập nhật preview tin nhắn cuối trong session
        session.setLastMessage(request.getContent());
        session.setStatus(1); // Admin đã reply → session chuyển về mở
        chatSessionRepository.save(session);

        return ChatResponse.fromEntity(adminMessage);
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    /** Tạo đối tượng ChatMessage */
    private ChatMessage buildMessage(ChatSession session, Account sender, String content,
                                     String senderRole, String messageType,
                                     String intent, String metadata) {
        ChatMessage message = new ChatMessage();
        message.setChatSession(session);
        message.setSender(sender);
        message.setContent(content);
        message.setSenderRole(senderRole);
        message.setMessageType(messageType);
        message.setStatus(0); // 0 = Sent
        message.setIntent(intent);
        message.setMetadata(metadata);
        message.setDeleted(false);
        return message;
    }

    /**
     * Trích xuất keyword từ tin nhắn (dùng Gemini) và đóng gói thành metadata JSON.
     * Frontend dùng metadata này để hiển thị chip/button context (VD: tên sp, orderId).
     * TODO: Truy vấn DB thực lấy productId / orderId rồi nhúng vào đây.
     */
    private String buildBotMetadata(String intent, String userMessage) {
        if (intent == null) return null;
        return switch (intent) {
            case "PRODUCT_ADVICE", "PRODUCT_COMPARE", "ORDER_PLACE", "PRODUCT_SUGGEST" -> {
                String keyword = geminiService.extractProductKeyword(userMessage);
                yield "UNKNOWN".equals(keyword) ? null
                        : "{\"keyword\":\"" + keyword + "\"}";
            }
            case "ORDER_LOOKUP" -> {
                String orderId = geminiService.extractOrderId(userMessage);
                yield "UNKNOWN".equals(orderId) ? null
                        : "{\"orderId\":\"" + orderId + "\"}";
            }
            default -> null;
        };
    }

    /** Cập nhật orderFlowState của session theo metadata gửi lên */
    private void updateOrderFlowState(ChatSession session, String metadata) {
        if (metadata == null) return;
        if (metadata.contains("CONFIRMING")) session.setOrderFlowState("CONFIRMING");
        else if (metadata.contains("PAYING")) session.setOrderFlowState("PAYING");
        else if (metadata.contains("DONE"))   session.setOrderFlowState("DONE");
        else                                  session.setOrderFlowState("SELECTING");
    }
}
