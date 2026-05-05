package fruitshop.ai_service.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Detailed response for the admin test chat UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestChatResponse {
    private String reply;
    private List<String> tools_called;
    private List<String> rag_sources_used;
    private Long latency_ms;
    private String config_name;
}
