package server.FruitShop.dto.response.ChatBot;

/**
 * Kết quả trả về từ Gemini Agent sau khi thực hiện function calling.
 *
 * @param reply    Câu trả lời tự nhiên bằng tiếng Việt của AI
 * @param metadata JSON string chứa dữ liệu cấu trúc từ DB (sản phẩm, đơn hàng...)
 *                 null nếu không cần hiển thị dữ liệu structured
 * @param intent   Intent đã detect được (PRODUCT_ADVICE, ORDER_LOOKUP, ...)
 */
public record GeminiAgentResult(String reply, String metadata, String intent) {

    /** Kết quả đơn giản không có metadata */
    public static GeminiAgentResult of(String reply, String intent) {
        return new GeminiAgentResult(reply, null, intent);
    }

    /** Kết quả có cả reply và metadata */
    public static GeminiAgentResult of(String reply, String metadata, String intent) {
        return new GeminiAgentResult(reply, metadata, intent);
    }
}
