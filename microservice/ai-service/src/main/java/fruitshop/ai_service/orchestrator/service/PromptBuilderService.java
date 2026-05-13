package fruitshop.ai_service.orchestrator.service;

import fruitshop.ai_service.orchestrator.model.AIConfig;
import fruitshop.ai_service.orchestrator.model.ConversationTurn;
import fruitshop.ai_service.orchestrator.model.RAGChunk;
import fruitshop.ai_service.orchestrator.model.Rule;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Builds prompts from system config, rules, RAG, and history.
 */
@Service
public class PromptBuilderService {

    private final RulesService rulesService;

    public PromptBuilderService(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    /**
     * Builds the system prompt with rules and RAG context.
     */
    public String buildSystemPrompt(AIConfig config, List<RAGChunk> ragChunks, String userMessage) {
        StringBuilder sb = new StringBuilder();
        
        String basePrompt = config != null ? config.getSystemPrompt() : null;
        if (basePrompt == null || basePrompt.isBlank() || "__default__".equals(basePrompt)) {
            basePrompt = "Ban la FruitBot, tro ly thong minh cua cua hang FruitShop. " +
                         "Nhiem vu cua ban la tu van cac loai trai cay tuoi ngon, gia ca hop ly va ho tro cham soc khach hang.";
        }
        sb.append(basePrompt);

        int tone = config != null && config.getTone() != null ? config.getTone() : 50;
        int verbosity = config != null && config.getVerbosity() != null ? config.getVerbosity() : 50;
        int proactiveness = config != null && config.getProactiveness() != null ? config.getProactiveness() : 50;

        if (tone <= 30) {
            sb.append("\n- Luôn trả lời lịch sự, chuyên nghiệp.");
        } else if (tone >= 70) {
            sb.append("\n- Trả lời thân thiện, dùng emoji phù hợp.");
        }

        if (verbosity <= 30) {
            sb.append("\n- Trả lời ngắn gọn dưới 3 câu.");
        } else if (verbosity >= 70) {
            sb.append("\n- Giải thích chi tiết khi cần.");
        }

        if (proactiveness >= 70) {
            sb.append("\n- Chủ động gợi ý sản phẩm liên quan và combo.");
        }

        // Load only active rules from DB
        List<String> activeRules = rulesService.findActiveRuleContents();
        if (!activeRules.isEmpty()) {
            String rulesText = activeRules.stream()
                    .map(r -> "- " + r)
                    .collect(Collectors.joining("\n"));
            sb.append("\n\n## Quy tac bat buoc:\n").append(rulesText);
        }

        if (ragChunks != null && !ragChunks.isEmpty()) {
            String ragText = ragChunks.stream()
                    .map(RAGChunk::getContent)
                    .collect(Collectors.joining("\n---\n"));
            sb.append("\n\n## Thong tin san pham lien quan:\n").append(ragText);
        }

        return sb.toString().trim();
    }

    /**
     * Builds a readable conversation history section.
     */
    public String buildConversationHistory(List<ConversationTurn> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }
        return history.stream()
                .map(t -> t.getRole() + ": " + t.getContent())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Builds the final prompt passed to the model.
     */
    public String buildFullPrompt(String systemPrompt, String history, String userMessage) {
        StringBuilder sb = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            sb.append(systemPrompt).append("\n\n");
        }
        if (history != null && !history.isBlank()) {
            sb.append("## Lich su hoi thoai:\n").append(history).append("\n\n");
        }
        sb.append("User: ").append(userMessage);
        return sb.toString();
    }
}
