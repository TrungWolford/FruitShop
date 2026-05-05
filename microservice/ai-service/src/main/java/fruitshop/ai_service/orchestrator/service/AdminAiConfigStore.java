package fruitshop.ai_service.orchestrator.service;

import fruitshop.ai_service.orchestrator.model.AIConfig;
import fruitshop.ai_service.orchestrator.model.AIConfigEntity;
import fruitshop.ai_service.repository.AIConfigRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * Store for admin-configured AI settings.
 * Persists to database via AIConfigRepository.
 */
@Component
public class AdminAiConfigStore {

    private static final String CONFIG_ID = "active_config";
    private final AIConfigRepository repository;
    private final AtomicReference<AIConfig> current = new AtomicReference<>(buildDefault());

    public AdminAiConfigStore(AIConfigRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        try {
            Optional<AIConfigEntity> entity = repository.findById(CONFIG_ID);
            if (entity.isPresent()) {
                current.set(toDto(entity.get()));
            }
        } catch (Exception e) {
            // Database or table might not exist yet, fallback to default and log
            System.err.println("Warning: AdminAiConfigStore could not load from DB: " + e.getMessage());
        }
    }

    public AIConfig get() {
        return current.get();
    }

    public void set(AIConfig config) {
        current.set(config);
        try {
            // Persist to DB
            AIConfigEntity entity = toEntity(config);
            repository.save(entity);
        } catch (Exception e) {
            System.err.println("Error saving AI config to DB: " + e.getMessage());
            // We still have it in 'current' memory, so the AI will work until restart
        }
    }

    public boolean hasCustomConfig() {
        AIConfig cfg = current.get();
        return cfg != null
                && cfg.getSystemPrompt() != null
                && !cfg.getSystemPrompt().isBlank()
                && !"__default__".equals(cfg.getSystemPrompt());
    }

    private static AIConfig buildDefault() {
        AIConfig cfg = new AIConfig();
        cfg.setSystemPrompt("__default__");
        cfg.setTone(50);
        cfg.setVerbosity(50);
        cfg.setProactiveness(50);
        cfg.setActiveRules(List.of());
        return cfg;
    }

    private AIConfig toDto(AIConfigEntity entity) {
        AIConfig dto = new AIConfig();
        dto.setSystemPrompt(entity.getSystemPrompt());
        dto.setTone(entity.getTone());
        dto.setVerbosity(entity.getVerbosity());
        dto.setProactiveness(entity.getProactiveness());
        dto.setActiveRules(List.of());
        return dto;
    }

    private AIConfigEntity toEntity(AIConfig dto) {
        return AIConfigEntity.builder()
                .id(CONFIG_ID)
                .systemPrompt(dto.getSystemPrompt())
                .tone(dto.getTone())
                .verbosity(dto.getVerbosity())
                .proactiveness(dto.getProactiveness())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
