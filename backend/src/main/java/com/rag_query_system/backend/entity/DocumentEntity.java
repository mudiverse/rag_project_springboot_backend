package com.rag_query_system.backend.entity;

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
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    @Column(name = "document_id")
    private String documentId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "pdf_url", nullable = false, length = 1024)
    private String pdfUrl;

    @Column(name = "cloudinary_public_id", nullable = false)
    private String cloudinaryPublicId;

    @Column(name = "status", nullable = false)
    private String status; // "PENDING", "INDEXED", "FAILED"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
