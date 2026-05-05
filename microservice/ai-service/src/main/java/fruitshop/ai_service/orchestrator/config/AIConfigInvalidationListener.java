package fruitshop.ai_service.orchestrator.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Clears AI config cache when a refresh event is received.
 */
@Component
public class AIConfigInvalidationListener {

    private final CacheManager cacheManager;

    public AIConfigInvalidationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener
    public void onAIConfigChanged(AIConfigChangedEvent event) {
        if (cacheManager.getCache("aiConfig") != null) {
            cacheManager.getCache("aiConfig").evict("ai_config_current");
        }
    }
}
