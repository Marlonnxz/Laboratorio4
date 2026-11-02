package edu.uptc.enrollments.service.impl;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateEnrollmentRequest;
import edu.uptc.enrollments.dto.request.UpdateEnrollmentRequest;
import edu.uptc.enrollments.dto.response.EnrollmentDetailResponse;
import edu.uptc.enrollments.dto.response.EnrollmentResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import edu.uptc.enrollments.entity.EnrollmentStatusHistory;
import edu.uptc.enrollments.exception.BadRequestException;
import edu.uptc.enrollments.exception.BusinessRuleException;
import edu.uptc.enrollments.exception.ResourceNotFoundException;
import edu.uptc.enrollments.mapper.EnrollmentMapper;
import edu.uptc.enrollments.repository.EnrollmentRepository;
import edu.uptc.enrollments.repository.specification.EnrollmentSpecification;
import edu.uptc.enrollments.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, EnrollmentMapper enrollmentMapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    @Transactional
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndPeriod(
                request.studentId(), request.courseId(), request.period())) {
            throw new BusinessRuleException(String.format(
                    "Student %d is already enrolled in course %d for period %s",
                    request.studentId(), request.courseId(), request.period()));
        }

        Enrollment enrollment = enrollmentMapper.toEntity(request);

        String initialReason = (request.reason() != null && !request.reason().isBlank())
                ? request.reason().trim()
                : "Initial enrollment registration";

        EnrollmentStatusHistory initialHistory = new EnrollmentStatusHistory(
                enrollment,
                enrollment.getStatus(),
                LocalDateTime.now(),
                initialReason
        );
        enrollment.addStatusHistory(initialHistory);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toResponse(savedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentResponse> getEnrollments(
            Long studentId,
            Long courseId,
            EnrollmentStatus status,
            String period,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean unpaged
    ) {
        Specification<Enrollment> spec = EnrollmentSpecification.withFilters(studentId, courseId, status, period);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort sort = Sort.by(direction, validSortBy);

        if (Boolean.TRUE.equals(unpaged)) {
            List<Enrollment> list = enrollmentRepository.findAll(spec, sort);
            return PagedResponse.unpaged(enrollmentMapper.toResponseList(list));
        }

        int validPageNumber = Math.max(0, pageNumber);
        int validPageSize = pageSize > 0 ? pageSize : 10;
        Pageable pageable = PageRequest.of(validPageNumber, validPageSize, sort);

        Page<Enrollment> page = enrollmentRepository.findAll(spec, pageable);
        return PagedResponse.fromPage(page.map(enrollmentMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentDetailResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + id + " was not found"));
        return enrollmentMapper.toDetailResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollment(Long id, UpdateEnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + id + " was not found"));

        boolean courseChanged = !enrollment.getCourseId().equals(request.courseId());
        boolean studentChanged = !enrollment.getStudentId().equals(request.studentId());
        boolean periodChanged = !enrollment.getPeriod().equals(request.period());

        if ((courseChanged || studentChanged || periodChanged) &&
                enrollmentRepository.existsByStudentIdAndCourseIdAndPeriod(
                        request.studentId(), request.courseId(), request.period())) {
            throw new BusinessRuleException(String.format(
                    "Student %d is already enrolled in course %d for period %s",
                    request.studentId(), request.courseId(), request.period()));
        }

        // Automatic Status Audit verification
        if (enrollment.getStatus() != request.status()) {
            if (request.reason() == null || request.reason().isBlank()) {
                throw new BadRequestException("Reason is required when changing enrollment status");
            }
            EnrollmentStatusHistory statusHistory = new EnrollmentStatusHistory(
                    enrollment,
                    request.status(),
                    LocalDateTime.now(),
                    request.reason().trim()
            );
            enrollment.addStatusHistory(statusHistory);
            enrollment.setStatus(request.status());
        }

        enrollment.setStudentId(request.studentId());
        enrollment.setCourseId(request.courseId());
        enrollment.setPeriod(request.period());
        if (request.enrollmentDate() != null) {
            enrollment.setEnrollmentDate(request.enrollmentDate());
        }

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toResponse(updatedEnrollment);
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + id + " was not found"));
        enrollmentRepository.delete(enrollment);
    }
}
