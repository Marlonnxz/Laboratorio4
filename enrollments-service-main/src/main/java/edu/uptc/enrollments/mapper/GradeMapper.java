package edu.uptc.enrollments.mapper;

import edu.uptc.enrollments.dto.request.CreateGradeRequest;
import edu.uptc.enrollments.dto.response.GradeResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.Grade;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GradeMapper {

    public GradeResponse toResponse(Grade grade) {
        if (grade == null) {
            return null;
        }
        return new GradeResponse(
                grade.getId(),
                grade.getEnrollment() != null ? grade.getEnrollment().getId() : null,
                grade.getType(),
                grade.getValue(),
                grade.getPercentage()
        );
    }

    public List<GradeResponse> toResponseList(List<Grade> grades) {
        if (grades == null) {
            return List.of();
        }
        return grades.stream()
                .map(this::toResponse)
                .toList();
    }

    public Grade toEntity(CreateGradeRequest request, Enrollment enrollment) {
        if (request == null) {
            return null;
        }
        return new Grade(
                enrollment,
                request.type(),
                request.value(),
                request.percentage()
        );
    }
}
