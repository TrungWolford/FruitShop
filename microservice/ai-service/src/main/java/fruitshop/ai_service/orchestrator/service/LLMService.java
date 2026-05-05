package fruitshop.ai_service.orchestrator.service;

import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import java.util.List;
import reactor.core.publisher.Flux;

/**
 * LLM abstraction to allow swapping providers.
 */
public interface LLMService {

    /**
     * Streams the model output as token chunks.
     * @param systemPrompt Instructions for the model's behavior.
     * @param history Previous turns in the conversation.
     * @param userMessage The current user input.
     */
    Flux<String> streamChat(String systemPrompt, List<ConversationTurn> history, String userMessage);
}
