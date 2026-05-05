package fruitshop.ai_service.orchestrator.model;

import lombok.Data;

/**
 * Chat request payload for the AI orchestrator.
 */
@Data
public class ChatRequest {
    private String sessionId;
    private String message;
    private String accountId;
}
