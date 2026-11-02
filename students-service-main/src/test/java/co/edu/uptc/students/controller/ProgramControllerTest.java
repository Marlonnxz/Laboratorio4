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

import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.dto.ProgramRequest;
import co.edu.uptc.students.dto.ProgramResponse;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.service.ProgramService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProgramController.class)
class ProgramControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProgramService programService;

    private ProgramResponse sampleProgram() {
        return new ProgramResponse(1L, "Ingenieria de Sistemas", "IS-01");
    }

    @Test
    void getAll_shouldReturn200WithPagedPrograms() throws Exception {
        PageResponse<ProgramResponse> page = new PageResponse<>(List.of(sampleProgram()), 0, 10, 1, 1);
        when(programService.getAll(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("IS-01"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getById_shouldReturn200_whenProgramExists() throws Exception {
        when(programService.getById(1L)).thenReturn(sampleProgram());

        mockMvc.perform(get("/api/programs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ingenieria de Sistemas"));
    }

    @Test
    void getById_shouldReturn404_whenProgramDoesNotExist() throws Exception {
        when(programService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Program with id 99 was not found"));

        mockMvc.perform(get("/api/programs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        ProgramRequest request = new ProgramRequest("Ingenieria de Sistemas", "IS-01");
        when(programService.create(any(ProgramRequest.class))).thenReturn(sampleProgram());

        mockMvc.perform(post("/api/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IS-01"));
    }

    @Test
    void create_shouldReturn400_whenNameOrCodeIsBlank() throws Exception {
        String invalidBody = """
                { "name": "", "code": "" }
                """;

        mockMvc.perform(post("/api/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn409_whenCodeAlreadyExists() throws Exception {
        ProgramRequest request = new ProgramRequest("Ingenieria de Sistemas", "IS-01");
        when(programService.create(any(ProgramRequest.class)))
                .thenThrow(new DuplicateResourceException("Program with code IS-01 already exists"));

        mockMvc.perform(post("/api/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        ProgramRequest request = new ProgramRequest("Ingenieria de Sistemas", "IS-01");
        when(programService.update(eq(1L), any(ProgramRequest.class))).thenReturn(sampleProgram());

        mockMvc.perform(put("/api/programs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn404_whenProgramDoesNotExist() throws Exception {
        ProgramRequest request = new ProgramRequest("Ingenieria de Sistemas", "IS-01");
        when(programService.update(eq(99L), any(ProgramRequest.class)))
                .thenThrow(new ResourceNotFoundException("Program with id 99 was not found"));

        mockMvc.perform(put("/api/programs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenProgramExists() throws Exception {
        mockMvc.perform(delete("/api/programs/1"))
                .andExpect(status().isNoContent());

        verify(programService, times(1)).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenProgramDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Program with id 99 was not found"))
                .when(programService).delete(99L);

        mockMvc.perform(delete("/api/programs/99"))
                .andExpect(status().isNotFound());
    }
}