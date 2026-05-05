package fruitshop.ai_service.orchestrator.client;

import fruitshop.ai_service.orchestrator.model.AIConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client to fetch AI configuration.
 */
@FeignClient(name = "config-service", url = "${services.config.base-url}")
public interface AIConfigClient {

    @GetMapping("${services.config.ai-config-path}")
    AIConfig getActiveConfig();
}
