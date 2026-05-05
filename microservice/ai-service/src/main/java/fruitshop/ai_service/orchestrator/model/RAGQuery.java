package fruitshop.ai_service.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * RAG query payload.
 */
@Data
@AllArgsConstructor
public class RAGQuery {
    private String query;
    private int topK;
}
