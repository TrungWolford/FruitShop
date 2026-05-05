package server.FruitShop.service.ChatBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.response.ChatBot.GeminiAgentResult;
import server.FruitShop.entity.ChatMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Gọi Gemini REST API bằng OkHttp (không dùng SDK).
 * Hỗ trợ:
 *  1. generate()      — Gọi Gemini với prompt đơn giản
 *  2. detectIntent()  — Nhận diện intent từ tin nhắn user
 *  3. generateReply() — Sinh câu trả lời tự nhiên
 *  4. agentChat()     — Function Calling: Gemini tự gọi tool query DB
 */
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
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-1.5-flash}") String model,
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

    // ================================================================
    // CORE — Gọi Gemini với prompt đơn giản
    // ================================================================

    /**
     * Gọi Gemini với 1 prompt thuần, trả về text.
     */
    public String generate(String prompt) {
        try {
            ObjectNode body = objectMapper.createObjectNode();

            // contents
            ObjectNode userPart = objectMapper.createObjectNode();
            userPart.put("text", prompt);
            ObjectNode content = objectMapper.createObjectNode();
            content.put("role", "user");
            content.set("parts", objectMapper.createArrayNode().add(userPart));
            body.set("contents", objectMapper.createArrayNode().add(content));

            // generationConfig
            ObjectNode genConfig = objectMapper.createObjectNode();
            genConfig.put("maxOutputTokens", maxOutputTokens);
            genConfig.put("temperature", temperature);
            body.set("generationConfig", genConfig);

            return callGemini(body);
        } catch (Exception e) {
            System.err.println("[GeminiService] generate error: " + e.getMessage());
            return null;
        }
    }

    // ================================================================
    // INTENT DETECTION
    // ================================================================

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
                - HUMAN_SUPPORT    : yêu cầu liên hệ/ nhắn tin trao đổi trực tiếp với nhân viên bán hàng
                - REFUND           : yêu cầu hoàn trả / trả hàng / hoàn tiền
                - GENERAL          : câu hỏi khác / chào hỏi / không liên quan
                Tin nhắn: "%s"
                Trả về đúng 1 nhãn:""".formatted(userMessage);

        String result = generate(prompt);
        if (result == null) return "GENERAL";

        String cleaned = result.trim().toUpperCase().split("\\s+")[0];
        List<String> validIntents = List.of(
                "PRODUCT_ADVICE", "PRODUCT_COMPARE", "ORDER_LOOKUP",
                "PRODUCT_SUGGEST", "ORDER_PLACE", "PAYMENT",
                "HUMAN_SUPPORT", "REFUND", "GENERAL");
        return validIntents.contains(cleaned) ? cleaned : "GENERAL";
    }

    // ================================================================
    // BOT REPLY
    // ================================================================

    public String generateReply(String intent, String userMessage, String dataContext) {
        String contextSection = (dataContext != null && !dataContext.isBlank())
                ? "Dữ liệu từ hệ thống:\n" + dataContext
                : "Chưa có dữ liệu cụ thể.";

        String prompt = """
                Bạn là nhân viên tư vấn thân thiện của cửa hàng trái cây FruitShop.
                Trả lời ngắn gọn, tự nhiên bằng tiếng Việt (tối đa 3-4 câu).
                Không bịa dữ liệu — chỉ dùng dữ liệu được cung cấp.
                Intent: %s
                Tin nhắn của khách: "%s"
                %s
                Câu trả lời:""".formatted(intent, userMessage, contextSection);

        String result = generate(prompt);
        return result != null ? result.trim() : getFallbackReply(intent);
    }

    // ================================================================
    // AGENT CHAT — Function Calling
    // ================================================================

    /**
     * Gemini tự quyết định gọi tool nào để lấy dữ liệu DB, rồi sinh câu trả lời.
     *
     * @param conversationHistory Lịch sử tin nhắn trước đó của session (không bao gồm tin nhắn hiện tại).
     *                            Dùng để Gemini hiểu context multi-turn (VD: user đã chọn sản phẩm gì).
     * Hỗ trợ multi-turn conversation với context history.
     */
    public GeminiAgentResult agentChat(String userMessage, String accountId,
                                       List<ChatMessage> conversationHistory) {
        try {
            String url = BASE_URL.formatted(model, apiKey);

            // ── Build request body ──────────────────────────────────
            ObjectNode body = objectMapper.createObjectNode();

            // systemInstruction
            ObjectNode sysPart = objectMapper.createObjectNode();
            sysPart.put("text", """
                    Bạn là trợ lý AI của cửa hàng FruitShop.
                    Trả lời thân thiện, ngắn gọn bằng tiếng Việt (tối đa 3-4 câu).
                    Nếu cần thông tin sản phẩm hoặc đơn hàng, hãy dùng các tools được cung cấp.
                    accountId của user hiện tại: %s
                    
                    QUY TRÌNH ĐẶT HÀNG QUA CHAT (đúng thứ tự sau):
                    Bước 1. Tìm sản phẩm và thêm vào giỏ hàng:
                       - Gọi searchProducts để tìm sản phẩm → xác nhận tên và số lượng với user.
                       - Sau khi user xác nhận: gọi NGAY addToCart(accountId, productId, quantity).
                       - Hỏi "Bạn có muốn thêm sản phẩm nào khác không?"
                       - Nếu có → tiếp tục tìm và addToCart cho từng sản phẩm.
                       - Nếu không → tiến hành bước 2.
                    Bước 2. Gọi getCartItems(accountId) để hiển thị lại toàn bộ giỏ hàng (cả sản phẩm từ chat lẫn website).
                    Bước 3. Gọi getUserShippingAddresses để kiểm tra địa chỉ:
                       - Có địa chỉ → cho user chọn hoặc nhập mới.
                       - Chưa có → hỏi: Tên người nhận, SĐT, Địa chỉ, Tỉnh/TP.
                    Bước 4. **BắT BUỘC**: Khi user cung cấp thông tin địa chỉ, PHẢI gọi createShippingAddress ngay.
                       **QUAN TRỌNG**: Sau khi gọi createShippingAddress, PHẢI đợi nhận kết quả (shippingId) trước.
                       TUYỆT ĐỐI KHÔNG gọi createOrderFromChat trong cùng lượt với createShippingAddress.
                       createOrderFromChat chỉ được gọi ở lượt TIẾP THEO sau khi đã có shippingId.
                    Bước 5. Hỏi phương thức thanh toán (COD hoặc chuyển khoản).
                    Bước 6. Tóm tắt đơn hàng → hỏi xác nhận lần cuối.
                    Bước 7. User đồng ý → gọi createOrderFromChat với itemsJson=[] (tự động dùng giỏ hàng).
                    Bước 8. Nếu paymentMethod=1 → gọi createMomoPayment(orderId) → gửi link/QR cho user.

                    QUY TRÌNH HOÀN TRẢ ĐƠN HÀNG:
                    Bước 1. Gọi getOrdersByAccount để hiển thị đơn hàng gần đây cho user chọn.
                    Bước 2. Gọi getRefundsByOrder(orderId) để kiểm tra đơn đã có yêu cầu hoàn trả chưa.
                       - Đã có và đang "Chờ xác nhận" → thông báo đã gửi yêu cầu, không tạo thêm.
                    Bước 3. Hỏi lý do hoàn trả (hàng lỗi, không đúng mô tả, v.v.).
                    Bước 4. Xác nhận thông tin → gọi createRefundFromChat(accountId, orderId, reason, refundAmount=0).
                    """.formatted(accountId != null ? accountId : "không có (khách vãng lai)"));
            ObjectNode sysContent = objectMapper.createObjectNode();
            sysContent.set("parts", objectMapper.createArrayNode().add(sysPart));
            body.set("systemInstruction", sysContent);

            // NEW: Multi-turn contents (lịch sử + tin hiện tại)
            ArrayNode contents = objectMapper.createArrayNode();

            // Thêm lịch sử hội thoại
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (server.FruitShop.entity.ChatMessage msg : conversationHistory) {
                    ObjectNode msgPart = objectMapper.createObjectNode();
                    msgPart.put("text", msg.getContent());
                    
                    ObjectNode msgContent = objectMapper.createObjectNode();
                    String role = msg.getSenderRole().equalsIgnoreCase("SYSTEM") ? "model" : "user";
                    msgContent.put("role", role);
                    msgContent.set("parts", objectMapper.createArrayNode().add(msgPart));
                    
                    contents.add(msgContent);
                }
            }

            // Thêm tin nhắn hiện tại
            ObjectNode userPart = objectMapper.createObjectNode();
            userPart.put("text", userMessage);
            ObjectNode userContent = objectMapper.createObjectNode();
            userContent.put("role", "user");
            userContent.set("parts", objectMapper.createArrayNode().add(userPart));
            contents.add(userContent);
            body.set("contents", contents);

            // tools (function declarations)
            body.set("tools", buildFunctionDeclarations());

            // toolConfig — AUTO: Gemini tự quyết định gọi tool khi cần
            ObjectNode toolConfig = objectMapper.createObjectNode();
            ObjectNode fnCallingConfig = objectMapper.createObjectNode();
            fnCallingConfig.put("mode", "AUTO");
            toolConfig.set("function_calling_config", fnCallingConfig);
            body.set("tool_config", toolConfig);

            // generationConfig
            ObjectNode genConfig = objectMapper.createObjectNode();
            genConfig.put("maxOutputTokens", maxOutputTokens);
            genConfig.put("temperature", temperature);
            body.set("generationConfig", genConfig);

            // ── Vòng lặp agentic (tối đa 3 lượt tool call) ─────────
            String capturedIntent = "GENERAL";
            String capturedMetadata = null;

            for (int round = 0; round < 5; round++) {
                String responseJson = callGeminiRaw(url, body.toString());
                if (responseJson == null) break;

                // Nếu Gemini trả lỗi HTTP (429, 403...) → trả thẳng debug message
                if (responseJson.contains("\"__error__\"")) {
                    JsonNode errNode = objectMapper.readTree(responseJson);
                    String errCode = errNode.path("__error__").asText();
                    String detail = errNode.path("detail").asText();
                    return GeminiAgentResult.of("[DEBUG] Gemini API error: " + errCode + " — " + detail, null, "GENERAL");
                }

                JsonNode root = objectMapper.readTree(responseJson);
                JsonNode candidate = root.path("candidates").path(0);
                JsonNode modelContent = candidate.path("content");
                JsonNode parts = modelContent.path("parts");

                if (parts.isEmpty()) break;

                // Kiểm tra có functionCall không
                JsonNode firstPart = parts.path(0);
                if (!firstPart.has("functionCall")) {
                    // Đây là câu trả lời text cuối cùng
                    String reply = firstPart.path("text").asText("").trim();
                    if (reply.isBlank()) reply = getFallbackReply(capturedIntent);
                    return GeminiAgentResult.of(reply, capturedMetadata, capturedIntent);
                }

                // Có functionCall → thực thi tất cả tool trong parts
                // Thêm model response vào lịch sử
                contents.add(modelContent);

                ArrayNode toolResultParts = objectMapper.createArrayNode();
                for (JsonNode part : parts) {
                    if (!part.has("functionCall")) continue;

                    String toolName = part.path("functionCall").path("name").asText();
                    JsonNode args = part.path("functionCall").path("args");

                    // Detect intent từ tool được gọi
                    capturedIntent = toolNameToIntent(toolName);

                    // Thực thi tool → query DB
                    String toolResult = dispatchTool(toolName, args, accountId);
                    capturedMetadata = buildMetadataJson(toolName, toolResult);

                    // Build functionResponse part
                    ObjectNode fnResp = objectMapper.createObjectNode();
                    fnResp.put("name", toolName);
                    ObjectNode respContent = objectMapper.createObjectNode();
                    respContent.put("result", toolResult);
                    fnResp.set("response", respContent);

                    ObjectNode toolPart = objectMapper.createObjectNode();
                    toolPart.set("functionResponse", fnResp);
                    toolResultParts.add(toolPart);
                }

                // Thêm tool results vào lịch sử
                ObjectNode toolContent = objectMapper.createObjectNode();
                toolContent.put("role", "tool");
                toolContent.set("parts", toolResultParts);
                contents.add(toolContent);

                // Cập nhật body với lịch sử mới
                body.set("contents", contents);
            }

            return GeminiAgentResult.of(getFallbackReply(capturedIntent), capturedMetadata, capturedIntent);

        } catch (Exception e) {
            System.err.println("[GeminiService] agentChat error: " + e.getMessage());
            e.printStackTrace();
            return GeminiAgentResult.of("[DEBUG] agentChat exception: " + e.getMessage(), null, "GENERAL");
        }
    }

    // NEW: Overload method (2 params) để backward compatibility
    public GeminiAgentResult agentChat(String userMessage, String accountId) {
        return agentChat(userMessage, accountId, null);
    }

    // ================================================================
    // PRIVATE — HTTP helpers
    // ================================================================

    /** Gọi Gemini API (simple generate), trả về text từ response */
    private String callGemini(ObjectNode body) {
        try {
            String url = BASE_URL.formatted(model, apiKey);
            String responseJson = callGeminiRaw(url, body.toString());
            if (responseJson == null) return null;

            // Nếu là lỗi HTTP, trả thẳng message lỗi
            if (responseJson.contains("__error__")) return responseJson;

            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText(null);
        } catch (Exception e) {
            System.err.println("[GeminiService] Error calling Gemini API: " + e.getMessage());
            return "[DEBUG] callGemini exception: " + e.getMessage();
        }
    }

    /** Gửi HTTP POST tới Gemini REST endpoint, trả về raw JSON string */
    private String callGeminiRaw(String url, String jsonBody) {
        try {
            RequestBody requestBody = RequestBody.create(
                    jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = http.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    System.err.println("[GeminiService] HTTP " + response.code() + ": " + errBody);
                    // Trả về JSON lỗi để caller biết nguyên nhân
                    return "{\"__error__\": \"HTTP_" + response.code() + "\", \"detail\": " + objectMapper.valueToTree(errBody) + "}";
                }
                return response.body() != null ? response.body().string() : null;
            }
        } catch (Exception e) {
            System.err.println("[GeminiService] HTTP error: " + e.getMessage());
            return null;
        }
    }

    // ================================================================
    // PRIVATE — Build conversation history for multi-turn context
    // ================================================================

    /**
     * Xây dựng mảng contents cho Gemini từ lịch sử hội thoại + tin nhắn hiện tại.
     * Gemini yêu cầu luân phiên user/model. History được truyền vào đây là
     * các tin nhắn ĐÃ LƯU TRƯỚC khi gửi tin nhắn hiện tại.
     */
    private ArrayNode buildContentsWithHistory(List<ChatMessage> history, String currentUserMessage) {
        ArrayNode contents = objectMapper.createArrayNode();

        if (history != null && !history.isEmpty()) {
            // Chỉ lấy text messages của CUSTOMER (user) và SYSTEM (model bot)
            List<ChatMessage> filtered = history.stream()
                    .filter(m -> !m.isDeleted() && "TEXT".equals(m.getMessageType()))
                    .filter(m -> "CUSTOMER".equals(m.getSenderRole()) || "SYSTEM".equals(m.getSenderRole()))
                    .collect(java.util.stream.Collectors.toList());

            // Giới hạn 8 tin nhắn (4 lượt trao đổi) để tránh vượt context
            int startIdx = Math.max(0, filtered.size() - 8);
            filtered = filtered.subList(startIdx, filtered.size());

            // Đảm bảo bắt đầu bằng "user" (Gemini yêu cầu)
            while (!filtered.isEmpty() && "SYSTEM".equals(filtered.get(0).getSenderRole())) {
                filtered = filtered.subList(1, filtered.size());
            }

            // Thêm từng tin nhắn vào contents, giữ luân phiên user/model
            String lastAddedRole = null;
            for (ChatMessage msg : filtered) {
                String role = "CUSTOMER".equals(msg.getSenderRole()) ? "user" : "model";
                if (role.equals(lastAddedRole)) continue; // bỏ qua nếu trùng vai

                ObjectNode part = objectMapper.createObjectNode();
                part.put("text", msg.getContent());
                ObjectNode content = objectMapper.createObjectNode();
                content.put("role", role);
                content.set("parts", objectMapper.createArrayNode().add(part));
                contents.add(content);
                lastAddedRole = role;
            }

            // Nếu tin nhắn cuối cùng trong history là "user", cần skip để thêm tin hiện tại
            // (Gemini không cho phép 2 user messages liên tiếp trong contents thông thường,
            //  nhưng chúng ta đã lọc ở trên nên lastAddedRole sẽ là "model" hoặc null)
        }

        // Thêm tin nhắn hiện tại của user
        ObjectNode userPart = objectMapper.createObjectNode();
        userPart.put("text", currentUserMessage);
        ObjectNode userContent = objectMapper.createObjectNode();
        userContent.put("role", "user");
        userContent.set("parts", objectMapper.createArrayNode().add(userPart));
        contents.add(userContent);

        return contents;
    }

    // ================================================================
    // PRIVATE — Build Function Declarations JSON
    // ================================================================

    private ArrayNode buildFunctionDeclarations() {
        ArrayNode tools = objectMapper.createArrayNode();
        ObjectNode toolWrapper = objectMapper.createObjectNode();
        ArrayNode fnDeclarations = objectMapper.createArrayNode();

        fnDeclarations.add(buildFn("searchProducts",
                "Tìm kiếm sản phẩm theo tên. Dùng khi user hỏi về sản phẩm cụ thể hoặc muốn mua hàng.",
                Map.of(
                        "keyword", Map.of("type", "STRING", "description", "Từ khóa tên sản phẩm (VD: Đắc nhân tâm, Nhà giả kim)"),
                        "limit", Map.of("type", "INTEGER", "description", "Số kết quả tối đa, mặc định 5")
                ), List.of("keyword")));

        fnDeclarations.add(buildFn("getProductDetail",
                "Lấy thông tin chi tiết của 1 sản phẩm. Dùng khi cần so sánh hoặc tư vấn sâu.",
                Map.of("productId", Map.of("type", "STRING", "description", "ID của sản phẩm")),
                List.of("productId")));

        fnDeclarations.add(buildFn("suggestProducts",
                "Gợi ý sản phẩm phù hợp với ngân sách. Dùng khi user nhờ gợi ý hoặc hỏi nên mua gì.",
                Map.of(
                        "maxPrice", Map.of("type", "INTEGER", "description", "Ngân sách tối đa (VND), -1 nếu không giới hạn"),
                        "limit", Map.of("type", "INTEGER", "description", "Số gợi ý tối đa, mặc định 5")
                ), List.of()));

        fnDeclarations.add(buildFn("getOrdersByAccount",
                "Lấy danh sách đơn hàng của user. Dùng khi user hỏi về các đơn hàng của mình.",
                Map.of("accountId", Map.of("type", "STRING", "description", "ID tài khoản của user")),
                List.of("accountId")));

        fnDeclarations.add(buildFn("getOrderDetail",
                "Lấy chi tiết 1 đơn hàng cụ thể. Dùng khi user cung cấp mã đơn hàng.",
                Map.of("orderId", Map.of("type", "STRING", "description", "Mã đơn hàng cần tra cứu")),
                List.of("orderId")));

        fnDeclarations.add(buildFn("getUserShippingAddresses",
                "Lấy danh sách địa chỉ giao hàng đã lưu của user. Gọi trước khi đặt hàng để biết user có địa chỉ nào.",
                Map.of("accountId", Map.of("type", "STRING", "description", "ID tài khoản của user")),
                List.of("accountId")));

        fnDeclarations.add(buildFn("createShippingAddress",
                "Tạo địa chỉ giao hàng mới cho user. " +
                "Gọi NGAY khi user cung cấp thông tin địa chỉ (tên, số điện thoại, địa chỉ, thành phố) - " +
                "dù được viết theo bất kỳ định dạng nào (cách nhau bằng dấu /, phẩy, xuống dòng, v.v.). " +
                "Trích xuất các trường tương ứng và lưu vào hệ thống ngay. KHÔNG yêu cầu user nhập lại hay làm thủ công.",
                Map.of(
                        "accountId",       Map.of("type", "STRING", "description", "ID tài khoản user"),
                        "receiverName",    Map.of("type", "STRING", "description", "Tên người nhận hàng"),
                        "receiverPhone",   Map.of("type", "STRING", "description", "Số điện thoại người nhận"),
                        "receiverAddress", Map.of("type", "STRING", "description", "Số nhà, tên đường, phường/xã"),
                        "city",            Map.of("type", "STRING", "description", "Tỉnh hoặc thành phố")
                ), List.of("accountId", "receiverName", "receiverPhone", "receiverAddress", "city")));

        fnDeclarations.add(buildFn("createOrderFromChat",
                "Tạo đơn hàng sau khi user xác nhận mua. Trả về hóa đơn chi tiết. " +
                "Chỉ gọi sau khi đã có đủ: địa chỉ giao hàng (shippingId), phương thức thanh toán. " +
                "itemsJson để trống nếu muốn dùng giỏ hàng hiện tại.",
                Map.of(
                        "accountId",     Map.of("type", "STRING",  "description", "ID tài khoản của user"),
                        "itemsJson",     Map.of("type", "STRING",  "description", "JSON string danh sách sản phẩm. Để trống [] để tự động dùng giỏ hàng."),
                        "shippingId",    Map.of("type", "STRING",  "description", "ID địa chỉ giao hàng đã chọn"),
                        "paymentMethod", Map.of("type", "INTEGER", "description", "0=COD (tiền mặt), 1=Chuyển khoản")
                ), List.of("accountId", "shippingId")));

        fnDeclarations.add(buildFn("addToCart",
                "Thêm sản phẩm vào giỏ hàng DB. Gọi ngay sau khi user xác nhận chọn sản phẩm và số lượng. " +
                "Giỏ hàng này dùng chung với website, nên khi checkout sẽ có đầy đủ tất cả sản phẩm.",
                Map.of(
                        "accountId", Map.of("type", "STRING",  "description", "ID tài khoản user"),
                        "productId", Map.of("type", "STRING",  "description", "ID sản phẩm cần thêm"),
                        "quantity",  Map.of("type", "INTEGER", "description", "Số lượng muốn mua")
                ), List.of("accountId", "productId", "quantity")));

        fnDeclarations.add(buildFn("getCartItems",
                "Lấy toàn bộ sản phẩm trong giỏ hàng của user (bao gồm cả sản phẩm thêm qua chat và website). " +
                "Gọi trước khi checkout để xem lại giỏ hàng đầy đủ.",
                Map.of("accountId", Map.of("type", "STRING", "description", "ID tài khoản user")),
                List.of("accountId")));

        fnDeclarations.add(buildFn("removeFromCart",
                "Xóa 1 sản phẩm khỏi giỏ hàng. Gọi khi user muốn bỏ bớt món nào đó.",
                Map.of("cartItemId", Map.of("type", "STRING", "description", "ID của cartItem cần xóa")),
                List.of("cartItemId")));

        fnDeclarations.add(buildFn("createMomoPayment",
                "Tạo thanh toán MoMo cho đơn hàng. Gọi NGAY SAU createOrderFromChat nếu paymentMethod=1 (chuyển khoản). " +
                "Trả về payUrl và qrCodeUrl để user quét mã hoặc nhấn link thanh toán.",
                Map.of("orderId", Map.of("type", "STRING", "description", "Mã đơn hàng vừa được tạo")),
                List.of("orderId")));

        fnDeclarations.add(buildFn("getRefundsByOrder",
                "Lấy danh sách yêu cầu hoàn trả của 1 đơn hàng. Gọi để kiểm tra đơn đã có yêu cầu hoàn trả chưa.",
                Map.of("orderId", Map.of("type", "STRING", "description", "Mã đơn hàng cần kiểm tra")),
                List.of("orderId")));

        fnDeclarations.add(buildFn("createRefundFromChat",
                "Tạo yêu cầu hoàn trả đơn hàng. Gọi sau khi đã thu thập: mã đơn hàng, lý do hoàn trả và user xác nhận. " +
                "Chỉ tạo được khi đơn hàng thuộc về user hiện tại.",
                Map.of(
                        "accountId",    Map.of("type", "STRING",  "description", "ID tài khoản user"),
                        "orderId",      Map.of("type", "STRING",  "description", "Mã đơn hàng cần hoàn trả"),
                        "reason",       Map.of("type", "STRING",  "description", "Lý do hoàn trả (hàng lỗi, không đúng mô tả, v.v.)"),
                        "refundAmount", Map.of("type", "INTEGER", "description", "Số tiền hoàn trả (VND), 0 = hoàn toàn bộ giá trị đơn")
                ), List.of("accountId", "orderId", "reason")));

        toolWrapper.set("functionDeclarations", fnDeclarations);
        tools.add(toolWrapper);
        return tools;
    }

    private ObjectNode buildFn(String name, String description,
                                Map<String, Map<String, String>> params,
                                List<String> required) {
        ObjectNode fn = objectMapper.createObjectNode();
        fn.put("name", name);
        fn.put("description", description);

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "OBJECT");

        ObjectNode properties = objectMapper.createObjectNode();
        params.forEach((paramName, paramInfo) -> {
            ObjectNode prop = objectMapper.createObjectNode();
            prop.put("type", paramInfo.get("type"));
            prop.put("description", paramInfo.get("description"));
            properties.set(paramName, prop);
        });
        schema.set("properties", properties);

        ArrayNode requiredArr = objectMapper.createArrayNode();
        required.forEach(requiredArr::add);
        schema.set("required", requiredArr);

        fn.set("parameters", schema);
        return fn;
    }

    // ================================================================
    // PRIVATE — Tool dispatch
    // ================================================================

    private String dispatchTool(String toolName, JsonNode args, String contextAccountId) {
        return switch (toolName) {
            case "searchProducts" -> chatToolService.searchProducts(
                    args.path("keyword").asText(""), args.path("limit").asInt(5));
            case "getProductDetail" -> chatToolService.getProductDetail(
                    args.path("productId").asText(""));
            case "suggestProducts" -> chatToolService.suggestProducts(
                    args.path("maxPrice").asLong(-1L), args.path("limit").asInt(5));
            case "getOrdersByAccount" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                yield chatToolService.getOrdersByAccount(id);
            }
            case "getOrderDetail" -> chatToolService.getOrderDetail(
                    args.path("orderId").asText(""));
            case "getUserShippingAddresses" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                yield chatToolService.getUserShippingAddresses(id);
            }
            case "createShippingAddress" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                yield chatToolService.createShippingAddress(
                        id,
                        args.path("receiverName").asText(""),
                        args.path("receiverPhone").asText(""),
                        args.path("receiverAddress").asText(""),
                        args.path("city").asText(""));
            }
            case "createOrderFromChat" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                String itemsJson = args.path("itemsJson").asText("[]");
                String shippingId = args.path("shippingId").asText("");
                int paymentMethod = args.path("paymentMethod").asInt(0);
                yield chatToolService.createOrderFromChat(id, itemsJson, shippingId, paymentMethod);
            }
            case "addToCart" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                yield chatToolService.addToCart(id,
                        args.path("productId").asText(""),
                        args.path("quantity").asInt(1));
            }
            case "getCartItems" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                yield chatToolService.getCartItems(id);
            }
            case "removeFromCart" -> chatToolService.removeFromCart(
                    args.path("cartItemId").asText(""));
            case "createMomoPayment" -> chatToolService.createMomoPayment(
                    args.path("orderId").asText(""));
            case "getRefundsByOrder" -> chatToolService.getRefundsByOrder(
                    args.path("orderId").asText(""));
            case "createRefundFromChat" -> {
                String id = args.has("accountId") ? args.path("accountId").asText() : contextAccountId;
                yield chatToolService.createRefundFromChat(
                        id,
                        args.path("orderId").asText(""),
                        args.path("reason").asText(""),
                        args.path("refundAmount").asLong(0));
            }
            default -> "{\"error\":\"Unknown tool: " + toolName + "\"}";
        };
    }

    private String buildMetadataJson(String toolName, String toolResult) {
        if (toolResult == null || toolResult.contains("\"error\"")) return null;
        String type = switch (toolName) {
            case "searchProducts"          -> "PRODUCT_LIST";
            case "getProductDetail"        -> "PRODUCT_DETAIL";
            case "suggestProducts"         -> "PRODUCT_SUGGEST";
            case "getOrdersByAccount"      -> "ORDER_LIST";
            case "getOrderDetail"          -> "ORDER_DETAIL";
            case "getUserShippingAddresses"-> "SHIPPING_ADDRESSES";
            case "createShippingAddress"   -> "SHIPPING_CREATED";
            case "createOrderFromChat"     -> "ORDER_INVOICE";
            case "addToCart"               -> "CART_ITEM_ADDED";
            case "getCartItems"            -> "CART_ITEMS";
            case "removeFromCart"          -> "CART_ITEM_REMOVED";
            case "createMomoPayment"       -> "MOMO_PAYMENT";
            case "getRefundsByOrder"        -> "REFUND_LIST";
            case "createRefundFromChat"     -> "REFUND_CREATED";
            default                        -> "DATA";
        };
        return "{\"type\":\"" + type + "\",\"data\":" + toolResult + "}";
    }

    private String toolNameToIntent(String toolName) {
        return switch (toolName) {
            case "searchProducts", "getProductDetail"          -> "PRODUCT_ADVICE";
            case "suggestProducts"                             -> "PRODUCT_SUGGEST";
            case "getOrdersByAccount", "getOrderDetail"        -> "ORDER_LOOKUP";
            case "getUserShippingAddresses", "createShippingAddress", "createOrderFromChat" -> "ORDER_PLACE";
            case "addToCart", "getCartItems", "removeFromCart"                              -> "ORDER_PLACE";
            case "createMomoPayment"                                                        -> "PAYMENT";
            case "getRefundsByOrder", "createRefundFromChat"                                -> "REFUND";
            default                                            -> "GENERAL";
        };
    }

    private String getFallbackReply(String intent) {
        if (intent == null) return "Xin chào! Tôi có thể giúp gì cho bạn?";
        return switch (intent) {
            case "PRODUCT_ADVICE"  -> "Bạn đang muốn tìm hiểu về sản phẩm nào? Tôi sẽ tư vấn ngay!";
            case "PRODUCT_COMPARE" -> "Bạn muốn so sánh những sản phẩm nào? Cho tôi biết tên nhé!";
            case "ORDER_LOOKUP"    -> "Bạn muốn tra cứu đơn hàng nào? Hãy cung cấp mã đơn hàng!";
            case "PRODUCT_SUGGEST" -> "Cho tôi biết ngân sách để gợi ý sản phẩm phù hợp!";
            case "ORDER_PLACE"     -> "Bạn muốn mua sản phẩm gì? Hãy cho tôi biết tên sản phẩm và số lượng!";
            case "PAYMENT"         -> "Shop hỗ trợ thanh toán MoMo và COD. Bạn muốn chọn phương thức nào?";
            case "HUMAN_SUPPORT"   -> "Bạn có cần hỗ trợ trực tiếp từ nhân viên FruitShop không?";
            case "REFUND"          -> "Bạn muốn yêu cầu hoàn trả đơn hàng nào? Hãy cho tôi biết mã đơn hàng!";
            default                -> "Tôi có thể tư vấn sản phẩm, tra cứu đơn hàng hoặc hỗ trợ đặt hàng. Bạn cần gì?";
        };
    }
}
