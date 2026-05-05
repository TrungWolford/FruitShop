package fruitshop.ai_service.orchestrator.client;

import fruitshop.ai_service.orchestrator.model.RAGChunk;
import fruitshop.ai_service.orchestrator.model.RAGQuery;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for RAG retrieval.
 */
@FeignClient(name = "rag-service", url = "${services.rag.base-url}")
public interface RAGClient {

    @PostMapping("${services.rag.search-path}")
    List<RAGChunk> search(@RequestBody RAGQuery query);
}
