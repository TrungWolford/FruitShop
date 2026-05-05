package fruitshop.ai_service.orchestrator.model;

import lombok.Data;

/**
 * Rule definition used in prompt building.
 */
@Data
public class Rule {
    private String text;
    private Integer priority;
    private String scope;
    private boolean active;
}
