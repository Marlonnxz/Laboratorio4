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
import co.edu.uptc.students.dto.StudentRequest;
import co.edu.uptc.students.dto.StudentResponse;
import co.edu.uptc.students.exception.ErrorResponse;
import co.edu.uptc.students.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Operations for managing students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "List students", description = "Returns a paginated, sortable and filterable list of students")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<StudentResponse>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) String email) {

        PageResponse<StudentResponse> response = studentService.getAll(
                firstName, lastName, programId, email, pageNumber, pageSize, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by id", description = "Returns a single student by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student found",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create student", description = "Creates a new student under an existing program")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Student created successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Referenced program not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Updates an existing student")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student updated successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Student or program not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", description = "Deletes a student by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Student deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload student photo", description = "Uploads a JPG or PNG photo (max 2 MB) to Azure Blob Storage and stores metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo uploaded successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or exceeds 2 MB limit"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<StudentResponse> uploadPhoto(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        StudentResponse response = studentService.uploadPhoto(id, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/photo")
    @Operation(summary = "Get student photo URL", description = "Generates and returns a temporary signed URL (SAS Token) to access the photo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo URL generated"),
            @ApiResponse(responseCode = "404", description = "Student or photo not found")
    })
    public ResponseEntity<java.util.Map<String, String>> getPhotoUrl(@PathVariable Long id) {
        String url = studentService.getPhotoSasUrl(id);
        return ResponseEntity.ok(java.util.Map.of("photoUrl", url));
    }

    @DeleteMapping("/{id}/photo")
    @Operation(summary = "Delete student photo", description = "Deletes photo from Azure Blob Storage and clears metadata")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        studentService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}