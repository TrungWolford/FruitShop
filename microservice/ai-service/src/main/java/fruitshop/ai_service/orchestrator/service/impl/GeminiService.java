package fruitshop.ai_service.orchestrator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import fruitshop.ai_service.orchestrator.service.FruitInventoryTool;
import fruitshop.ai_service.orchestrator.service.LLMService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Gemini implementation for LLM streaming.
 *
 * Agent Flow (2-step):
 * 1. Non-streaming probe call (generateContent) to detect if Gemini wants to call a tool.
 * 2a. If tool call: execute on boundedElastic, then make a SECOND non-streaming call
 *     with tool result in context (no tool declarations to prevent re-calling).
 * 2b. If no tool call: stream directly.
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
    private final FruitInventoryTool inventoryTool;

    public GeminiService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.max-output-tokens:1024}") int maxOutputTokens,
            @Value("${gemini.temperature:0.7}") float temperature,
            FruitInventoryTool inventoryTool) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.objectMapper = objectMapper;
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
        this.model = (model != null) ? model.trim() : "gemini-2.0-flash";
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
        this.inventoryTool = inventoryTool;

        System.out.println("[AI-SERVICE] GeminiService initialized with model: " + this.model);
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, List<ConversationTurn> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return Flux.just("[DEBUG] Missing gemini.api-key in ai-service");
        }

        // Step 1: Non-streaming probe to detect if Gemini wants a tool call
        String probeBody = buildRequestBody(systemPrompt, history, userMessage, null, null, true);

        return webClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(probeBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("[AI-SERVICE] Probe call error: " + e.getMessage());
                    return Mono.just("");
                })
                .flatMapMany(probeResponse -> {
                    JsonNode toolCall = extractToolCallFromResponse(probeResponse);

                    if (toolCall != null) {
                        // Step 2a: Execute tool on blocking-safe thread
                        String functionName = toolCall.path("name").asText();
                        JsonNode args = toolCall.path("args");
                        System.out.println("[AI-SERVICE] Tool call detected: " + functionName + " args=" + args);

                        return Mono.fromCallable(() -> executeToolCall(functionName, args))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMapMany(toolResultText -> {
                                    System.out.println("[AI-SERVICE] Tool result: " + toolResultText);

                                    // Build body WITHOUT tool declarations to prevent re-calling
                                    String finalBody = buildRequestBody(
                                            systemPrompt, history, userMessage, toolCall, toolResultText, false);

                                    System.out.println("[AI-SERVICE] Making final call (non-streaming) with tool result...");

                                    // Use NON-streaming for final call to get complete text
                                    return webClient.post()
                                            .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                                            .header("Content-Type", "application/json")
                                            .bodyValue(finalBody)
                                            .retrieve()
                                            .bodyToMono(String.class)
                                            .onErrorResume(e -> {
                                                System.err.println("[AI-SERVICE] Final call error: " + e.getMessage());
                                                return Mono.just("");
                                            })
                                            .flatMapMany(finalResponse -> {
                                                String text = extractTextFromNonStreamResponse(finalResponse);
                                                System.out.println("[AI-SERVICE] Final response text: " +
                                                        (text != null ? text.substring(0, Math.min(text.length(), 100)) + "..." : "NULL"));
                                                if (text != null && !text.isBlank()) {
                                                    return Flux.just(text);
                                                }
                                                return Flux.just("Xin lỗi, tôi không thể trả lời ngay bây giờ. Vui lòng thử lại.");
                                            });
                                });
                    }

                    // Step 2b: No tool call – stream directly
                    String streamBody = buildRequestBody(systemPrompt, history, userMessage, null, null, true);
                    return streamFromGemini(streamBody);
                });
    }

    // -------------------------------------------------------------------------
    // Tool execution
    // -------------------------------------------------------------------------

    private String executeToolCall(String functionName, JsonNode args) {
        if ("check_inventory".equals(functionName)) {
            String fruitName = args.path("fruit_name").asText();
            return inventoryTool.checkInventory(fruitName);
        }
        return "Không tìm thấy tool: " + functionName;
    }

    // -------------------------------------------------------------------------
    // Streaming helper (used only for non-tool-call responses)
    // -------------------------------------------------------------------------

    private Flux<String> streamFromGemini(String body) {
        return webClient.post()
                .uri("/v1beta/models/{model}:streamGenerateContent?key={key}", model, apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> {
                    System.err.println("[AI-SERVICE] Stream error: " + e.getMessage());
                    return Flux.just(
                            "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"[Lỗi kết nối Gemini: "
                                    + e.getMessage() + "]\"}]}}]}");
                })
                .handle((json, sink) -> {
                    String text = extractText(json);
                    if (text != null && !text.isBlank()) {
                        sink.next(text);
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Request body builder
    // -------------------------------------------------------------------------

    /**
     * Builds the JSON body for Gemini API calls.
     *
     * @param originalToolCall  the functionCall node returned by Gemini (null if no tool call)
     * @param toolResultText    the plain-text result of executing the tool (null if no tool call)
     * @param includeTools      whether to include tool declarations (false for final call after tool execution)
     */
    private String buildRequestBody(String systemPrompt, List<ConversationTurn> history,
                                    String userMessage, JsonNode originalToolCall, String toolResultText,
                                    boolean includeTools) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // 1. System instruction
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                ObjectNode si = objectMapper.createObjectNode();
                si.set("parts", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().put("text", systemPrompt)));
                root.set("system_instruction", si);
            }

            // 2. Conversation contents
            ArrayNode contents = objectMapper.createArrayNode();

            // 2a. History
            if (history != null) {
                for (ConversationTurn turn : history) {
                    ObjectNode content = objectMapper.createObjectNode();
                    content.put("role", "user".equals(turn.getRole()) ? "user" : "model");
                    content.set("parts", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode().put("text", turn.getContent())));
                    contents.add(content);
                }
            }

            // 2b. Current user message
            ObjectNode userTurn = objectMapper.createObjectNode();
            userTurn.put("role", "user");
            userTurn.set("parts", objectMapper.createArrayNode()
                    .add(objectMapper.createObjectNode().put("text", userMessage)));
            contents.add(userTurn);

            // 2c. If a tool was called, append the correct multi-turn structure
            if (originalToolCall != null && toolResultText != null) {
                // Model's turn: it said "I want to call this function"
                ObjectNode modelTurn = objectMapper.createObjectNode();
                modelTurn.put("role", "model");
                modelTurn.set("parts", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().set("functionCall", originalToolCall)));
                contents.add(modelTurn);

                // Tool's response turn
                String funcName = originalToolCall.path("name").asText();
                ObjectNode funcResponseNode = objectMapper.createObjectNode();
                funcResponseNode.put("name", funcName);
                funcResponseNode.set("response",
                        objectMapper.createObjectNode().put("content", toolResultText));

                ObjectNode toolTurn = objectMapper.createObjectNode();
                toolTurn.put("role", "user");
                toolTurn.set("parts", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().set("functionResponse", funcResponseNode)));
                contents.add(toolTurn);
            }

            root.set("contents", contents);

            // 3. Generation config
            ObjectNode gen = objectMapper.createObjectNode();
            gen.put("maxOutputTokens", maxOutputTokens);
            gen.put("temperature", temperature);
            root.set("generationConfig", gen);

            // 4. Tool declarations (ONLY included when we want Gemini to consider calling tools)
            if (includeTools) {
                ObjectNode checkInventoryDecl = objectMapper.createObjectNode();
                checkInventoryDecl.put("name", "check_inventory");
                checkInventoryDecl.put("description",
                        "Kiểm tra số lượng tồn kho và giá của các loại trái cây trong cửa hàng FruitShop. "
                        + "Gọi hàm này khi khách hàng hỏi về số lượng còn lại, tình trạng còn hàng, "
                        + "hay giá của một loại trái cây.");

                ObjectNode params = objectMapper.createObjectNode();
                params.put("type", "OBJECT");
                ObjectNode props = objectMapper.createObjectNode();
                ObjectNode fruitNameParam = objectMapper.createObjectNode();
                fruitNameParam.put("type", "string");
                fruitNameParam.put("description", "Tên loại trái cây cần kiểm tra (ví dụ: táo, cam, nho, lê)");
                props.set("fruit_name", fruitNameParam);
                params.set("properties", props);
                params.set("required", objectMapper.createArrayNode().add("fruit_name"));
                checkInventoryDecl.set("parameters", params);

                ArrayNode funcDeclarations = objectMapper.createArrayNode().add(checkInventoryDecl);
                ArrayNode tools = objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode().set("function_declarations", funcDeclarations));
                root.set("tools", tools);
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            System.err.println("[AI-SERVICE] Error building request body: " + e.getMessage());
            return "{}";
        }
    }

    // -------------------------------------------------------------------------
    // Parsers
    // -------------------------------------------------------------------------

    /**
     * Extract text from a non-streaming generateContent response.
     * Response format: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}
     */
    private String extractTextFromNonStreamResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            if (parts.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode part : parts) {
                    if (part.has("text")) {
                        sb.append(part.get("text").asText());
                    }
                }
                return sb.length() > 0 ? sb.toString() : null;
            }
            return null;
        } catch (Exception e) {
            System.err.println("[AI-SERVICE] Error parsing non-stream response: " + e.getMessage());
            return null;
        }
    }

    private JsonNode extractToolCallFromResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode part = root.path("candidates").path(0).path("content").path("parts").path(0);
            if (part.has("functionCall")) {
                return part.get("functionCall");
            }
        } catch (Exception e) {
            System.err.println("[AI-SERVICE] Error parsing probe response: " + e.getMessage());
        }
        return null;
    }

    private String extractText(String json) {
        if (json == null || json.isBlank()) return null;

        String cleaned = json.trim();
        if (cleaned.equals("[") || cleaned.equals("]") || cleaned.equals(",")) return null;
        if (cleaned.startsWith(",")) cleaned = cleaned.substring(1).trim();

        try {
            StringBuilder sb = new StringBuilder();
            com.fasterxml.jackson.core.JsonFactory factory = objectMapper.getFactory();
            com.fasterxml.jackson.core.JsonParser parser = factory.createParser(cleaned);
            parser.nextToken();

            while (!parser.isClosed() && parser.getCurrentToken() != null) {
                JsonNode root = objectMapper.readTree(parser);
                if (root != null) {
                    JsonNode text = root.path("candidates").path(0)
                            .path("content").path("parts").path(0).path("text");
                    if (!text.isMissingNode()) {
                        sb.append(text.asText());
                    } else if (root.has("error")) {
                        sb.append("[Error: ")
                          .append(root.path("error").path("message").asText())
                          .append("]");
                    }
                }
                parser.nextToken();
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            // Regex fallback
            if (cleaned.contains("\"text\"")) {
                java.util.regex.Pattern p =
                        java.util.regex.Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");
                java.util.regex.Matcher m = p.matcher(cleaned);
                StringBuilder sb = new StringBuilder();
                while (m.find()) {
                    sb.append(m.group(1)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\"));
                }
                if (sb.length() > 0) return sb.toString();
            }
            return null;
        }
    }
}
