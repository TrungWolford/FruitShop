package fruitshop.ai_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import fruitshop.ai_service.dto.request.ChatBot.AiChatRequest;
import fruitshop.ai_service.dto.response.ChatBot.AiChatResponse;
import fruitshop.ai_service.dto.response.ChatBot.GeminiAgentResult;
import fruitshop.ai_service.service.ChatBot.GeminiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        GeminiAgentResult result = geminiService.agentChat(request.getMessage(), request.getAccountId());
        return ResponseEntity.ok(new AiChatResponse(result.reply(), result.metadata(), result.intent()));
    }
}
