package com.rag_query_system.backend.service;

import com.rag_query_system.backend.dto.DeleteResponse;
import com.rag_query_system.backend.dto.DocumentResponse;
import com.rag_query_system.backend.dto.FastApiIngestResponse;
import com.rag_query_system.backend.dto.QueryResponse;
import com.rag_query_system.backend.entity.DocumentEntity;
import com.rag_query_system.backend.exception.ResourceNotFoundException;
import com.rag_query_system.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CloudinaryService cloudinaryService;
    private final FastApiService fastApiService;

    /**
     * Handles document uploading flow:
     * 1. Uploads file to Cloudinary.
     * 2. Saves metadata to PostgreSQL with PENDING status.
     * 3. Triggers FastAPI /ingest.
     * 4. Updates PostgreSQL metadata status to INDEXED or FAILED.
     */
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) throws IOException {
        String documentId = "doc_" + UUID.randomUUID().toString().replace("-", "");
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "document.pdf";
        }

        log.info("Uploading PDF to Cloudinary: {}", originalFilename);
        Map<String, String> uploadResult = cloudinaryService.uploadPdf(file);
        String pdfUrl = uploadResult.get("url");
        String publicId = uploadResult.get("publicId");

        log.info("Saving metadata to PostgreSQL with status PENDING for doc ID: {}", documentId);
        DocumentEntity entity = DocumentEntity.builder()
                .documentId(documentId)
                .fileName(originalFilename)
                .pdfUrl(pdfUrl)
                .cloudinaryPublicId(publicId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        entity = documentRepository.save(entity);

        try {
            log.info("Calling FastAPI ingestion for doc ID: {}", documentId);
            FastApiIngestResponse ingestResponse = fastApiService.ingestDocument(documentId, pdfUrl);
            String status = (ingestResponse != null && ingestResponse.getStatus() != null) 
                    ? ingestResponse.getStatus() 
                    : "INDEXED";
            
            entity.setStatus(status);
            documentRepository.save(entity);
            log.info("Document successfully indexed: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to ingest document {} in FastAPI backend: {}", documentId, e.getMessage());
            entity.setStatus("FAILED");
            documentRepository.save(entity);
            throw new RuntimeException("Ingestion failed: " + e.getMessage(), e);
        }

        return DocumentResponse.builder()
                .documentId(entity.getDocumentId())
                .fileName(entity.getFileName())
                .status(entity.getStatus())
                .build();
    }

    /**
     * Lists all uploaded documents from database.
     */
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .map(entity -> DocumentResponse.builder()
                        .documentId(entity.getDocumentId())
                        .fileName(entity.getFileName())
                        .status(entity.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Queries the FastAPI backend after verifying document existence.
     */
    public QueryResponse queryDocument(String documentId, String question) {
        // Verify document exists
        documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        log.info("Querying FastAPI for document: {} (question: {})", documentId, question);
        return fastApiService.queryDocument(documentId, question);
    }

    /**
     * Deletes a document:
     * 1. Removes PDF from Cloudinary.
     * 2. Calls FastAPI to delete embeddings.
     * 3. Deletes metadata from PostgreSQL.
     */
    @Transactional
    public DeleteResponse deleteDocument(String documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        try {
            log.info("Deleting PDF from Cloudinary: {}", entity.getCloudinaryPublicId());
            cloudinaryService.deletePdf(entity.getCloudinaryPublicId());
        } catch (Exception e) {
            log.error("Failed to delete PDF from Cloudinary: {}", e.getMessage());
        }

        try {
            log.info("Deleting embeddings from FastAPI for: {}", documentId);
            fastApiService.deleteDocument(documentId);
        } catch (Exception e) {
            log.error("Failed to delete embeddings from FastAPI: {}", e.getMessage());
        }

        documentRepository.delete(entity);
        log.info("Successfully deleted document from database: {}", documentId);

        return DeleteResponse.builder()
                .status("DELETED")
                .build();
    }
}
