package fruitshop.ai_service.orchestrator.service;

import fruitshop.ai_service.orchestrator.model.AIConfig;
import fruitshop.ai_service.orchestrator.model.RAGChunk;
import fruitshop.ai_service.orchestrator.model.Rule;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBuilderServiceTest {

    @Test
    void buildSystemPrompt_includesRulesAndRag() {
        PromptBuilderService service = new PromptBuilderService();

        Rule rule = new Rule();
        rule.setText("Always verify stock");
        rule.setPriority(10);

        AIConfig config = new AIConfig();
        config.setSystemPrompt("Base persona");
        config.setTone(80);
        config.setVerbosity(80);
        config.setProactiveness(80);
        config.setActiveRules(List.of(rule));

        List<RAGChunk> rag = List.of(new RAGChunk("Apple Fuji 1kg 120k", "catalog", 0.9));

        String prompt = service.buildSystemPrompt(config, rag, "hello");

        assertTrue(prompt.contains("Base persona"));
        assertTrue(prompt.contains("Quy tac bat buoc"));
        assertTrue(prompt.contains("Apple Fuji"));
        assertTrue(prompt.contains("emoji"));
    }
}
