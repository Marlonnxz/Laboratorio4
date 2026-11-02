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

import co.edu.uptc.students.dto.DocumentRequest;
import co.edu.uptc.students.dto.DocumentResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.exception.ErrorResponse;
import co.edu.uptc.students.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Operations for managing student identification documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(summary = "List documents", description = "Returns a paginated list of student documents")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<DocumentResponse>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(documentService.getAll(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by id", description = "Returns a single document by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create document", description = "Creates a new identification document for a student")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document created successfully",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Referenced student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody DocumentRequest request) {
        DocumentResponse response = documentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update document", description = "Updates an existing document")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document updated successfully",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Document or student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentResponse> update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Deletes a document by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}