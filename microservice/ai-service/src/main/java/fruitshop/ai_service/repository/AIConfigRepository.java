package fruitshop.ai_service.repository;

import fruitshop.ai_service.orchestrator.model.AIConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIConfigRepository extends JpaRepository<AIConfigEntity, String> {
}
