package edu.uptc.enrollments.dto.request;

import edu.uptc.enrollments.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateStatusHistoryRequest(
        @NotNull(message = "enrollmentId is required")
        @Positive(message = "enrollmentId must be a positive number")
        Long enrollmentId,

        @NotNull(message = "status is required")
        EnrollmentStatus status,

        @NotBlank(message = "reason is required")
        String reason
) {
}
