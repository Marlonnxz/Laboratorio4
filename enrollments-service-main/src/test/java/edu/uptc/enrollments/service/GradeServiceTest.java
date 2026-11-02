package edu.uptc.enrollments.service;

import edu.uptc.enrollments.dto.request.CreateGradeRequest;
import edu.uptc.enrollments.dto.response.GradeResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import edu.uptc.enrollments.entity.Grade;
import edu.uptc.enrollments.exception.BadRequestException;
import edu.uptc.enrollments.exception.BusinessRuleException;
import edu.uptc.enrollments.mapper.GradeMapper;
import edu.uptc.enrollments.repository.EnrollmentRepository;
import edu.uptc.enrollments.repository.GradeRepository;
import edu.uptc.enrollments.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private GradeMapper gradeMapper;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Enrollment sampleEnrollment;

    @BeforeEach
    void setUp() {
        sampleEnrollment = new Enrollment(1L, 12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.now());
    }

    @Test
    @DisplayName("Should successfully create a valid grade within limits")
    void createGrade_Success() {
        CreateGradeRequest request = new CreateGradeRequest(1L, "Midterm 1", BigDecimal.valueOf(4.2), BigDecimal.valueOf(30.0));
        Grade grade = new Grade(1L, sampleEnrollment, "Midterm 1", BigDecimal.valueOf(4.2), BigDecimal.valueOf(30.0));
        GradeResponse expectedResponse = new GradeResponse(1L, 1L, "Midterm 1", BigDecimal.valueOf(4.2), BigDecimal.valueOf(30.0));

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(sampleEnrollment));
        when(gradeRepository.sumPercentageByEnrollmentId(1L)).thenReturn(BigDecimal.valueOf(40.0));
        when(gradeMapper.toEntity(request, sampleEnrollment)).thenReturn(grade);
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toResponse(grade)).thenReturn(expectedResponse);

        GradeResponse result = gradeService.createGrade(request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(4.2), result.value());
        assertEquals(BigDecimal.valueOf(30.0), result.percentage());
    }

    @Test
    @DisplayName("Should reject grade when value is negative")
    void createGrade_ValueBelowZero_ThrowsBadRequestException() {
        CreateGradeRequest request = new CreateGradeRequest(1L, "Quiz 1", BigDecimal.valueOf(-0.5), BigDecimal.valueOf(10.0));

        assertThrows(BadRequestException.class, () -> gradeService.createGrade(request));
    }

    @Test
    @DisplayName("Should reject grade when value exceeds 5.0")
    void createGrade_ValueAboveFive_ThrowsBadRequestException() {
        CreateGradeRequest request = new CreateGradeRequest(1L, "Quiz 1", BigDecimal.valueOf(5.1), BigDecimal.valueOf(10.0));

        assertThrows(BadRequestException.class, () -> gradeService.createGrade(request));
    }

    @Test
    @DisplayName("Should reject grade when total percentage exceeds 100%")
    void createGrade_PercentageExceeds100_ThrowsBusinessRuleException() {
        CreateGradeRequest request = new CreateGradeRequest(1L, "Final", BigDecimal.valueOf(4.0), BigDecimal.valueOf(50.0));

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(sampleEnrollment));
        when(gradeRepository.sumPercentageByEnrollmentId(1L)).thenReturn(BigDecimal.valueOf(60.0));

        assertThrows(BusinessRuleException.class, () -> gradeService.createGrade(request));
    }
}
