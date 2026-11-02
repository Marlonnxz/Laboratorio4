package edu.uptc.enrollments.dto.response;

import java.math.BigDecimal;

public record GradeResponse(
        Long id,
        Long enrollmentId,
        String type,
        BigDecimal value,
        BigDecimal percentage
) {
}
