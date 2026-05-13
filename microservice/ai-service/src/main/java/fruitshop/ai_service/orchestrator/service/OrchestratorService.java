package fruitshop.ai_service.orchestrator.service;

import fruitshop.ai_service.orchestrator.client.AIConfigClient;
import fruitshop.ai_service.orchestrator.model.AIConfig;
import fruitshop.ai_service.orchestrator.model.ChatRequest;
import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import fruitshop.ai_service.orchestrator.model.RAGChunk;
import fruitshop.ai_service.orchestrator.model.TestChatResponse;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import fruitshop.ai_service.orchestrator.config.AIConfigChangedEvent;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Orchestrates config, RAG, prompt building, LLM call, and persistence.
 */
@Service
public class OrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorService.class);

    private final AIConfigClient aiConfigClient;
    private final VectorStoreService vectorStoreService;
    private final PromptBuilderService promptBuilderService;
    private final LLMService llmService;
    private final ConversationService conversationService;
    private final RetryRegistry retryRegistry;
    private final AdminAiConfigStore adminAiConfigStore;

    public OrchestratorService(
            AIConfigClient aiConfigClient,
            VectorStoreService vectorStoreService,
            PromptBuilderService promptBuilderService,
            LLMService llmService,
            ConversationService conversationService,
            RetryRegistry retryRegistry,
            AdminAiConfigStore adminAiConfigStore) {
        this.aiConfigClient = aiConfigClient;
        this.vectorStoreService = vectorStoreService;
        this.promptBuilderService = promptBuilderService;
        this.llmService = llmService;
        this.conversationService = conversationService;
        this.retryRegistry = retryRegistry;
        this.adminAiConfigStore = adminAiConfigStore;
    }

    /**
     * Streams chat responses via SSE.
     */
    public Flux<ServerSentEvent<String>> streamChat(ChatRequest request) {
        long start = System.nanoTime();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : "test-session";

        AIConfig config = getActiveConfig();

        Mono<List<RAGChunk>> ragMono = Mono.fromCallable(() -> {
                    try {
                        return vectorStoreService.similaritySearch(request.getMessage(), 4)
                                .stream()
                                .map(content -> new RAGChunk(content, "PDF Document", 1.0))
                                .toList();
                    } catch (Exception e) {
                        log.warn("RAG search failed: {}", e.getMessage());
                        return List.<RAGChunk>of();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(List.of());

        Mono<List<ConversationTurn>> historyMono = conversationService
                .loadHistory(sessionId)
                .timeout(Duration.ofSeconds(1))
                .onErrorReturn(List.of())
                .defaultIfEmpty(List.of());

        return Mono.zip(ragMono, historyMono)
                .flatMapMany(tuple -> {
                    List<RAGChunk> ragChunks = tuple.getT1();
                    List<ConversationTurn> history = tuple.getT2();
                    String systemPrompt = promptBuilderService.buildSystemPrompt(config, ragChunks, request.getMessage());

                    Retry retry = retryRegistry.retry("llmApi");
                    AtomicReference<StringBuilder> buffer = new AtomicReference<>(new StringBuilder());

                    Mono<Void> saveUser = conversationService
                            .appendTurn(sessionId, ConversationTurn.user(request.getMessage()))
                            .onErrorResume(e -> {
                                log.warn("Failed to save user message: {}", e.getMessage());
                                return Mono.empty();
                            });

                    Flux<String> responseStream = llmService.streamChat(systemPrompt, history, request.getMessage())
                            .doOnNext(token -> buffer.get().append(token))
                            .transformDeferred(RetryOperator.of(retry));

                    return saveUser.thenMany(responseStream)
                            .map(token -> ServerSentEvent.builder(token).event("message").build())
                            .doOnComplete(() -> {
                                String fullResponse = buffer.get().toString();
                                if (!fullResponse.isBlank()) {
                                    conversationService
                                            .appendTurn(sessionId, ConversationTurn.assistant(fullResponse))
                                            .subscribe();
                                }
                            })
                            .doFinally(signalType -> {
                                long latencyMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
                                log.info("ai_orchestrator stream sessionId={} latencyMs={} tokens_approx={}",
                                        sessionId, latencyMs, buffer.get().length());
                            });
                })
                .onErrorResume(e -> {
                    log.error("Stream chat failed", e);
                    return Flux.just(ServerSentEvent.builder("Lỗi hệ thống: " + e.getMessage()).event("message").build());
                });
    }

    /**
     * Synchronous chat for admin testing, returns full JSON instead of stream.
     */
    public Mono<TestChatResponse> testChat(ChatRequest request) {
        long start = System.nanoTime();
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank()) 
                ? request.getSessionId() : "test-session";
        
        AIConfig config = getActiveConfig();

        Mono<List<RAGChunk>> ragMono = Mono.fromCallable(() -> {
                    try {
                        return vectorStoreService.similaritySearch(request.getMessage(), 4)
                                .stream()
                                .map(content -> new RAGChunk(content, "PDF Document", 1.0))
                                .toList();
                    } catch (Exception e) {
                        return List.<RAGChunk>of();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(List.of());

        Mono<List<ConversationTurn>> historyMono = conversationService.loadHistory(sessionId)
                .timeout(Duration.ofSeconds(1))
                .onErrorReturn(List.of());

        return Mono.zip(ragMono, historyMono).flatMap(tuple -> {
            List<RAGChunk> ragChunks = tuple.getT1();
            List<ConversationTurn> history = tuple.getT2();
            
            String systemPrompt = promptBuilderService.buildSystemPrompt(config, ragChunks, request.getMessage());
            
            // Save user message
            Mono<Void> saveUser = conversationService.appendTurn(sessionId, ConversationTurn.user(request.getMessage()))
                    .onErrorResume(e -> Mono.empty());

            return saveUser.then(
                llmService.streamChat(systemPrompt, history, request.getMessage())
                    .collectList()
                    .map(tokens -> String.join("", tokens))
                    .flatMap(reply -> {
                        // Save assistant message
                        return conversationService.appendTurn(sessionId, ConversationTurn.assistant(reply))
                                .thenReturn(TestChatResponse.builder()
                                    .reply(reply)
                                    .tools_called(List.of("rag"))
                                    .rag_sources_used(ragChunks.stream().map(RAGChunk::getSource).distinct().toList())
                                    .latency_ms(Duration.ofNanos(System.nanoTime() - start).toMillis())
                                    .config_name(adminAiConfigStore.hasCustomConfig() ? "Custom Admin Prompt" : "System Default")
                                    .build());
                    })
            );
        }).onErrorResume(e -> {
            log.error("Test chat failed", e);
            TestChatResponse errorResp = TestChatResponse.builder()
                    .reply("Lỗi hệ thống: " + e.getMessage())
                    .latency_ms(Duration.ofNanos(System.nanoTime() - start).toMillis())
                    .config_name("Error")
                    .build();
            return Mono.just(errorResp);
        });
    }

    public Flux<ConversationTurn> getHistory(String sessionId) {
        return conversationService.loadHistory(sessionId)
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * Loads AI config — priority: AdminAiConfigStore (admin UI) > Feign config-service > default.
     */
    @Cacheable(cacheNames = "aiConfig", key = "'ai_config_current'")
    public AIConfig getActiveConfig() {
        // 1. Admin has explicitly set a config via UI
        if (adminAiConfigStore.hasCustomConfig()) {
            return adminAiConfigStore.get();
        }
        // 2. Fallback to config-service via Feign
        try {
            return aiConfigClient.getActiveConfig();
        } catch (Exception e) {
            log.warn("ai_orchestrator cannot reach config-service, using in-memory store", e);
            return adminAiConfigStore.get();
        }
    }

    @EventListener
    @CacheEvict(cacheNames = "aiConfig", allEntries = true)
    public void onConfigChanged(AIConfigChangedEvent event) {
        log.info("AI configuration changed, evicting cache.");
    }

    private AIConfig defaultConfig() {
        AIConfig config = new AIConfig();
        config.setSystemPrompt("Ban la tro ly AI ho tro cua hang FruitShop.");
        config.setTone(50);
        config.setVerbosity(50);
        config.setProactiveness(50);
        config.setActiveRules(List.of());
        return config;
    }
}
