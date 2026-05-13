package fruitshop.ai_service.config;

import org.springframework.context.annotation.Configuration;

/**
 * AI Configuration.
 * VectorStore operations are handled via plain JDBC in VectorStoreService.
 * No Spring AI dependency required.
 */
@Configuration
public class AiConfig {
    // RAG is implemented manually via JDBC + Gemini Embedding API
    // See: VectorStoreService.java and DocumentService.java
}
