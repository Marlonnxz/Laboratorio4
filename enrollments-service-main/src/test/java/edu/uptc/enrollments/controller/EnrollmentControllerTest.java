package edu.uptc.enrollments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateEnrollmentRequest;
import edu.uptc.enrollments.dto.response.EnrollmentResponse;
import edu.uptc.enrollments.dto.response.GradeResponse;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import edu.uptc.enrollments.exception.ResourceNotFoundException;
import edu.uptc.enrollments.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @Test
    @DisplayName("GET /api/enrollments should return paginated response with 200 OK")
    void getEnrollments_ReturnsPagedResponse() throws Exception {
        GradeResponse grade = new GradeResponse(1L, 45L, "Midterm 1", BigDecimal.valueOf(4.2), BigDecimal.valueOf(30.0));
        EnrollmentResponse enrollment = new EnrollmentResponse(
                45L, 12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.parse("2026-08-20"), List.of(grade));
        PagedResponse<EnrollmentResponse> pagedResponse = new PagedResponse<>(
                List.of(enrollment), 0, 10, 1, 1);

        when(enrollmentService.getEnrollments(any(), any(), any(), any(), eq(0), eq(10), eq("id"), eq("asc"), eq(false)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(45))
                .andExpect(jsonPath("$.content[0].studentId").value(12))
                .andExpect(jsonPath("$.content[0].courseId").value(7))
                .andExpect(jsonPath("$.content[0].grades[0].type").value("Midterm 1"));
    }

    @Test
    @DisplayName("GET /api/enrollments?studentId=12 critical contract check for API Gateway /detalle")
    void getEnrollments_WithStudentId_ReturnsGradesList() throws Exception {
        GradeResponse grade1 = new GradeResponse(1L, 45L, "Midterm 1", BigDecimal.valueOf(4.2), BigDecimal.valueOf(30.0));
        EnrollmentResponse enrollment = new EnrollmentResponse(
                45L, 12L, 7L, "2026-2", EnrollmentStatus.ACTIVE, LocalDate.parse("2026-08-20"), List.of(grade1));
        PagedResponse<EnrollmentResponse> pagedResponse = new PagedResponse<>(
                List.of(enrollment), 0, 10, 1, 1);

        when(enrollmentService.getEnrollments(eq(12L), any(), any(), any(), eq(0), eq(10), eq("id"), eq("asc"), eq(false)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/enrollments")
                        .param("studentId", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].studentId").value(12))
                .andExpect(jsonPath("$.content[0].courseId").value(7))
                .andExpect(jsonPath("$.content[0].grades").isArray())
                .andExpect(jsonPath("$.content[0].grades[0].value").value(4.2));
    }

    @Test
    @DisplayName("POST /api/enrollments with invalid payload should return 400 Bad Request")
    void createEnrollment_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Missing required fields
        CreateEnrollmentRequest invalidRequest = new CreateEnrollmentRequest(
                null, -1L, "", null, null, null);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/enrollments/{id} when not found should return 404 with standard error body")
    void getEnrollmentById_NotFound_ReturnsNotFoundJson() throws Exception {
        when(enrollmentService.getEnrollmentById(999L))
                .thenThrow(new ResourceNotFoundException("Enrollment with id 999 was not found"));

        mockMvc.perform(get("/api/enrollments/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Enrollment with id 999 was not found"))
                .andExpect(jsonPath("$.path").value("/api/enrollments/999"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
