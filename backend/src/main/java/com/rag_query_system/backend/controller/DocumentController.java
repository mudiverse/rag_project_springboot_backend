package com.rag_query_system.backend.controller;

import com.rag_query_system.backend.dto.DeleteResponse;
import com.rag_query_system.backend.dto.DocumentResponse;
import com.rag_query_system.backend.dto.QueryRequest;
import com.rag_query_system.backend.dto.QueryResponse;
import com.rag_query_system.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows React frontend to communicate without CORS issues
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload Document
     * POST /documents
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        DocumentResponse response = documentService.uploadDocument(file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * List Documents
     * GET /documents
     */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>> listDocuments() {
        List<DocumentResponse> response = documentService.listDocuments();
        return ResponseEntity.ok(response);
    }

    /**
     * Ask Question
     * POST /query
     */
    @PostMapping(value = "/query", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QueryResponse> askQuestion(@RequestBody QueryRequest request) {
        QueryResponse response = documentService.queryDocument(request.getDocumentId(), request.getQuestion());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete Document
     * DELETE /documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<DeleteResponse> deleteDocument(@PathVariable("documentId") String documentId) {
        DeleteResponse response = documentService.deleteDocument(documentId);
        return ResponseEntity.ok(response);
    }
}
