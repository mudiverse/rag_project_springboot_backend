package com.rag_query_system.backend.service;

import com.rag_query_system.backend.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class FastApiService {

    private final WebClient fastApiWebClient;

    /**
     * Calls FastAPI's POST /ingest endpoint to build embeddings for the document.
     */
    public FastApiIngestResponse ingestDocument(String documentId, String pdfUrl) {
        FastApiIngestRequest request = FastApiIngestRequest.builder()
                .documentId(documentId)
                .pdfUrl(pdfUrl)
                .build();

        return fastApiWebClient.post()
                .uri("/ingest")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FastApiIngestResponse.class)
                .block();
    }

    /**
     * Calls FastAPI's POST /rag/query endpoint to ask a question on a specific document.
     */
    public QueryResponse queryDocument(String documentId, String question) {
        QueryRequest request = QueryRequest.builder()
                .documentId(documentId)
                .question(question)
                .build();

        return fastApiWebClient.post()
                .uri("/rag/query")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(QueryResponse.class)
                .block();
    }

    /**
     * Calls FastAPI's DELETE /rag/document/{documentId} to delete all associated embeddings.
     */
    public DeleteResponse deleteDocument(String documentId) {
        return fastApiWebClient.delete()
                .uri("/rag/document/{documentId}", documentId)
                .retrieve()
                .bodyToMono(DeleteResponse.class)
                .block();
    }
}
