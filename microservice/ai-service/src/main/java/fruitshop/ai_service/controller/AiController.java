package fruitshop.ai_service.controller;

import fruitshop.ai_service.orchestrator.config.AIConfigChangedEvent;
import fruitshop.ai_service.orchestrator.model.AIConfig;
import fruitshop.ai_service.orchestrator.service.AdminAiConfigStore;
import fruitshop.ai_service.orchestrator.service.DocumentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fruitshop.ai_service.dto.request.ChatBot.AiChatRequest;
import fruitshop.ai_service.dto.response.ChatBot.AiChatResponse;
import fruitshop.ai_service.dto.response.ChatBot.GeminiAgentResult;
import fruitshop.ai_service.service.ChatBot.GeminiService;
import lombok.Data;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;
    private final AdminAiConfigStore store;
    private final DocumentService documentService;
    private final ApplicationEventPublisher eventPublisher;

    public AiController(GeminiService geminiService, 
                        AdminAiConfigStore store,
                        DocumentService documentService,
                        ApplicationEventPublisher eventPublisher) {
        this.geminiService = geminiService;
        this.store = store;
        this.documentService = documentService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        GeminiAgentResult result = geminiService.agentChat(request.getMessage(), request.getAccountId());
        return ResponseEntity.ok(new AiChatResponse(result.reply(), result.metadata(), result.intent()));
    }

    // --- Admin AI Config Endpoints ---

    @GetMapping("/admin/config")
    public ResponseEntity<AdminAiConfigResponse> getConfig() {
        AIConfig cfg = store.get();
        return ResponseEntity.ok(toResponse(cfg));
    }

    @PutMapping("/admin/config")
    public ResponseEntity<AdminAiConfigResponse> setConfig(@RequestBody AdminAiConfigRequest req) {
        AIConfig cfg = new AIConfig();
        cfg.setSystemPrompt(req.getSystemPrompt() != null ? req.getSystemPrompt().trim() : "");
        cfg.setTone(req.getStyle() != null ? req.getStyle() : 50);
        cfg.setVerbosity(req.getLength() != null ? req.getLength() : 50);
        cfg.setProactiveness(req.getSales() != null ? req.getSales() : 50);
        cfg.setActiveRules(List.of());

        store.set(cfg);
        eventPublisher.publishEvent(new AIConfigChangedEvent(this));

        return ResponseEntity.ok(toResponse(cfg));
    }

    @PostMapping("/admin/rag-sources/upload")
    public ResponseEntity<String> ingestPdf(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File rong!");
            }
            documentService.ingestPdf(file);
            return ResponseEntity.ok("Da nap kien thuc tu PDF thanh cong!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Loi khi nap PDF: " + e.getMessage());
        }
    }

    @Data
    public static class AdminAiConfigRequest {
        private String name;
        private String systemPrompt;
        private Integer style;
        private Integer length;
        private Integer sales;
        private String language;
    }

    @Data
    public static class AdminAiConfigResponse {
        private String name;
        private String systemPrompt;
        private Integer style;
        private Integer length;
        private Integer sales;
        private String language;
    }

    private AdminAiConfigResponse toResponse(AIConfig cfg) {
        AdminAiConfigResponse r = new AdminAiConfigResponse();
        r.setName("FruitBot");
        r.setSystemPrompt(cfg.getSystemPrompt() != null
                && !"__default__".equals(cfg.getSystemPrompt())
                ? cfg.getSystemPrompt() : "");
        r.setStyle(cfg.getTone());
        r.setLength(cfg.getVerbosity());
        r.setSales(cfg.getProactiveness());
        r.setLanguage("vi");
        return r;
    }
}
