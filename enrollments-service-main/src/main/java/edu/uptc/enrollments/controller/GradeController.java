package edu.uptc.enrollments.controller;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateGradeRequest;
import edu.uptc.enrollments.dto.request.UpdateGradeRequest;
import edu.uptc.enrollments.dto.response.GradeResponse;
import edu.uptc.enrollments.exception.ErrorResponse;
import edu.uptc.enrollments.service.GradeService;
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
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
@Tag(name = "Grades", description = "Operations for academic grades management associated with enrollments")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a grade", description = "Registers an academic grade for an enrollment. Value must be 0.0-5.0 and total percentage cannot exceed 100%.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grade created successfully",
                    content = @Content(schema = @Schema(implementation = GradeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid grade value or percentage",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Enrollment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Total percentage exceeds 100%",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GradeResponse> createGrade(@Valid @RequestBody CreateGradeRequest request) {
        GradeResponse response = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get grades with pagination", description = "Retrieves grades, optionally filtered by enrollment ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grades retrieved successfully")
    })
    public ResponseEntity<PagedResponse<GradeResponse>> getGrades(
            @Parameter(description = "Filter by enrollment ID")
            @RequestParam(required = false) Long enrollmentId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Sort by field name")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        PagedResponse<GradeResponse> response = gradeService.getGrades(enrollmentId, pageNumber, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get grade by ID", description = "Retrieves an academic grade by its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grade found",
                    content = @Content(schema = @Schema(implementation = GradeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grade not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GradeResponse> getGradeById(@PathVariable Long id) {
        GradeResponse response = gradeService.getGradeById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a grade", description = "Updates an existing grade. Total accumulated percentage must remain <= 100%.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grade updated successfully",
                    content = @Content(schema = @Schema(implementation = GradeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid values",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grade not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Total percentage exceeds 100%",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GradeResponse> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGradeRequest request
    ) {
        GradeResponse response = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a grade", description = "Deletes an academic grade record by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Grade deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Grade not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}
