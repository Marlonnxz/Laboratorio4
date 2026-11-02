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

import co.edu.uptc.students.dto.ContactRequest;
import co.edu.uptc.students.dto.ContactResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.exception.ErrorResponse;
import co.edu.uptc.students.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contacts", description = "Operations for managing student contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    @Operation(summary = "List contacts", description = "Returns a paginated list of student contacts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    public ResponseEntity<PageResponse<ContactResponse>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(contactService.getAll(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contact by id", description = "Returns a single contact by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact found",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContactResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create contact", description = "Creates the contact information for a student (one per student)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contact created successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Referenced student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Student already has a contact",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update contact", description = "Updates an existing contact")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact updated successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact or student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Student already has a contact",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContactResponse> update(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete contact", description = "Deletes a contact by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contact deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}