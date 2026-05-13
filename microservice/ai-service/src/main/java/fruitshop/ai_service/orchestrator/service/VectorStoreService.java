package fruitshop.ai_service.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Manual RAG Vector Store using plain JDBC + pgvector + Gemini Embedding API.
 * Fully compatible with Spring Boot 4.x — no Spring AI dependency needed.
 */
@Service
public class VectorStoreService {
    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final JdbcTemplate jdbcTemplate;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent";

    public VectorStoreService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initSchema() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_documents (
                    id BIGSERIAL PRIMARY KEY,
                    content TEXT NOT NULL,
                    source VARCHAR(500),
                    embedding vector(768),
                    created_at TIMESTAMP DEFAULT NOW()
                )
            """);
            jdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS rag_documents_embedding_idx " +
                "ON rag_documents USING hnsw (embedding vector_cosine_ops)"
            );
            // Metadata table to track uploaded sources
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS rag_sources (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(500) NOT NULL UNIQUE,
                    status VARCHAR(50) DEFAULT 'indexed',
                    document_count INTEGER DEFAULT 0,
                    updated_at TIMESTAMP DEFAULT NOW()
                )
            """);
            log.info("RAG schema initialized successfully.");
        } catch (Exception e) {
            log.warn("RAG schema init warning (may already exist): {}", e.getMessage());
        }
    }

    /**
     * Store a text chunk with its embedding into the vector database.
     */
    public void storeChunk(String content, String source) {
        try {
            float[] embedding = generateEmbedding(content);
            String vectorStr = toPostgresVector(embedding);
            jdbcTemplate.update(
                "INSERT INTO rag_documents (content, source, embedding) VALUES (?, ?, ?::vector)",
                content, source, vectorStr
            );
        } catch (Exception e) {
            log.error("Failed to store chunk from {}: {}", source, e.getMessage());
            throw new RuntimeException("Failed to store chunk", e);
        }
    }

    /**
     * Upsert source metadata after ingestion.
     */
    public void upsertSourceMetadata(String name, int chunkCount) {
        jdbcTemplate.update("""
            INSERT INTO rag_sources (name, status, document_count, updated_at)
            VALUES (?, 'indexed', ?, NOW())
            ON CONFLICT (name) DO UPDATE
            SET status = 'indexed', document_count = EXCLUDED.document_count, updated_at = NOW()
        """, name, chunkCount);
    }

    /**
     * List all indexed sources.
     */
    public List<java.util.Map<String, Object>> listSources() {
        return jdbcTemplate.queryForList(
            "SELECT id, name, status, document_count, updated_at FROM rag_sources ORDER BY updated_at DESC"
        );
    }

    /**
     * Search for the top-k most similar chunks to the query.
     */
    public List<String> similaritySearch(String query, int topK) {
        try {
            float[] embedding = generateEmbedding(query);
            String vectorStr = toPostgresVector(embedding);
            return jdbcTemplate.queryForList(
                "SELECT content FROM rag_documents ORDER BY embedding <=> ?::vector LIMIT ?",
                String.class, vectorStr, topK
            );
        } catch (Exception e) {
            log.error("Similarity search failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Delete all documents from a specific source (PDF file).
     */
    public void deleteBySource(String source) {
        jdbcTemplate.update("DELETE FROM rag_documents WHERE source = ?", source);
    }

    /**
     * Delete source metadata entry.
     */
    public void deleteSourceMetadata(String name) {
        jdbcTemplate.update("DELETE FROM rag_sources WHERE name = ?", name);
    }

    /**
     * Call Gemini text-embedding-004 API to get a 768-dim float vector.
     */
    @SuppressWarnings("unchecked")
    private float[] generateEmbedding(String text) throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "model", "models/text-embedding-004",
            "content", Map.of("parts", List.of(Map.of("text", text)))
        ));

        Request request = new Request.Builder()
            .url(EMBEDDING_URL + "?key=" + apiKey)
            .post(RequestBody.create(json, MediaType.get("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Embedding API error: " + response.code());
            }
            Map<String, Object> body = objectMapper.readValue(response.body().string(), Map.class);
            Map<String, Object> embeddingObj = (Map<String, Object>) body.get("embedding");
            List<Number> values = (List<Number>) embeddingObj.get("values");
            float[] result = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                result[i] = values.get(i).floatValue();
            }
            return result;
        }
    }

    private String toPostgresVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
