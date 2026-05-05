package fruitshop.ai_service.orchestrator.model;

import java.util.List;
import lombok.Data;

/**
 * AI configuration loaded from config-service.
 */
@Data
public class AIConfig {
    private String systemPrompt;
    private Integer tone;
    private Integer verbosity;
    private Integer proactiveness;
    private List<Rule> activeRules;
}
