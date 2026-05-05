package fruitshop.ai_service.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG chunk returned by the retrieval service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RAGChunk {
    private String content;
    private String source;
    private Double score;
}
