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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public ChatServiceImpl(ChatSessionRepository chatSessionRepository,
                           ChatMessageRepository chatMessageRepository,
                           AccountRepository accountRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.accountRepository = accountRepository;
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

        // 3. Detect intent nếu chưa có
        String detectedIntent = detectIntent(request.getContent(), request.getIntent());

        // 4. Sinh bot reply dựa trên intent
        String botReply = generateBotReply(detectedIntent, request.getContent(), request.getMetadata());
        String botMetadata = generateBotMetadata(detectedIntent, request.getMetadata());

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
     * Detect intent từ nội dung tin nhắn (rule-based đơn giản).
     * TODO: Thay thế bằng gọi AI/NLP API (OpenAI, Gemini, Rasa...) để chính xác hơn.
     */
    private String detectIntent(String content, String hintIntent) {
        // Nếu frontend đã gợi ý intent → dùng luôn
        if (hintIntent != null && !hintIntent.isBlank()) return hintIntent;

        String lower = content.toLowerCase();

        if (lower.contains("so sánh") || lower.contains("compare"))       return "PRODUCT_COMPARE";
        if (lower.contains("đơn hàng") || lower.contains("order"))        return "ORDER_LOOKUP";
        if (lower.contains("đặt hàng") || lower.contains("mua"))          return "ORDER_PLACE";
        if (lower.contains("thanh toán") || lower.contains("payment"))     return "PAYMENT";
        if (lower.contains("gợi ý") || lower.contains("recommend"))        return "PRODUCT_SUGGEST";
        if (lower.contains("sản phẩm") || lower.contains("product"))       return "PRODUCT_ADVICE";

        return "GENERAL";
    }

    /**
     * Sinh nội dung text reply của bot theo intent.
     * TODO: Gọi AI API để tạo câu trả lời tự nhiên hơn.
     */
    private String generateBotReply(String intent, String userContent, String metadata) {
        if (intent == null) return "Xin chào! Tôi có thể giúp gì cho bạn?";

        return switch (intent) {
            case "PRODUCT_ADVICE"  -> "Bạn đang muốn tìm hiểu về sản phẩm nào? Tôi sẽ tư vấn cho bạn ngay!";
            case "PRODUCT_COMPARE" -> "Bạn muốn so sánh những sản phẩm nào? Hãy cho tôi biết tên hoặc mã sản phẩm nhé!";
            case "ORDER_LOOKUP"    -> "Bạn muốn tra cứu đơn hàng nào? Hãy cung cấp mã đơn hàng cho tôi nhé!";
            case "PRODUCT_SUGGEST" -> "Để gợi ý sản phẩm phù hợp, bạn có thể cho tôi biết ngân sách hoặc loại sản phẩm bạn muốn không?";
            case "ORDER_PLACE"     -> "Tôi sẽ hỗ trợ bạn đặt hàng! Bạn muốn mua sản phẩm gì?";
            case "PAYMENT"         -> "Shop hỗ trợ thanh toán qua MoMo và COD (thanh toán khi nhận hàng). Bạn muốn chọn phương thức nào?";
            default                -> "Tôi có thể tư vấn sản phẩm, so sánh giá, tra cứu đơn hàng hoặc hỗ trợ bạn đặt hàng. Bạn cần gì ạ?";
        };
    }

    /**
     * Sinh metadata JSON cho bot reply theo intent.
     * TODO: Truy vấn DB thực để lấy dữ liệu sản phẩm / đơn hàng và nhúng vào metadata.
     */
    private String generateBotMetadata(String intent, String requestMetadata) {
        // Placeholder — sẽ được implement khi tích hợp AI + truy vấn dữ liệu thực
        return null;
    }

    /** Cập nhật orderFlowState của session theo metadata gửi lên */
    private void updateOrderFlowState(ChatSession session, String metadata) {
        if (metadata == null) return;
        // TODO: Parse metadata JSON và cập nhật orderFlowState tương ứng
        // VD: {"orderFlowState": "CONFIRMING"} → session.setOrderFlowState("CONFIRMING")
        if (metadata.contains("CONFIRMING")) session.setOrderFlowState("CONFIRMING");
        else if (metadata.contains("PAYING")) session.setOrderFlowState("PAYING");
        else if (metadata.contains("DONE"))   session.setOrderFlowState("DONE");
        else                                  session.setOrderFlowState("SELECTING");
    }
}
