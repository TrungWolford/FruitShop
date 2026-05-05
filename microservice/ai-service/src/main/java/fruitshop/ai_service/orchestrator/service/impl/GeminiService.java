package fruitshop.ai_service.orchestrator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import fruitshop.ai_service.orchestrator.service.LLMService;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Gemini implementation for LLM streaming.
 */
@Service("orchestratorGeminiService")
public class GeminiService implements LLMService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;
    private final float temperature;

    public GeminiService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash-lite}") String model,
            @Value("${gemini.max-output-tokens:1024}") int maxOutputTokens,
            @Value("${gemini.temperature:0.7}") float temperature) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.objectMapper = objectMapper;
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
        this.model = (model != null) ? model.trim() : "gemini-1.5-flash";
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, List<ConversationTurn> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return Flux.just("[DEBUG] Missing gemini.api-key in ai-service");
        }

        String body = buildRequestBody(systemPrompt, history, userMessage);

        String maskedKey = (apiKey.length() > 8) ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4) : "****";
        System.out.println("[AI-SERVICE] Calling Gemini API: model=" + model + ", key=" + maskedKey);

        return webClient.post()
                .uri("/v1beta/models/{model}:streamGenerateContent?key={key}", model, apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> {
                    System.err.println("Gemini API Error: " + e.getMessage());
                    return Flux.just("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"[Lỗi Gemini API: " + e.getMessage() + "]\"}]}}]}");
                })
                .handle((json, sink) -> {
                    String text = extractText(json);
                    if (text != null && !text.isBlank()) {
                        sink.next(text);
                    }
                });
    }

    private String buildRequestBody(String systemPrompt, List<ConversationTurn> history, String userMessage) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // 1. System Instruction
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                ObjectNode si = objectMapper.createObjectNode();
                si.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", systemPrompt)));
                root.set("system_instruction", si);
            }

            // 2. Contents (History + Current Message)
            ArrayNode contents = objectMapper.createArrayNode();
            
            // Add history turns
            if (history != null) {
                for (ConversationTurn turn : history) {
                    ObjectNode content = objectMapper.createObjectNode();
                    content.put("role", "user".equals(turn.getRole()) ? "user" : "model");
                    content.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", turn.getContent())));
                    contents.add(content);
                }
            }

            // Add current message
            ObjectNode current = objectMapper.createObjectNode();
            current.put("role", "user");
            current.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", userMessage)));
            contents.add(current);

            root.set("contents", contents);

            // 3. Generation Config
            ObjectNode gen = objectMapper.createObjectNode();
            gen.put("maxOutputTokens", maxOutputTokens);
            gen.put("temperature", temperature);
            root.set("generationConfig", gen);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String extractText(String json) {
        if (json == null || json.isBlank()) return null;
        
        String cleaned = json.trim();
        // Skip array delimiters
        if (cleaned.equals("[") || cleaned.equals("]") || cleaned.equals(",")) {
            return null;
        }
        
        // Remove leading comma if present (common in JSON array streams)
        if (cleaned.startsWith(",")) {
            cleaned = cleaned.substring(1).trim();
        }

        try {
            StringBuilder sb = new StringBuilder();
            com.fasterxml.jackson.core.JsonFactory factory = objectMapper.getFactory();
            com.fasterxml.jackson.core.JsonParser parser = factory.createParser(cleaned);
            
            // Move to first token
            parser.nextToken();
            
            while (!parser.isClosed() && parser.getCurrentToken() != null) {
                JsonNode root = objectMapper.readTree(parser);
                if (root != null) {
                    JsonNode text = root.path("candidates").path(0)
                            .path("content").path("parts").path(0).path("text");
                    if (!text.isMissingNode()) {
                        sb.append(text.asText());
                    } else if (root.has("error")) {
                        // Handle explicit error objects from Gemini
                        sb.append("[Error: ").append(root.path("error").path("message").asText()).append("]");
                    }
                }
                parser.nextToken();
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            // Last resort: try to find "text" field via regex if JSON parsing fails
            if (cleaned.contains("\"text\"")) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");
                java.util.regex.Matcher m = p.matcher(cleaned);
                StringBuilder sb = new StringBuilder();
                while (m.find()) {
                    String match = m.group(1);
                    // Basic unescaping for common characters
                    match = match.replace("\\n", "\n")
                                 .replace("\\\"", "\"")
                                 .replace("\\\\", "\\");
                    sb.append(match);
                }
                if (sb.length() > 0) return sb.toString();
            }
            return null;
        }
    }
}
