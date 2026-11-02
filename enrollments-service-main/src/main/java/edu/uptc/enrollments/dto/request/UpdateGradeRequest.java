package edu.uptc.enrollments.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateGradeRequest(
        @NotBlank(message = "type is required (e.g. Midterm 1, Final Exam)")
        String type,

        @NotNull(message = "value is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "value must be at least 0.0")
        @DecimalMax(value = "5.0", inclusive = true, message = "value cannot exceed 5.0")
        BigDecimal value,

        @NotNull(message = "percentage is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "percentage must be at least 0.0")
        @DecimalMax(value = "100.0", inclusive = true, message = "percentage cannot exceed 100.0")
        BigDecimal percentage
) {
}
