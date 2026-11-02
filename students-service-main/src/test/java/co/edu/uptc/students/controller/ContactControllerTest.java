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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.uptc.students.dto.ContactRequest;
import co.edu.uptc.students.dto.ContactResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactService contactService;

    private ContactResponse sampleContact() {
        return new ContactResponse(1L, 1L, "Calle 10 # 2-31, Tunja", "3111234567", "Rosa Gomez", "3109876543");
    }

    @Test
    void getAll_shouldReturn200WithPagedContacts() throws Exception {
        PageResponse<ContactResponse> page = new PageResponse<>(List.of(sampleContact()), 0, 10, 1, 1);
        when(contactService.getAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].studentId").value(1));
    }

    @Test
    void getById_shouldReturn200_whenContactExists() throws Exception {
        when(contactService.getById(1L)).thenReturn(sampleContact());

        mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("3111234567"));
    }

    @Test
    void getById_shouldReturn404_whenContactDoesNotExist() throws Exception {
        when(contactService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Contact with id 99 was not found"));

        mockMvc.perform(get("/api/contacts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        ContactRequest request = new ContactRequest(1L, "Calle 10 # 2-31, Tunja", "3111234567",
                "Rosa Gomez", "3109876543");
        when(contactService.create(any(ContactRequest.class))).thenReturn(sampleContact());

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(1));
    }

    @Test
    void create_shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
        String invalidBody = """
                { "studentId": null, "address": "", "phone": "", "emergencyContactName": "", "emergencyContactPhone": "" }
                """;

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404_whenReferencedStudentDoesNotExist() throws Exception {
        ContactRequest request = new ContactRequest(99L, "Calle 10 # 2-31, Tunja", "3111234567",
                "Rosa Gomez", "3109876543");
        when(contactService.create(any(ContactRequest.class)))
                .thenThrow(new ResourceNotFoundException("Student with id 99 was not found"));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn409_whenStudentAlreadyHasContact() throws Exception {
        ContactRequest request = new ContactRequest(1L, "Calle 10 # 2-31, Tunja", "3111234567",
                "Rosa Gomez", "3109876543");
        when(contactService.create(any(ContactRequest.class)))
                .thenThrow(new DuplicateResourceException("Student with id 1 already has a contact"));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        ContactRequest request = new ContactRequest(1L, "Calle 10 # 2-31, Tunja", "3111234567",
                "Rosa Gomez", "3109876543");
        when(contactService.update(eq(1L), any(ContactRequest.class))).thenReturn(sampleContact());

        mockMvc.perform(put("/api/contacts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn404_whenContactDoesNotExist() throws Exception {
        ContactRequest request = new ContactRequest(1L, "Calle 10 # 2-31, Tunja", "3111234567",
                "Rosa Gomez", "3109876543");
        when(contactService.update(eq(99L), any(ContactRequest.class)))
                .thenThrow(new ResourceNotFoundException("Contact with id 99 was not found"));

        mockMvc.perform(put("/api/contacts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenContactExists() throws Exception {
        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isNoContent());

        verify(contactService, times(1)).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenContactDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Contact with id 99 was not found"))
                .when(contactService).delete(99L);

        mockMvc.perform(delete("/api/contacts/99"))
                .andExpect(status().isNotFound());
    }
}