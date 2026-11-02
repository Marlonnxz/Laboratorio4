package edu.uptc.subjects.dto;

import java.time.LocalDateTime;

public record SubjectDocumentResponse(
        Long id,
        Long subjectId,
        String originalName,
        String contentType,
        Long fileSize,
        LocalDateTime uploadedAt,
        String downloadUrl) {
}

