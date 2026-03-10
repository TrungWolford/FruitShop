package server.FruitShop.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service chuyên gọi Google Gemini API.
 * Được inject vào ChatServiceImpl để:
 *  1. detectIntent()      — Nhận diện ý định của user
 *  2. generateBotReply()  — Sinh câu trả lời tự nhiên
 *  3. extractMetadata()   — Trích xuất thông tin từ câu hỏi (tên sp, orderId...)
 */
@Service
public class GeminiService {

    private final Client client;
    private final String model;
    private final int maxOutputTokens;
    private final float temperature;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.max-output-tokens:1024}") int maxOutputTokens,
            @Value("${gemini.temperature:0.7}") float temperature) {

        this.client = Client.builder().apiKey(apiKey).build();
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
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
}
