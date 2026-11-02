package edu.uptc.enrollments.dto.request;

import edu.uptc.enrollments.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record UpdateEnrollmentRequest(
        @NotNull(message = "studentId is required")
        @Positive(message = "studentId must be a positive number")
        Long studentId,

        @NotNull(message = "courseId is required")
        @Positive(message = "courseId must be a positive number")
        Long courseId,

        @NotBlank(message = "period is required")
        @Pattern(regexp = "^\\d{4}-[1-2]$", message = "period must match format YYYY-N (e.g. 2026-2)")
        String period,

        @NotNull(message = "status is required")
        EnrollmentStatus status,

        LocalDate enrollmentDate,

        String reason
) {
}
