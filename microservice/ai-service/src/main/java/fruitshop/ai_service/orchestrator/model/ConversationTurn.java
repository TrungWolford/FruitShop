package fruitshop.ai_service.orchestrator.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Conversation message stored in Redis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTurn {
    private String role;
    private String content;
    private Instant timestamp;

    public static ConversationTurn user(String content) {
        return new ConversationTurn("user", content, Instant.now());
    }

    public static ConversationTurn assistant(String content) {
        return new ConversationTurn("assistant", content, Instant.now());
    }
}
