package fruitshop.ai_service.orchestrator.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.List;

/**
 * Reads PDF files using Apache PDFBox, chunks the text,
 * and stores chunks in the vector database via VectorStoreService.
 * No Spring AI dependency — fully compatible with Spring Boot 4.x.
 */
@Service
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final int CHUNK_SIZE = 800;       // ~800 chars per chunk
    private static final int CHUNK_OVERLAP = 100;    // overlap between chunks

    private final VectorStoreService vectorStoreService;

    public DocumentService(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    public void ingestPdf(MultipartFile file) {
        String filename = file.getOriginalFilename();
        log.info("Starting PDF ingestion: {}", filename);

        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String fullText = stripper.getText(document);

                // Delete old chunks from the same file before re-ingesting
                vectorStoreService.deleteBySource(filename);

                List<String> chunks = chunkText(fullText);
                log.info("Split PDF into {} chunks, storing in vector DB...", chunks.size());

                for (String chunk : chunks) {
                    if (!chunk.isBlank()) {
                        vectorStoreService.storeChunk(chunk, filename);
                    }
                }
                // Update metadata table so this source shows up in the admin list
                vectorStoreService.upsertSourceMetadata(filename, chunks.size());
                log.info("PDF ingestion complete: {} chunks stored from '{}'", chunks.size(), filename);
            }
        } catch (Exception e) {
            log.error("PDF ingestion failed for '{}': {}", filename, e.getMessage());
            throw new RuntimeException("Failed to ingest PDF: " + filename, e);
        }
    }

    /**
     * Split text into overlapping chunks for better RAG retrieval.
     */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end).trim());
            start += (CHUNK_SIZE - CHUNK_OVERLAP);
        }
        return chunks;
    }
}
