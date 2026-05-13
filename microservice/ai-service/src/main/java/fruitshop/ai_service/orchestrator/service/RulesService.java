package fruitshop.ai_service.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Manages business rules stored in Postgres.
 * Rules with is_active=false are excluded from the chatbot prompt.
 */
@Service
public class RulesService {
    private static final Logger log = LoggerFactory.getLogger(RulesService.class);
    private final JdbcTemplate jdbcTemplate;

    public RulesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initSchema() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ai_rules (
                    id BIGSERIAL PRIMARY KEY,
                    content TEXT NOT NULL,
                    priority INTEGER DEFAULT 1,
                    scope VARCHAR(50) DEFAULT 'all',
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                )
            """);
            log.info("ai_rules table initialized.");
        } catch (Exception e) {
            log.warn("ai_rules table init warning: {}", e.getMessage());
        }
    }

    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList(
            "SELECT id, content, priority, scope, is_active, created_at, updated_at " +
            "FROM ai_rules ORDER BY priority DESC, id ASC"
        );
    }

    /**
     * Returns only ACTIVE rules — used by chatbot prompt builder.
     */
    public List<String> findActiveRuleContents() {
        return jdbcTemplate.queryForList(
            "SELECT content FROM ai_rules WHERE is_active = TRUE ORDER BY priority DESC",
            String.class
        );
    }

    public Map<String, Object> create(String content, int priority, String scope) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ai_rules (content, priority, scope, is_active) VALUES (?, ?, ?, TRUE)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, content);
            ps.setInt(2, priority);
            ps.setString(3, scope);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        return findById(id != null ? id.longValue() : -1L);
    }

    public Map<String, Object> toggleActive(long id, boolean isActive) {
        int updated = jdbcTemplate.update(
            "UPDATE ai_rules SET is_active = ?, updated_at = NOW() WHERE id = ?",
            isActive, id
        );
        if (updated == 0) throw new RuntimeException("Rule not found: " + id);
        return findById(id);
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM ai_rules WHERE id = ?", id);
    }

    private Map<String, Object> findById(long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, content, priority, scope, is_active, created_at, updated_at FROM ai_rules WHERE id = ?",
            id
        );
        if (rows.isEmpty()) throw new RuntimeException("Rule not found: " + id);
        return rows.get(0);
    }
}
