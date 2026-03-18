package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import server.FruitShop.dto.response.ChatBot.GeminiAgentResult;
import server.FruitShop.service.ChatService;
import server.FruitShop.service.ChatBot.GeminiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;
    private final GeminiService geminiService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatServiceImpl(ChatSessionRepository chatSessionRepository,
                           ChatMessageRepository chatMessageRepository,
                           AccountRepository accountRepository,
                           SimpMessagingTemplate messagingTemplate,
                           GeminiService geminiService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.accountRepository = accountRepository;
        this.messagingTemplate = messagingTemplate;
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

        String senderRole = request.getSenderRole() != null ? request.getSenderRole() : "CUSTOMER";

        // Nếu session đang ở chế độ chờ admin (status=2), skip Gemini hoàn toàn
        // — forward tin nhắn customer trực tiếp đến admin qua WebSocket
        if (session.getStatus() == 2 && "CUSTOMER".equalsIgnoreCase(senderRole)) {
            session.setLastMessage(request.getContent());
            session.setUnreadCount(session.getUnreadCount() + 1);
            chatSessionRepository.save(session);

            ChatResponse response = ChatResponse.fromEntity(userMessage);
            String destination = "/topic/session/" + session.getSessionId();
            try {
                messagingTemplate.convertAndSend(destination, response);
                log.info("Customer message broadcast successfully to {}", destination);
            } catch (Exception e) {
                log.error("Failed to forward customer message to admin on {}: {}", destination, e.getMessage(), e);
                // Không throw exception - tin nhắn đã lưu vào DB
            }
            return response;
        }

        // Gemini tự nhận diện intent từ nội dung tin nhắn (không tin vào frontend)
        if ("CUSTOMER".equalsIgnoreCase(senderRole)) {
            String detectedUserIntent = geminiService.detectIntent(request.getContent());
            if ("HUMAN_SUPPORT".equals(detectedUserIntent)) {
                // Cập nhật intent cho tin nhắn user đã lưu
                userMessage.setIntent("HUMAN_SUPPORT");
                chatMessageRepository.save(userMessage);

                // Chuyển session sang chờ nhân viên phản hồi
                session.setStatus(2); // 2 = Pending admin
                session.setLastMessage(request.getContent());
                session.setLastIntent("HUMAN_SUPPORT");
                session.setUnreadCount(session.getUnreadCount() + 1);

                // Bot xác nhận cho khách biết đang chuyển sang nhân viên
                String ackText = "Tôi đã ghi nhận yêu cầu của bạn. Nhân viên FruitShop sẽ sớm liên hệ và hỗ trợ bạn trực tiếp!";
                ChatMessage botAck = buildMessage(session, null, ackText, "SYSTEM", "TEXT", "HUMAN_SUPPORT", null);
                chatMessageRepository.save(botAck);

                chatSessionRepository.save(session);

                ChatResponse response = ChatResponse.fromEntity(botAck);

                // Notify customer: bot-ack message
                String destination = "/topic/session/" + session.getSessionId();
                try {
                    messagingTemplate.convertAndSend(destination, response);
                } catch (Exception e) {
                    log.error("Failed to send WebSocket bot-ack to {}: {}", destination, e.getMessage());
                }

                // Notify admin: new HUMAN_SUPPORT ticket
                try {
                    messagingTemplate.convertAndSend("/topic/admin/new-ticket", ChatSessionResponse.fromEntity(session));
                } catch (Exception e) {
                    log.error("Failed to send WebSocket new-ticket notify to admin: {}", e.getMessage());
                }

                return response;
            }
        }

        // Lấy lịch sử hội thoại từ DB (10 tin nhắn gần nhất) cho multi-turn context
        List<ChatMessage> conversationHistory =
            chatMessageRepository.findRecentMessagesBySessionId(session.getSessionId(), 10);
        if (conversationHistory != null) {
            java.util.Collections.reverse(conversationHistory); // Đảo để thứ tự cũ → mới
        }

        // 3. Agentic chat: Gemini tự gọi tool query DB và sinh câu trả lời
        // ✅ Truyền conversation history vào (MULTI-TURN)
        GeminiAgentResult agentResult = geminiService.agentChat(
            request.getContent(), 
            request.getSenderId(), 
            conversationHistory  // ← History được truyền vào đây
        );
        String detectedIntent = agentResult.intent();
        String botReply = agentResult.reply();
        String botMetadata = agentResult.metadata();

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

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Reply content must not be blank");
        }

        Account adminSender = null;
        if (request.getSenderId() != null) {
            adminSender = accountRepository.findById(request.getSenderId()).orElse(null);
        }

        ChatMessage adminMessage = buildMessage(session, adminSender, request.getContent(),
                "ADMIN", request.getMessageType() != null ? request.getMessageType() : "TEXT",
                null, null);
        chatMessageRepository.save(adminMessage);

        // Cập nhật preview tin nhắn cuối trong session
        // Giữ status=2 để customer tiếp tục nói chuyện với admin, không bị chuyển sang bot
        session.setLastMessage(request.getContent());
        session.setUnreadCount(0);
        chatSessionRepository.save(session);

        ChatResponse response = ChatResponse.fromEntity(adminMessage);

        // Broadcast tin nhắn của admin đến customer qua WebSocket
        String destination = "/topic/session/" + session.getSessionId();
        try {
            messagingTemplate.convertAndSend(destination, response);
            log.info("Admin message broadcast successfully to {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast admin message to {}: {}", destination, e.getMessage(), e);
            // Không throw exception - tin nhắn đã lưu vào DB, customer sẽ thấy khi reload
        }

        return response;
    }

    @Override
    public List<ChatSessionResponse> getPendingTickets() {
        return chatSessionRepository.findByStatusOrderByUpdatedAtDesc(2)
                .stream()
                .map(ChatSessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatResponse> getTicketMessages(String sessionId) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Chat session not found: " + sessionId);
        }
        return getMessagesBySession(sessionId);
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

    /** Cập nhật orderFlowState của session theo metadata gửi lên */
    private void updateOrderFlowState(ChatSession session, String metadata) {
        if (metadata == null) return;
        if (metadata.contains("CONFIRMING")) session.setOrderFlowState("CONFIRMING");
        else if (metadata.contains("PAYING")) session.setOrderFlowState("PAYING");
        else if (metadata.contains("DONE"))   session.setOrderFlowState("DONE");
        else                                  session.setOrderFlowState("SELECTING");
    }
}
