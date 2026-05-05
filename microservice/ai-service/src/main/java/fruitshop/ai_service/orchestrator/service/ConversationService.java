package fruitshop.ai_service.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Stores and loads conversation history from Redis.
 */
@Service
public class ConversationService {

    private static final Duration HISTORY_TTL = Duration.ofHours(24);

    private final ReactiveRedisTemplate<String, String> redis;
    private final ObjectMapper objectMapper;

    public ConversationService(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis,
            ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        // Ensure JavaTimeModule is registered for Instant support
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Loads conversation history for a session.
     */
    public Mono<List<ConversationTurn>> loadHistory(String sessionId) {
        String key = key(sessionId);
        return redis.opsForList().range(key, 0, -1)
                .map(this::fromJson)
                .collectList();
    }

    /**
     * Appends a conversation turn and refreshes TTL.
     */
    public Mono<Void> appendTurn(String sessionId, ConversationTurn turn) {
        String key = key(sessionId);
        String json = toJson(turn);
        return redis.opsForList().rightPush(key, json)
                .then(redis.expire(key, HISTORY_TTL))
                .then();
    }

    private String key(String sessionId) {
        return "conversation:" + sessionId;
    }

    private String toJson(ConversationTurn turn) {
        try {
            return objectMapper.writeValueAsString(turn);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"role\":\"system\",\"content\":\"json_error\"}";
        }
    }

    private ConversationTurn fromJson(String json) {
        try {
            return objectMapper.readValue(json, ConversationTurn.class);
        } catch (Exception e) {
            return new ConversationTurn("system", "json_error", java.time.Instant.now());
        }
    }
}
