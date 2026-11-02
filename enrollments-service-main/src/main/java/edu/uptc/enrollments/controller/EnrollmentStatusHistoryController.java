package edu.uptc.enrollments.controller;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateStatusHistoryRequest;
import edu.uptc.enrollments.dto.response.EnrollmentStatusHistoryResponse;
import edu.uptc.enrollments.exception.ErrorResponse;
import edu.uptc.enrollments.service.EnrollmentStatusHistoryService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollment-status-history")
@CrossOrigin(origins = "*")
@Tag(name = "Enrollment Status History", description = "Audit trail and lifecycle transition logs for enrollments")
public class EnrollmentStatusHistoryController {

    private final EnrollmentStatusHistoryService historyService;

    public EnrollmentStatusHistoryController(EnrollmentStatusHistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record status history entry", description = "Manually logs a lifecycle status transition for an enrollment.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Audit entry recorded successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentStatusHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Enrollment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnrollmentStatusHistoryResponse> createHistory(@Valid @RequestBody CreateStatusHistoryRequest request) {
        EnrollmentStatusHistoryResponse response = historyService.createHistory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get audit status history with pagination", description = "Retrieves audit entries, optionally filtered by enrollment ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit history retrieved successfully")
    })
    public ResponseEntity<PagedResponse<EnrollmentStatusHistoryResponse>> getHistory(
            @Parameter(description = "Filter by enrollment ID")
            @RequestParam(required = false) Long enrollmentId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Sort by field name")
            @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PagedResponse<EnrollmentStatusHistoryResponse> response = historyService.getHistory(
                enrollmentId, pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit status history entry by ID", description = "Retrieves a single audit log entry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit entry found",
                    content = @Content(schema = @Schema(implementation = EnrollmentStatusHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Audit entry not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnrollmentStatusHistoryResponse> getHistoryById(@PathVariable Long id) {
        EnrollmentStatusHistoryResponse response = historyService.getHistoryById(id);
        return ResponseEntity.ok(response);
    }
}
