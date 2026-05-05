package fruitshop.ai_service.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Non-stream chat response wrapper.
 */
@Data
@AllArgsConstructor
public class ChatResponse {
    private String reply;
}
