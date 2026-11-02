package edu.uptc.enrollments.service;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateGradeRequest;
import edu.uptc.enrollments.dto.request.UpdateGradeRequest;
import edu.uptc.enrollments.dto.response.GradeResponse;

public interface GradeService {

    GradeResponse createGrade(CreateGradeRequest request);

    PagedResponse<GradeResponse> getGrades(
            Long enrollmentId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection
    );

    GradeResponse getGradeById(Long id);

    GradeResponse updateGrade(Long id, UpdateGradeRequest request);

    void deleteGrade(Long id);
}
