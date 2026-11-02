package co.edu.uptc.students.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.uptc.students.dto.DocumentRequest;
import co.edu.uptc.students.dto.DocumentResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    private DocumentResponse sampleDocument() {
        return new DocumentResponse(1L, 1L, "CC", "1002345678", LocalDate.of(2020, 3, 15));
    }

    @Test
    void getAll_shouldReturn200WithPagedDocuments() throws Exception {
        PageResponse<DocumentResponse> page = new PageResponse<>(List.of(sampleDocument()), 0, 10, 1, 1);
        when(documentService.getAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].number").value("1002345678"));
    }

    @Test
    void getById_shouldReturn200_whenDocumentExists() throws Exception {
        when(documentService.getById(1L)).thenReturn(sampleDocument());

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CC"));
    }

    @Test
    void getById_shouldReturn404_whenDocumentDoesNotExist() throws Exception {
        when(documentService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Document with id 99 was not found"));

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        DocumentRequest request = new DocumentRequest(1L, "CC", "1002345678", LocalDate.of(2020, 3, 15));
        when(documentService.create(any(DocumentRequest.class))).thenReturn(sampleDocument());

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1002345678"));
    }

    @Test
    void create_shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
        String invalidBody = """
                { "studentId": null, "type": "", "number": "", "issueDate": null }
                """;

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404_whenReferencedStudentDoesNotExist() throws Exception {
        DocumentRequest request = new DocumentRequest(99L, "CC", "1002345678", LocalDate.of(2020, 3, 15));
        when(documentService.create(any(DocumentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Student with id 99 was not found"));

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        DocumentRequest request = new DocumentRequest(1L, "CC", "1002345678", LocalDate.of(2020, 3, 15));
        when(documentService.update(eq(1L), any(DocumentRequest.class))).thenReturn(sampleDocument());

        mockMvc.perform(put("/api/documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn404_whenDocumentDoesNotExist() throws Exception {
        DocumentRequest request = new DocumentRequest(1L, "CC", "1002345678", LocalDate.of(2020, 3, 15));
        when(documentService.update(eq(99L), any(DocumentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Document with id 99 was not found"));

        mockMvc.perform(put("/api/documents/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenDocumentExists() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenDocumentDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Document with id 99 was not found"))
                .when(documentService).delete(99L);

        mockMvc.perform(delete("/api/documents/99"))
                .andExpect(status().isNotFound());
    }
}