package fruitshop.ai_service.dto.response.ChatBot;

public record GeminiAgentResult(String reply, String metadata, String intent) {

    public static GeminiAgentResult of(String reply, String intent) {
        return new GeminiAgentResult(reply, null, intent);
    }

    public static GeminiAgentResult of(String reply, String metadata, String intent) {
        return new GeminiAgentResult(reply, metadata, intent);
    }
}
