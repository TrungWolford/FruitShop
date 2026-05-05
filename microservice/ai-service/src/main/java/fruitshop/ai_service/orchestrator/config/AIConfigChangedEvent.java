package fruitshop.ai_service.orchestrator.config;

import org.springframework.context.ApplicationEvent;

/**
 * Event indicating AI config changed in config-service.
 */
public class AIConfigChangedEvent extends ApplicationEvent {

    public AIConfigChangedEvent(Object source) {
        super(source);
    }
}
