package fruitshop.ai_service.controller;

import fruitshop.ai_service.orchestrator.model.ChatRequest;
import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import fruitshop.ai_service.orchestrator.model.TestChatResponse;
import fruitshop.ai_service.orchestrator.service.OrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final OrchestratorService orchestratorService;

    public ChatController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("AI Service is up - Version 5 (standard package)");
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatRequest request) {
        return orchestratorService.streamChat(request);
    }

    @PostMapping("/admin/test-chat")
    public Mono<TestChatResponse> testChat(@RequestBody ChatRequest request) {
        return orchestratorService.testChat(request);
    }

    @PostMapping("/sessions")
    public Map<String, String> createSession() {
        log.info("Creating new chat session (sync - std package).");
        try {
            String sessionId = UUID.randomUUID().toString();
            Map<String, String> response = new HashMap<>();
            response.put("sessionId", sessionId);
            return response;
        } catch (Exception e) {
            log.error("Error creating session", e);
            throw e;
        }
    }

    @GetMapping("/messages/{sessionId}")
    public Flux<Map<String, String>> getHistory(@PathVariable String sessionId) {
        return orchestratorService.getHistory(sessionId)
                .map(turn -> Map.of(
                        "content", turn.getContent(),
                        "senderRole", "user".equals(turn.getRole()) ? "CUSTOMER" : "SYSTEM"
                ));
    }

    @PostMapping("/messages")
    public Mono<Map<String, String>> sendMessage(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        String content = (String) payload.get("content");
        
        ChatRequest request = new ChatRequest();
        request.setSessionId(sessionId);
        request.setMessage(content);

        return orchestratorService.testChat(request)
                .map(resp -> Map.of("content", resp.getReply()));
    }

    @PatchMapping("/sessions/{sessionId}/close")
    public Mono<Void> closeSession(@PathVariable String sessionId) {
        return Mono.empty();
    }
}
