package fruitshop.ai_service.dto.request.ChatBot;

import lombok.Data;

@Data
public class AiChatRequest {
    private String message;
    private String accountId;
}
