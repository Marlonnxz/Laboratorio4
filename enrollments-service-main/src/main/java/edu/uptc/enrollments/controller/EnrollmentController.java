package edu.uptc.enrollments.controller;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateEnrollmentRequest;
import edu.uptc.enrollments.dto.request.UpdateEnrollmentRequest;
import edu.uptc.enrollments.dto.response.EnrollmentDetailResponse;
import edu.uptc.enrollments.dto.response.EnrollmentResponse;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import edu.uptc.enrollments.exception.ErrorResponse;
import edu.uptc.enrollments.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
@Tag(name = "Enrollments", description = "Operations for student course enrollments management")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an enrollment", description = "Registers a student in a course for a specific academic period and generates initial status audit entry.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Enrollment created successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Student is already enrolled in this course for this period",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Search enrollments with dynamic filtering and pagination",
            description = "Retrieves enrollments with their grades eagerly loaded. Crucial for API Gateway '/detalle' composition when queried with ?studentId={id}.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully")
    })
    public ResponseEntity<PagedResponse<EnrollmentResponse>> getEnrollments(
            @Parameter(description = "Filter by student numeric ID")
            @RequestParam(required = false) Long studentId,
            @Parameter(description = "Filter by course numeric ID")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "Filter by status: ACTIVE, CANCELLED, COMPLETED")
            @RequestParam(required = false) EnrollmentStatus status,
            @Parameter(description = "Filter by period (e.g. 2026-2)")
            @RequestParam(required = false) String period,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Field name to sort by")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Return unpaged complete list without truncation (useful for Gateway)")
            @RequestParam(defaultValue = "false") Boolean unpaged
    ) {
        PagedResponse<EnrollmentResponse> response = enrollmentService.getEnrollments(
                studentId, courseId, status, period, pageNumber, pageSize, sortBy, sortDirection, unpaged);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get enrollment detail by ID", description = "Retrieves an enrollment along with its grades and audit status history.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollment found",
                    content = @Content(schema = @Schema(implementation = EnrollmentDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Enrollment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnrollmentDetailResponse> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDetailResponse response = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an enrollment", description = "Updates enrollment data. If status is modified, the reason field is mandatory for audit logging.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollment updated successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing reason on status change",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Enrollment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicated enrollment for this student and course",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnrollmentResponse> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEnrollmentRequest request
    ) {
        EnrollmentResponse response = enrollmentService.updateEnrollment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an enrollment", description = "Deletes an enrollment record and cascades deletion to its associated grades and audit history.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Enrollment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
