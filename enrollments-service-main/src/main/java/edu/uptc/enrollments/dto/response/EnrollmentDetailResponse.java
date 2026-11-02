package edu.uptc.enrollments.dto.response;

import edu.uptc.enrollments.entity.EnrollmentStatus;

import java.time.LocalDate;
import java.util.List;

public record EnrollmentDetailResponse(
        Long id,
        Long studentId,
        Long courseId,
        String period,
        EnrollmentStatus status,
        LocalDate enrollmentDate,
        List<GradeResponse> grades,
        List<EnrollmentStatusHistoryResponse> statusHistory
) {
}
