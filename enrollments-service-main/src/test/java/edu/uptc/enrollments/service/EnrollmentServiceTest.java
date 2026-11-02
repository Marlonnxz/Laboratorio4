package edu.uptc.enrollments.service;

import edu.uptc.enrollments.dto.request.CreateEnrollmentRequest;
import edu.uptc.enrollments.dto.request.UpdateEnrollmentRequest;
import edu.uptc.enrollments.dto.response.EnrollmentDetailResponse;
import edu.uptc.enrollments.dto.response.EnrollmentResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import edu.uptc.enrollments.exception.BadRequestException;
import edu.uptc.enrollments.exception.BusinessRuleException;
import edu.uptc.enrollments.exception.ResourceNotFoundException;
import edu.uptc.enrollments.mapper.EnrollmentMapper;
import edu.uptc.enrollments.repository.EnrollmentRepository;
import edu.uptc.enrollments.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private Enrollment sampleEnrollment;
    private EnrollmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEnrollment = new Enrollment(1L, 12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.now());
        sampleResponse = new EnrollmentResponse(1L, 12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.now(), List.of());
    }

    @Test
    @DisplayName("Should create enrollment and generate automatic initial status audit history")
    void createEnrollment_Success_GeneratesInitialStatusHistory() {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest(
                12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.now(), "Regular semester enrollment");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndPeriod(12L, 7L, "2026-2")).thenReturn(false);
        when(enrollmentMapper.toEntity(request)).thenReturn(sampleEnrollment);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(sampleEnrollment);
        when(enrollmentMapper.toResponse(sampleEnrollment)).thenReturn(sampleResponse);

        EnrollmentResponse result = enrollmentService.createEnrollment(request);

        assertNotNull(result);
        assertEquals(12L, result.studentId());
        assertEquals(7L, result.courseId());
        assertEquals(EnrollmentStatus.ACTIVE, result.status());

        // Verify that initial status history was added to the entity before saving
        assertEquals(1, sampleEnrollment.getStatusHistory().size());
        assertEquals("Regular semester enrollment", sampleEnrollment.getStatusHistory().get(0).getReason());
        verify(enrollmentRepository).save(sampleEnrollment);
    }

    @Test
    @DisplayName("Should reject enrollment creation when duplicate student, course, and period exist")
    void createEnrollment_Duplicate_ThrowsBusinessRuleException() {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest(
                12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.now(), "Duplicate");

        when(enrollmentRepository.existsByStudentIdAndCourseIdAndPeriod(12L, 7L, "2026-2")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                enrollmentService.createEnrollment(request));

        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should update status and record new audit history entry when reason is provided")
    void updateEnrollment_StatusChange_Success_WithReason() {
        UpdateEnrollmentRequest request = new UpdateEnrollmentRequest(
                12L, 7L, "2026-2", EnrollmentStatus.CANCELLED, LocalDate.now(), "Schedule conflict with another subject");

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(sampleEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(sampleEnrollment);
        when(enrollmentMapper.toResponse(sampleEnrollment)).thenReturn(
                new EnrollmentResponse(1L, 12L, 7L, "2026-2", EnrollmentStatus.CANCELLED, LocalDate.now(), List.of()));

        EnrollmentResponse result = enrollmentService.updateEnrollment(1L, request);

        assertNotNull(result);
        assertEquals(EnrollmentStatus.CANCELLED, result.status());
        assertEquals(1, sampleEnrollment.getStatusHistory().size());
        assertEquals("Schedule conflict with another subject", sampleEnrollment.getStatusHistory().get(0).getReason());
    }

    @Test
    @DisplayName("Should throw BadRequestException when changing status without providing a reason")
    void updateEnrollment_StatusChange_WithoutReason_ThrowsBadRequestException() {
        UpdateEnrollmentRequest request = new UpdateEnrollmentRequest(
                12L, 7L, "2026-2", EnrollmentStatus.CANCELLED, LocalDate.now(), "");

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(sampleEnrollment));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                enrollmentService.updateEnrollment(1L, request));

        assertEquals("Reason is required when changing enrollment status", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when enrollment ID does not exist")
    void getEnrollmentById_NotFound_ThrowsResourceNotFoundException() {
        when(enrollmentRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                enrollmentService.getEnrollmentById(999L));
    }
}
