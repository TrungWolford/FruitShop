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
            sb.append("\n- Luon tra loi lich su, chuyen nghiep.");
        } else if (tone >= 70) {
            sb.append("\n- Tra loi than thien, dung emoji phu hop.");
        }

        if (verbosity <= 30) {
            sb.append("\n- Tra loi ngan gon duoi 3 cau.");
        } else if (verbosity >= 70) {
            sb.append("\n- Giai thich chi tiet khi can.");
        }

        if (proactiveness >= 70) {
            sb.append("\n- Chu dong goi y san pham lien quan va combo.");
        }

        List<Rule> rules = config != null ? config.getActiveRules() : null;
        if (rules != null && !rules.isEmpty()) {
            String rulesText = rules.stream()
                    .sorted(Comparator.comparing(Rule::getPriority, Comparator.nullsLast(Integer::compareTo)).reversed())
                    .map(r -> "- " + r.getText())
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
