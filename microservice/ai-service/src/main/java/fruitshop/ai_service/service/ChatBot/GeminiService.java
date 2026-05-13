package fruitshop.ai_service.service.ChatBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import fruitshop.ai_service.dto.response.ChatBot.GeminiAgentResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiService {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final ChatToolService chatToolService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient http;

    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;
    private final float temperature;

    @Autowired
    public GeminiService(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.max-output-tokens:1024}") int maxOutputTokens,
            @Value("${gemini.temperature:0.7}") float temperature,
            ChatToolService chatToolService) {

        this.apiKey = apiKey;
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
        this.chatToolService = chatToolService;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public String generate(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[DEBUG] Missing gemini.api-key in ai-service";
        }
        try {
            var body = objectMapper.createObjectNode();
            var userPart = objectMapper.createObjectNode().put("text", prompt);
            var content = objectMapper.createObjectNode();
            content.put("role", "user");
            content.set("parts", objectMapper.createArrayNode().add(userPart));
            body.set("contents", objectMapper.createArrayNode().add(content));

            var genConfig = objectMapper.createObjectNode();
            genConfig.put("maxOutputTokens", maxOutputTokens);
            genConfig.put("temperature", temperature);
            body.set("generationConfig", genConfig);

            return callGemini(body);
        } catch (Exception e) {
            return "[DEBUG] generate exception: " + e.getMessage();
        }
    }

    public String detectIntent(String userMessage) {
        String prompt = """
                Bạn là AI phân tích ý định chat trong cửa hàng trái cây.
                Phân loại tin nhắn sau vào đúng 1 nhãn (chỉ trả về nhãn):
                - PRODUCT_ADVICE
                - PRODUCT_COMPARE
                - ORDER_LOOKUP
                - PRODUCT_SUGGEST
                - ORDER_PLACE
                - PAYMENT
                - HUMAN_SUPPORT
                - REFUND
                - GENERAL
                Tin nhắn: "%s"
                """.formatted(userMessage);

        String result = generate(prompt);
        if (result == null) return "GENERAL";

        String cleaned = result.trim().toUpperCase().split("\\s+")[0];
        List<String> validIntents = List.of(
                "PRODUCT_ADVICE", "PRODUCT_COMPARE", "ORDER_LOOKUP",
                "PRODUCT_SUGGEST", "ORDER_PLACE", "PAYMENT",
                "HUMAN_SUPPORT", "REFUND", "GENERAL");
        return validIntents.contains(cleaned) ? cleaned : "GENERAL";
    }

    public String generateReply(String intent, String userMessage, String dataContext) {
        String contextSection = (dataContext != null && !dataContext.isBlank())
                ? "Du lieu he thong:\n" + dataContext
                : "Chua co du lieu cu the.";

        String prompt = """
                Ban la nhan vien tu van than thien cua cua hang FruitShop.
                Tra loi ngan gon, tu nhien bang tieng Viet (toi da 3-4 cau).
                Khong bia du lieu.
                Intent: %s
                Tin nhan khach: "%s"
                %s
                Cau tra loi:
                """.formatted(intent, userMessage, contextSection);

        String result = generate(prompt);
        return result != null ? result.trim() : getFallbackReply(intent);
    }

    public GeminiAgentResult agentChat(String userMessage, String accountId) {
        String intent = detectIntent(userMessage);

        // Reserved place for tool-based routing; currently graceful fallback
        String toolData = switch (intent) {
            case "PRODUCT_ADVICE", "PRODUCT_SUGGEST" -> chatToolService.searchProducts(userMessage, 5);
            case "ORDER_LOOKUP" -> chatToolService.getOrdersByAccount(accountId);
            default -> null;
        };

        String reply = generateReply(intent, userMessage, toolData);
        String metadata = (toolData != null && !toolData.contains("\"error\""))
                ? "{\"type\":\"DATA\",\"data\":" + toolData + "}"
                : null;

        return GeminiAgentResult.of(reply, metadata, intent);
    }

    private String callGemini(com.fasterxml.jackson.databind.node.ObjectNode body) {
        try {
            String url = BASE_URL.formatted(model, apiKey);
            String responseJson = callGeminiRaw(url, body.toString());
            if (responseJson == null) return null;
            if (responseJson.contains("__error__")) return responseJson;

            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText(null);
        } catch (Exception e) {
            return "[DEBUG] callGemini exception: " + e.getMessage();
        }
    }

    private String callGeminiRaw(String url, String jsonBody) {
        try {
            RequestBody requestBody = RequestBody.create(
                    jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(requestBody).build();

            try (Response response = http.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    return "{\"__error__\":\"HTTP_" + response.code() + "\",\"detail\":" + objectMapper.valueToTree(errBody) + "}";
                }
                return response.body() != null ? response.body().string() : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getFallbackReply(String intent) {
        if (intent == null) return "Xin chao! Toi co the giup gi cho ban?";
        return switch (intent) {
            case "PRODUCT_ADVICE" -> "Ban muon tim hieu san pham nao? Toi se tu van ngay!";
            case "PRODUCT_COMPARE" -> "Ban muon so sanh nhung san pham nao?";
            case "ORDER_LOOKUP" -> "Ban hay cho toi ma don hang de tra cuu nhe.";
            case "PRODUCT_SUGGEST" -> "Ban cho toi ngan sach de goi y san pham phu hop.";
            case "ORDER_PLACE" -> "Ban muon mua san pham gi va so luong bao nhieu?";
            case "PAYMENT" -> "Shop ho tro COD va chuyen khoan. Ban muon chon cach nao?";
            case "HUMAN_SUPPORT" -> "Toi co the ket noi ban voi nhan vien ho tro truc tiep.";
            case "REFUND" -> "Ban hay cung cap ma don hang can hoan tra nhe.";
            default -> "Toi co the tu van san pham, tra cuu don hang va ho tro dat hang.";
        };
    }
}
