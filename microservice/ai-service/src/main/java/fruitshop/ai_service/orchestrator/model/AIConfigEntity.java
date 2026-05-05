package fruitshop.ai_service.orchestrator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIConfigEntity {
    
    @Id
    private String id; // "active_config"
    
    @Column(columnDefinition = "TEXT")
    private String systemPrompt;
    
    private Integer tone;
    private Integer verbosity;
    private Integer proactiveness;
    
    private String model;
    private String language;
    
    private LocalDateTime updatedAt;
}
