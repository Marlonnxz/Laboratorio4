package edu.uptc.enrollments.dto.response;

import edu.uptc.enrollments.entity.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentStatusHistoryResponse(
        Long id,
        Long enrollmentId,
        EnrollmentStatus status,
        LocalDateTime date,
        String reason
) {
}
