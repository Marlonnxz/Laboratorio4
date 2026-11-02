package edu.uptc.enrollments.service.impl;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateGradeRequest;
import edu.uptc.enrollments.dto.request.UpdateGradeRequest;
import edu.uptc.enrollments.dto.response.GradeResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.Grade;
import edu.uptc.enrollments.exception.BadRequestException;
import edu.uptc.enrollments.exception.BusinessRuleException;
import edu.uptc.enrollments.exception.ResourceNotFoundException;
import edu.uptc.enrollments.mapper.GradeMapper;
import edu.uptc.enrollments.repository.EnrollmentRepository;
import edu.uptc.enrollments.repository.GradeRepository;
import edu.uptc.enrollments.service.GradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class GradeServiceImpl implements GradeService {

    private static final BigDecimal MIN_GRADE = BigDecimal.ZERO;
    private static final BigDecimal MAX_GRADE = BigDecimal.valueOf(5.0);
    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100.0);

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeMapper gradeMapper;

    public GradeServiceImpl(
            GradeRepository gradeRepository,
            EnrollmentRepository enrollmentRepository,
            GradeMapper gradeMapper
    ) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeMapper = gradeMapper;
    }

    @Override
    @Transactional
    public GradeResponse createGrade(CreateGradeRequest request) {
        validateGradeValue(request.value());
        validateGradePercentage(request.percentage());

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + request.enrollmentId() + " was not found"));

        BigDecimal currentTotal = gradeRepository.sumPercentageByEnrollmentId(request.enrollmentId());
        BigDecimal newTotal = currentTotal.add(request.percentage());

        if (newTotal.compareTo(MAX_PERCENTAGE) > 0) {
            throw new BusinessRuleException(String.format(
                    "Total grade percentage for enrollment %d cannot exceed 100%%. Current total: %s%%, Attempted to add: %s%% (Sum: %s%%)",
                    request.enrollmentId(), currentTotal, request.percentage(), newTotal));
        }

        Grade grade = gradeMapper.toEntity(request, enrollment);
        Grade savedGrade = gradeRepository.save(grade);
        return gradeMapper.toResponse(savedGrade);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GradeResponse> getGrades(
            Long enrollmentId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort sort = Sort.by(direction, validSortBy);

        int validPageNumber = Math.max(0, pageNumber);
        int validPageSize = pageSize > 0 ? pageSize : 10;
        Pageable pageable = PageRequest.of(validPageNumber, validPageSize, sort);

        Page<Grade> page;
        if (enrollmentId != null) {
            page = gradeRepository.findByEnrollmentId(enrollmentId, pageable);
        } else {
            page = gradeRepository.findAll(pageable);
        }

        return PagedResponse.fromPage(page.map(gradeMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGradeById(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade with id " + id + " was not found"));
        return gradeMapper.toResponse(grade);
    }

    @Override
    @Transactional
    public GradeResponse updateGrade(Long id, UpdateGradeRequest request) {
        validateGradeValue(request.value());
        validateGradePercentage(request.percentage());

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade with id " + id + " was not found"));

        Long enrollmentId = grade.getEnrollment().getId();
        BigDecimal totalExcludingCurrent = gradeRepository.sumPercentageByEnrollmentIdExcludingGrade(enrollmentId, id);
        BigDecimal newTotal = totalExcludingCurrent.add(request.percentage());

        if (newTotal.compareTo(MAX_PERCENTAGE) > 0) {
            throw new BusinessRuleException(String.format(
                    "Total grade percentage for enrollment %d cannot exceed 100%%. Other grades sum: %s%%, Attempted: %s%% (Sum: %s%%)",
                    enrollmentId, totalExcludingCurrent, request.percentage(), newTotal));
        }

        grade.setType(request.type());
        grade.setValue(request.value());
        grade.setPercentage(request.percentage());

        Grade updatedGrade = gradeRepository.save(grade);
        return gradeMapper.toResponse(updatedGrade);
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade with id " + id + " was not found"));
        gradeRepository.delete(grade);
    }

    private void validateGradeValue(BigDecimal value) {
        if (value == null) {
            throw new BadRequestException("Grade value cannot be null");
        }
        if (value.compareTo(MIN_GRADE) < 0 || value.compareTo(MAX_GRADE) > 0) {
            throw new BadRequestException(String.format("Grade value must be between 0.0 and 5.0. Provided: %s", value));
        }
    }

    private void validateGradePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new BadRequestException("Grade percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(MAX_PERCENTAGE) > 0) {
            throw new BadRequestException(String.format("Grade percentage must be between 0.0 and 100.0. Provided: %s", percentage));
        }
    }
}
