package edu.uptc.enrollments.mapper;

import edu.uptc.enrollments.dto.request.CreateEnrollmentRequest;
import edu.uptc.enrollments.dto.response.EnrollmentDetailResponse;
import edu.uptc.enrollments.dto.response.EnrollmentResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EnrollmentMapper {

    private final GradeMapper gradeMapper;
    private final EnrollmentStatusHistoryMapper historyMapper;

    public EnrollmentMapper(GradeMapper gradeMapper, EnrollmentStatusHistoryMapper historyMapper) {
        this.gradeMapper = gradeMapper;
        this.historyMapper = historyMapper;
    }

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                enrollment.getPeriod(),
                enrollment.getStatus(),
                enrollment.getEnrollmentDate(),
                gradeMapper.toResponseList(enrollment.getGrades())
        );
    }

    public List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments) {
        if (enrollments == null) {
            return List.of();
        }
        return enrollments.stream()
                .map(this::toResponse)
                .toList();
    }

    public EnrollmentDetailResponse toDetailResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return new EnrollmentDetailResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                enrollment.getPeriod(),
                enrollment.getStatus(),
                enrollment.getEnrollmentDate(),
                gradeMapper.toResponseList(enrollment.getGrades()),
                historyMapper.toResponseList(enrollment.getStatusHistory())
        );
    }

    public Enrollment toEntity(CreateEnrollmentRequest request) {
        if (request == null) {
            return null;
        }
        EnrollmentStatus status = request.status() != null ? request.status() : EnrollmentStatus.ACTIVE;
        LocalDate date = request.enrollmentDate() != null ? request.enrollmentDate() : LocalDate.now();

        return new Enrollment(
                request.studentId(),
                request.courseId(),
                request.period(),
                status,
                date
        );
    }
}
