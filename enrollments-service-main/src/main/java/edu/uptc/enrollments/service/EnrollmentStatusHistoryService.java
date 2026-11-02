package edu.uptc.enrollments.service;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateStatusHistoryRequest;
import edu.uptc.enrollments.dto.response.EnrollmentStatusHistoryResponse;

public interface EnrollmentStatusHistoryService {

    EnrollmentStatusHistoryResponse createHistory(CreateStatusHistoryRequest request);

    PagedResponse<EnrollmentStatusHistoryResponse> getHistory(
            Long enrollmentId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection
    );

    EnrollmentStatusHistoryResponse getHistoryById(Long id);
}
