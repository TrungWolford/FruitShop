package fruitshop.ai_service.dto.response.ChatBot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiChatResponse {
    private String reply;
    private String metadata;
    private String intent;
}
