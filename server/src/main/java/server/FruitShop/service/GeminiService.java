package server.FruitShop.service;

import com.google.genai.Client;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.response.ChatBot.GeminiAgentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service chuyên gọi Google Gemini API.
 * Được inject vào ChatServiceImpl để:
 *  1. detectIntent()      — Nhận diện ý định của user
 *  2. generateBotReply()  — Sinh câu trả lời tự nhiên
 *  3. extractMetadata()   — Trích xuất thông tin từ câu hỏi (tên sp, orderId...)
 *  4. agentChat()         — Function Calling: AI tự gọi tool query DB
 */
@Service
public class GeminiService {

    private final ChatToolService chatToolService;
    private final Client client;
    private final String model;
    private final int maxOutputTokens;
    private final float temperature;

    @Autowired
    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.max-output-tokens:1024}") int maxOutputTokens,
            @Value("${gemini.temperature:0.7}") float temperature,
            ChatToolService chatToolService) {

        this.client = Client.builder().apiKey(apiKey).build();
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
        this.chatToolService = chatToolService;
    }

    // ================================================================
    // CORE — Gửi prompt đơn giản, nhận text response
    // ================================================================

    /**
     * Gọi Gemini với 1 prompt và trả về text response.
     *
     * @param prompt Nội dung prompt đầy đủ
     * @return Text response từ Gemini, hoặc fallback nếu lỗi
     */
    public String generate(String prompt) {
        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .maxOutputTokens(maxOutputTokens)
                    .temperature(temperature)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    model,
                    prompt,
                    config
            );

            return response.text();
        } catch (Exception e) {
            System.err.println("[GeminiService] Error calling Gemini API: " + e.getMessage());
            return null;
        }
    }

    // ================================================================
    // INTENT DETECTION
    // ================================================================

    /**
     * Gọi Gemini để nhận diện intent từ tin nhắn của user.
     * Trả về 1 trong các nhãn: PRODUCT_ADVICE | PRODUCT_COMPARE |
     *   ORDER_LOOKUP | PRODUCT_SUGGEST | ORDER_PLACE | PAYMENT | GENERAL
     *
     * @param userMessage Tin nhắn của user
     * @return Intent string (uppercase, không có khoảng trắng)
     */
    public String detectIntent(String userMessage) {
        String prompt = """
                Bạn là AI phân tích ý định chat trong cửa hàng trái cây.
                Phân loại tin nhắn sau vào ĐÚNG 1 nhãn (chỉ trả về nhãn, không giải thích):

                - PRODUCT_ADVICE   : hỏi thông tin, tư vấn về 1 sản phẩm cụ thể
                - PRODUCT_COMPARE  : so sánh 2 hoặc nhiều sản phẩm
                - ORDER_LOOKUP     : tra cứu tình trạng đơn hàng
                - PRODUCT_SUGGEST  : nhờ gợi ý sản phẩm phù hợp
                - ORDER_PLACE      : muốn đặt hàng / mua sản phẩm
                - PAYMENT          : hỏi về thanh toán, phương thức thanh toán
                - GENERAL          : câu hỏi khác / chào hỏi / không liên quan

                Tin nhắn: "%s"

                Trả về đúng 1 nhãn (VD: PRODUCT_ADVICE):
                """.formatted(userMessage);

        String result = generate(prompt);
        if (result == null) return "GENERAL";

        // Normalize: trim, uppercase, lấy từ đầu tiên phòng model trả dư chữ
        String cleaned = result.trim().toUpperCase().split("\\s+")[0];
        List<String> validIntents = List.of(
                "PRODUCT_ADVICE", "PRODUCT_COMPARE", "ORDER_LOOKUP",
                "PRODUCT_SUGGEST", "ORDER_PLACE", "PAYMENT", "GENERAL"
        );
        return validIntents.contains(cleaned) ? cleaned : "GENERAL";
    }

    // ================================================================
    // BOT REPLY GENERATION
    // ================================================================

    /**
     * Sinh câu trả lời của bot dựa trên intent + context dữ liệu thực từ DB.
     *
     * @param intent      Intent đã detect được
     * @param userMessage Tin nhắn gốc của user
     * @param dataContext JSON string chứa dữ liệu thực (sản phẩm, đơn hàng...)
     *                    null nếu chưa có dữ liệu
     * @return Câu trả lời tự nhiên bằng tiếng Việt
     */
    public String generateReply(String intent, String userMessage, String dataContext) {
        String contextSection = (dataContext != null && !dataContext.isBlank())
                ? "Dữ liệu thực từ hệ thống:\n" + dataContext
                : "Chưa có dữ liệu cụ thể từ hệ thống.";

        String prompt = """
                Bạn là nhân viên tư vấn thân thiện của cửa hàng trái cây FruitShop.
                Trả lời ngắn gọn, tự nhiên bằng tiếng Việt (tối đa 3-4 câu).
                Không bịa dữ liệu — chỉ dùng dữ liệu được cung cấp.

                Intent: %s
                Tin nhắn của khách: "%s"
                %s

                Câu trả lời:
                """.formatted(intent, userMessage, contextSection);

        String result = generate(prompt);
        return (result != null) ? result.trim() : getFallbackReply(intent);
    }

    // ================================================================
    // KEYWORD EXTRACTION (phục vụ truy vấn DB)
    // ================================================================

    /**
     * Trích xuất tên sản phẩm từ câu hỏi của user.
     * Dùng khi intent = PRODUCT_ADVICE | PRODUCT_COMPARE | ORDER_PLACE
     *
     * VD: "Táo đỏ New Zealand giá bao nhiêu?" → "táo đỏ new zealand"
     */
    public String extractProductKeyword(String userMessage) {
        String prompt = """
                Trích xuất tên sản phẩm trái cây từ câu sau.
                Chỉ trả về TÊN SẢN PHẨM, viết thường, không có dấu câu, không giải thích.
                Nếu không tìm thấy, trả về: UNKNOWN

                Câu: "%s"
                """.formatted(userMessage);

        String result = generate(prompt);
        return (result != null) ? result.trim().toLowerCase() : "UNKNOWN";
    }

    /**
     * Trích xuất mã đơn hàng từ câu hỏi của user.
     * Dùng khi intent = ORDER_LOOKUP
     *
     * VD: "Đơn hàng ORD-12345 của tôi đến đâu rồi?" → "ORD-12345"
     */
    public String extractOrderId(String userMessage) {
        String prompt = """
                Trích xuất mã đơn hàng từ câu sau.
                Chỉ trả về MÃ ĐƠN HÀNG (VD: ORD-12345), không giải thích.
                Nếu không tìm thấy, trả về: UNKNOWN

                Câu: "%s"
                """.formatted(userMessage);

        String result = generate(prompt);
        return (result != null) ? result.trim() : "UNKNOWN";
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    /** Câu trả lời dự phòng khi Gemini API lỗi */
    private String getFallbackReply(String intent) {
        if (intent == null) return "Xin chào! Tôi có thể giúp gì cho bạn?";
        return switch (intent) {
            case "PRODUCT_ADVICE"  -> "Bạn đang muốn tìm hiểu về sản phẩm nào? Tôi sẽ tư vấn ngay!";
            case "PRODUCT_COMPARE" -> "Bạn muốn so sánh những sản phẩm nào? Cho tôi biết tên nhé!";
            case "ORDER_LOOKUP"    -> "Bạn muốn tra cứu đơn hàng nào? Hãy cung cấp mã đơn hàng!";
            case "PRODUCT_SUGGEST" -> "Cho tôi biết ngân sách và sở thích để gợi ý sản phẩm phù hợp!";
            case "ORDER_PLACE"     -> "Bạn muốn mua sản phẩm gì? Tôi sẽ hỗ trợ đặt hàng ngay!";
            case "PAYMENT"         -> "Shop hỗ trợ thanh toán MoMo và COD. Bạn muốn chọn phương thức nào?";
            default                -> "Tôi có thể tư vấn sản phẩm, tra cứu đơn hàng hoặc hỗ trợ đặt hàng. Bạn cần gì?";
        };
    }

    // ================================================================
    // AGENT CHAT — Function Calling (Tool Use)
    // ================================================================

    /**
     * Agentic chat với Gemini Function Calling.
     * Gemini tự quyết định gọi tool nào để lấy dữ liệu thực từ DB, sau đó sinh câu trả lời.
     *
     * @param userMessage Tin nhắn của user
     * @param accountId   ID tài khoản (dùng cho ORDER_LOOKUP), null nếu khách vãng lai
     * @return GeminiAgentResult chứa reply text + metadata JSON + intent
     */
    public GeminiAgentResult agentChat(String userMessage, String accountId) {
        try {
            // 1. Xây dựng tool definitions
            Tool tools = buildTools();

            // 2. Chuẩn bị system prompt + config
            String systemPrompt = """
                    Bạn là trợ lý AI của cửa hàng trái cây FruitShop.
                    Hãy trả lời thân thiện, ngắn gọn bằng tiếng Việt (tối đa 3-4 câu).
                    Nếu cần thông tin sản phẩm hoặc đơn hàng, hãy dùng các tools được cung cấp.
                    accountId của user hiện tại: %s
                    """.formatted(accountId != null ? accountId : "không có (khách vãng lai)");

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.builder()
                            .role("system")
                            .parts(List.of(Part.fromText(systemPrompt)))
                            .build())
                    .maxOutputTokens(maxOutputTokens)
                    .temperature(temperature)
                    .tools(List.of(tools))
                    .build();

            // 3. Bắt đầu họiệp (multi-turn)
            List<Content> history = new ArrayList<>();
            history.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(userMessage)))
                    .build());

            String capturedMetadata = null;
            String capturedIntent = "GENERAL";
            int maxRounds = 3; // giới hạn vòng lặp tool call

            for (int round = 0; round < maxRounds; round++) {
                GenerateContentResponse response = client.models.generateContent(model, history, config);

                List<Candidate> candidates = response.candidates().orElse(List.of());
                if (candidates.isEmpty()) break;

                Content modelContent = candidates.get(0).content().orElse(null);
                if (modelContent == null) break;
                List<Part> parts = modelContent.parts().orElse(List.of());

                // Kiểm tra có FunctionCall không
                boolean hasFunctionCall = parts.stream().anyMatch(p -> p.functionCall().isPresent());

                if (!hasFunctionCall) {
                    // Không có tool call → đây là câu trả lời cuối
                    String reply = response.text();
                    return GeminiAgentResult.of(
                            reply != null ? reply.trim() : getFallbackReply(capturedIntent),
                            capturedMetadata,
                            capturedIntent);
                }

                // Có FunctionCall: thực thi tất cả tools, thu thập kết quả
                history.add(modelContent); // thêm model response vào lịch sử

                List<Part> toolResponseParts = new ArrayList<>();
                for (Part part : parts) {
                    if (part.functionCall().isEmpty()) continue;
                    FunctionCall fc = part.functionCall().get();

                    String toolName = fc.name().orElse("");
                    Map<String, Object> args = fc.args().orElse(Map.of());

                    // Detect intent từ tên tool
                    capturedIntent = toolNameToIntent(toolName);

                    // Thực thi tool và lấy kết quả JSON
                    String toolResult = dispatchTool(toolName, args, accountId);
                    capturedMetadata = buildMetadataJson(toolName, toolResult);

                    toolResponseParts.add(Part.builder()
                            .functionResponse(FunctionResponse.builder()
                                    .name(toolName)
                                    .response(Map.of("result", toolResult))
                                    .build())
                            .build());
                }

                // Gửi kết quả tool trở lại cho Gemini
                history.add(Content.builder()
                        .role("tool")
                        .parts(toolResponseParts)
                        .build());
            }

            // Fallback nếu vượt số vòng
            return GeminiAgentResult.of(getFallbackReply(capturedIntent), capturedMetadata, capturedIntent);

        } catch (Exception e) {
            System.err.println("[GeminiService] agentChat error: " + e.getMessage());
            // Fallback sang chế độ đơn giản
            String intent = detectIntent(userMessage);
            String reply = generateReply(intent, userMessage, null);
            return GeminiAgentResult.of(reply, null, intent);
        }
    }

    // ================================================================
    // PRIVATE HELPERS — Tool definitions & dispatch
    // ================================================================

    /** Xây dựng danh sách tool definitions gửi cho Gemini */
    private Tool buildTools() {
        FunctionDeclaration searchProducts = FunctionDeclaration.builder()
                .name("searchProducts")
                .description("Tìm kiếm sản phẩm trái cây theo tên. Dùng khi user hỏi về sản phẩm cụ thể hoặc muốn mua hàng.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "keyword", Schema.builder()
                                        .type("STRING")
                                        .description("Từ khóa tên sản phẩm cần tìm (VD: táo, xoài cát)")
                                        .build(),
                                "limit", Schema.builder()
                                        .type("INTEGER")
                                        .description("Số kết quả tối đa, mặc định 5")
                                        .build()
                        ))
                        .required(List.of("keyword"))
                        .build())
                .build();

        FunctionDeclaration getProductDetail = FunctionDeclaration.builder()
                .name("getProductDetail")
                .description("Lấy thông tin chi tiết của 1 sản phẩm. Dùng khi cần so sánh hoặc tư vấn sâu.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "productId", Schema.builder()
                                        .type("STRING")
                                        .description("ID của sản phẩm")
                                        .build()
                        ))
                        .required(List.of("productId"))
                        .build())
                .build();

        FunctionDeclaration suggestProducts = FunctionDeclaration.builder()
                .name("suggestProducts")
                .description("Gợi ý sản phẩm phù hợp với ngân sách. Dùng khi user nhờ gợi ý hoặc hỏi nên mua gì.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "maxPrice", Schema.builder()
                                        .type("INTEGER")
                                        .description("Ngân sách tối đa (VND). Truyền -1 nếu không giới hạn.")
                                        .build(),
                                "limit", Schema.builder()
                                        .type("INTEGER")
                                        .description("Số sản phẩm gợi ý tối đa, mặc định 5")
                                        .build()
                        ))
                        .required(List.of())
                        .build())
                .build();

        FunctionDeclaration getOrdersByAccount = FunctionDeclaration.builder()
                .name("getOrdersByAccount")
                .description("Lấy danh sách đơn hàng của user. Dùng khi user hỏi về đơn hàng của mình.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "accountId", Schema.builder()
                                        .type("STRING")
                                        .description("ID tài khoản của user")
                                        .build()
                        ))
                        .required(List.of("accountId"))
                        .build())
                .build();

        FunctionDeclaration getOrderDetail = FunctionDeclaration.builder()
                .name("getOrderDetail")
                .description("Lấy chi tiết 1 đơn hàng cụ thể. Dùng khi user cung cấp mã đơn hàng.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "orderId", Schema.builder()
                                        .type("STRING")
                                        .description("Mã đơn hàng cần tra cứu")
                                        .build()
                        ))
                        .required(List.of("orderId"))
                        .build())
                .build();

        return Tool.builder()
                .functionDeclarations(List.of(
                        searchProducts,
                        getProductDetail,
                        suggestProducts,
                        getOrdersByAccount,
                        getOrderDetail
                ))
                .build();
    }

    /** Phân phối thực thi tool theo tên, trả về JSON kết quả */
    private String dispatchTool(String toolName, Map<String, Object> args, String contextAccountId) {
        return switch (toolName) {
            case "searchProducts" -> {
                String keyword = getString(args, "keyword", "");
                int limit = getInt(args, "limit", 5);
                yield chatToolService.searchProducts(keyword, limit);
            }
            case "getProductDetail" -> {
                String productId = getString(args, "productId", "");
                yield chatToolService.getProductDetail(productId);
            }
            case "suggestProducts" -> {
                long maxPrice = getLong(args, "maxPrice", -1L);
                int limit = getInt(args, "limit", 5);
                yield chatToolService.suggestProducts(maxPrice, limit);
            }
            case "getOrdersByAccount" -> {
                // Ưu tiên accountId từ args, fallback vào context
                String accountId = getString(args, "accountId", contextAccountId);
                yield chatToolService.getOrdersByAccount(accountId);
            }
            case "getOrderDetail" -> {
                String orderId = getString(args, "orderId", "");
                yield chatToolService.getOrderDetail(orderId);
            }
            default -> "{\"error\":\"Unknown tool: " + toolName + "\"}";
        };
    }

    /** Wrap kết quả tool thành metadata JSON kèm type để frontend biết hiển thị gì */
    private String buildMetadataJson(String toolName, String toolResult) {
        if (toolResult == null || toolResult.contains("\"error\"")) return null;
        String type = switch (toolName) {
            case "searchProducts"    -> "PRODUCT_LIST";
            case "getProductDetail"  -> "PRODUCT_DETAIL";
            case "suggestProducts"   -> "PRODUCT_SUGGEST";
            case "getOrdersByAccount"-> "ORDER_LIST";
            case "getOrderDetail"    -> "ORDER_DETAIL";
            default                  -> "DATA";
        };
        return "{\"type\":\"" + type + "\",\"data\":" + toolResult + "}";
    }

    private String toolNameToIntent(String toolName) {
        return switch (toolName) {
            case "searchProducts"    -> "PRODUCT_ADVICE";
            case "getProductDetail"  -> "PRODUCT_ADVICE";
            case "suggestProducts"   -> "PRODUCT_SUGGEST";
            case "getOrdersByAccount"-> "ORDER_LOOKUP";
            case "getOrderDetail"    -> "ORDER_LOOKUP";
            default                  -> "GENERAL";
        };
    }

    // ================================================================
    // PRIVATE HELPERS — Arg extraction
    // ================================================================

    private String getString(Map<String, Object> args, String key, String defaultVal) {
        Object v = args.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    private int getInt(Map<String, Object> args, String key, int defaultVal) {
        Object v = args.get(key);
        if (v == null) return defaultVal;
        try { return ((Number) v).intValue(); } catch (Exception e) { return defaultVal; }
    }

    private long getLong(Map<String, Object> args, String key, long defaultVal) {
        Object v = args.get(key);
        if (v == null) return defaultVal;
        try { return ((Number) v).longValue(); } catch (Exception e) { return defaultVal; }
    }
}
