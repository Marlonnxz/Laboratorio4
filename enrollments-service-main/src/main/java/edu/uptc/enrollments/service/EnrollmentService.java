package edu.uptc.enrollments.service;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateEnrollmentRequest;
import edu.uptc.enrollments.dto.request.UpdateEnrollmentRequest;
import edu.uptc.enrollments.dto.response.EnrollmentDetailResponse;
import edu.uptc.enrollments.dto.response.EnrollmentResponse;
import edu.uptc.enrollments.entity.EnrollmentStatus;

public interface EnrollmentService {

    EnrollmentResponse createEnrollment(CreateEnrollmentRequest request);

    PagedResponse<EnrollmentResponse> getEnrollments(
            Long studentId,
            Long courseId,
            EnrollmentStatus status,
            String period,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean unpaged
    );

    EnrollmentDetailResponse getEnrollmentById(Long id);

    EnrollmentResponse updateEnrollment(Long id, UpdateEnrollmentRequest request);

    void deleteEnrollment(Long id);
}
