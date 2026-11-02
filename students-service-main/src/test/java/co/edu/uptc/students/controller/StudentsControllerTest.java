package co.edu.uptc.students.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import co.edu.uptc.students.dto.StudentRequest;
import co.edu.uptc.students.dto.StudentResponse;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(StudentController.class)
class StudentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private StudentResponse sampleStudent() {
        return new StudentResponse(1L, "Laura", "Gomez", "laura.gomez@uptc.edu.co", 2L);
    }

    // ---------- GET /api/students ----------

    @Test
    void getAll_shouldReturn200WithDefaultPaginationAndSorting_whenNoParamsProvided() throws Exception {
        PageResponse<StudentResponse> page = new PageResponse<>(List.of(sampleStudent()), 0, 10, 1, 1);
        when(studentService.getAll(isNull(), isNull(), isNull(), isNull(), eq(0), eq(10), eq("id"), eq("asc")))
                .thenReturn(page);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("laura.gomez@uptc.edu.co"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(studentService, times(1))
                .getAll(isNull(), isNull(), isNull(), isNull(), eq(0), eq(10), eq("id"), eq("asc"));
    }

    @Test
    void getAll_shouldForwardFiltersPaginationAndSorting_whenQueryParamsProvided() throws Exception {
        PageResponse<StudentResponse> page = new PageResponse<>(List.of(sampleStudent()), 0, 10, 1, 1);
        when(studentService.getAll(eq("Laura"), eq("Gomez"), eq(2L), eq("laura"), eq(0), eq(10),
                eq("lastName"), eq("desc"))).thenReturn(page);

        mockMvc.perform(get("/api/students")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "lastName")
                        .param("sortDirection", "desc")
                        .param("firstName", "Laura")
                        .param("lastName", "Gomez")
                        .param("programId", "2")
                        .param("email", "laura"))
                .andExpect(status().isOk());

        verify(studentService, times(1))
                .getAll(eq("Laura"), eq("Gomez"), eq(2L), eq("laura"), eq(0), eq(10), eq("lastName"), eq("desc"));
    }

    // ---------- GET /api/students/{id} ----------

    @Test
    void getById_shouldReturn200_whenStudentExists() throws Exception {
        when(studentService.getById(1L)).thenReturn(sampleStudent());

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Laura"));
    }

    @Test
    void getById_shouldReturn404WithErrorBody_whenStudentDoesNotExist() throws Exception {
        when(studentService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Student with id 99 was not found"));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/api/students/99"));
    }

    // ---------- POST /api/students ----------

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 2L);
        when(studentService.create(any(StudentRequest.class))).thenReturn(sampleStudent());

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("laura.gomez@uptc.edu.co"));
    }

    @Test
    void create_shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
        String invalidBody = """
                {
                  "firstName": "",
                  "lastName": "",
                  "email": "not-an-email",
                  "programId": null
                }
                """;

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_shouldReturn404_whenReferencedProgramDoesNotExist() throws Exception {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 99L);
        when(studentService.create(any(StudentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Program with id 99 was not found"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 2L);
        when(studentService.create(any(StudentRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Student with email laura.gomez@uptc.edu.co already exists"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ---------- PUT /api/students/{id} ----------

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 2L);
        when(studentService.update(eq(1L), any(StudentRequest.class))).thenReturn(sampleStudent());

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn404_whenStudentDoesNotExist() throws Exception {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 2L);
        when(studentService.update(eq(99L), any(StudentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Student with id 99 was not found"));

        mockMvc.perform(put("/api/students/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn409_whenEmailBelongsToAnotherStudent() throws Exception {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "andres@uptc.edu.co", 2L);
        when(studentService.update(eq(1L), any(StudentRequest.class)))
                .thenThrow(new DuplicateResourceException("Student with email andres@uptc.edu.co already exists"));

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ---------- DELETE /api/students/{id} ----------

    @Test
    void delete_shouldReturn204_whenStudentExists() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenStudentDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Student with id 99 was not found"))
                .when(studentService).delete(99L);

        mockMvc.perform(delete("/api/students/99"))
                .andExpect(status().isNotFound());
    }
}