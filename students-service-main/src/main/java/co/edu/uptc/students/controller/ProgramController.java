package co.edu.uptc.students.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.dto.ProgramRequest;
import co.edu.uptc.students.dto.ProgramResponse;
import co.edu.uptc.students.exception.ErrorResponse;
import co.edu.uptc.students.service.ProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/programs")
@Tag(name = "Programs", description = "Operations for managing academic programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    @Operation(summary = "List programs", description = "Returns a paginated list of academic programs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Programs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<ProgramResponse>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(programService.getAll(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get program by id", description = "Returns a single academic program by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program found",
                    content = @Content(schema = @Schema(implementation = ProgramResponse.class))),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProgramResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create program", description = "Creates a new academic program")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Program created successfully",
                    content = @Content(schema = @Schema(implementation = ProgramResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Program code already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProgramResponse> create(@Valid @RequestBody ProgramRequest request) {
        ProgramResponse response = programService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update program", description = "Updates an existing academic program")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program updated successfully",
                    content = @Content(schema = @Schema(implementation = ProgramResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Program code already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProgramResponse> update(@PathVariable Long id, @Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(programService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete program", description = "Deletes an academic program by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Program deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ResponseEntity.noContent().build();
    }
}