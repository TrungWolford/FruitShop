package fruitshop.ai_service.controller;

import fruitshop.ai_service.orchestrator.service.DocumentService;
import fruitshop.ai_service.orchestrator.service.RulesService;
import fruitshop.ai_service.orchestrator.service.VectorStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints at /api/admin/** matching the frontend's AI_SERVICE_BASE.
 * Frontend: /ai-service/api/admin/... → Gateway (StripPrefix=1) → /api/admin/...
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DocumentService documentService;
    private final VectorStoreService vectorStoreService;
    private final RulesService rulesService;

    public AdminController(DocumentService documentService,
                           VectorStoreService vectorStoreService,
                           RulesService rulesService) {
        this.documentService = documentService;
        this.vectorStoreService = vectorStoreService;
        this.rulesService = rulesService;
    }

    /**
     * GET /api/admin/rag-sources
     * Returns list of all indexed PDF sources.
     */
    @GetMapping("/rag-sources")
    public ResponseEntity<Map<String, Object>> getRagSources() {
        try {
            List<Map<String, Object>> rawSources = vectorStoreService.listSources();

            // Map DB rows to the frontend's RagSource interface
            List<Map<String, Object>> sources = rawSources.stream().map(row -> Map.<String, Object>of(
                "id",            String.valueOf(row.get("id")),
                "name",          String.valueOf(row.get("name")),
                "status",        String.valueOf(row.getOrDefault("status", "indexed")),
                "documentCount", row.getOrDefault("document_count", 0),
                "updatedAt",     row.get("updated_at") != null ? row.get("updated_at").toString() : ""
            )).toList();

            return ResponseEntity.ok(Map.of(
                "sources", sources,
                "totalDocuments", sources.stream()
                    .mapToInt(s -> (Integer) s.get("documentCount")).sum()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/rag-sources/upload
     * Upload a PDF file to be ingested into the vector DB.
     */
    @PostMapping("/rag-sources/upload")
    public ResponseEntity<Map<String, Object>> uploadRagSource(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File rong!"));
            }
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.pdf";
            if (!filename.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chi ho tro file PDF!"));
            }
            documentService.ingestPdf(file);
            return ResponseEntity.ok(Map.of(
                "message", "Da nap kien thuc thanh cong!",
                "filename", filename,
                "status", "indexed"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Loi khi nap PDF: " + e.getMessage()));
        }
    }

    /**
     * POST /api/admin/rag-sources/{id}/reindex
     * Re-trigger indexing for a source (placeholder — re-uses existing data).
     */
    @PostMapping("/rag-sources/{id}/reindex")
    public ResponseEntity<Map<String, Object>> reindexSource(@PathVariable String id) {
        return ResponseEntity.ok(Map.of(
            "message", "Reindex da duoc yeu cau. Vui long upload lai file de reindex.",
            "id", id
        ));
    }

    /**
     * DELETE /api/admin/rag-sources/{name}
     * Delete a source and all its chunks from the vector DB.
     */
    @DeleteMapping("/rag-sources/{name}")
    public ResponseEntity<Map<String, Object>> deleteSource(@PathVariable String name) {
        try {
            vectorStoreService.deleteBySource(name);
            // Also remove from metadata table
            vectorStoreService.deleteSourceMetadata(name);
            return ResponseEntity.ok(Map.of("message", "Da xoa: " + name));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ─── RULES ENDPOINTS ──────────────────────────────────────────────────────

    /**
     * GET /api/admin/rules
     * Returns all rules (active and inactive).
     */
    @GetMapping("/rules")
    public ResponseEntity<List<Map<String, Object>>> getRules() {
        return ResponseEntity.ok(rulesService.findAll());
    }

    /**
     * POST /api/admin/rules
     * Create a new rule.
     */
    @PostMapping("/rules")
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody Map<String, Object> body) {
        try {
            String content = (String) body.get("content");
            int priority = body.get("priority") instanceof Number n ? n.intValue() : 1;
            String scope = body.getOrDefault("scope", "all").toString();
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "content is required"));
            }
            return ResponseEntity.ok(rulesService.create(content, priority, scope));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/rules/{id}
     * Toggle active/inactive status.
     */
    @PatchMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> toggleRule(@PathVariable long id,
                                                          @RequestBody Map<String, Object> body) {
        try {
            boolean isActive = Boolean.TRUE.equals(body.get("is_active"));
            return ResponseEntity.ok(rulesService.toggleActive(id, isActive));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/rules/{id}
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable long id) {
        rulesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
