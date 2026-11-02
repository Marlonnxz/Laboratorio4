package co.edu.uptc.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.dto.StudentRequest;
import co.edu.uptc.students.dto.StudentResponse;
import co.edu.uptc.students.entity.Program;
import co.edu.uptc.students.entity.Student;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.ProgramRepository;
import co.edu.uptc.students.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private AzureBlobStorageService azureBlobStorageService;

    private StudentService studentService;

    private Program program;
    private Student student;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, programRepository, azureBlobStorageService);
        program = new Program(1L, "Ingeniería de Sistemas", "IS-01");
        student = new Student(1L, "Laura", "Gomez", "laura.gomez@uptc.edu.co", program);
    }

    @Test
    void getAll_shouldReturnPageResponse_whenStudentsExist() {
        Page<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<StudentResponse> response = studentService.getAll(
                null, null, null, null, 0, 10, "id", "asc");

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).email()).isEqualTo("laura.gomez@uptc.edu.co");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void getById_shouldReturnStudent_whenExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.programId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrowResourceNotFound_whenStudentDoesNotExist() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveStudent_whenEmailIsUniqueAndProgramExists() {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 1L);
        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(programRepository.findById(1L)).thenReturn(Optional.of(program));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.create(request);

        assertThat(response.email()).isEqualTo("laura.gomez@uptc.edu.co");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void create_shouldThrowDuplicateResource_whenEmailAlreadyExists() {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 1L);
        when(studentRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void create_shouldThrowResourceNotFound_whenProgramDoesNotExist() {
        StudentRequest request = new StudentRequest("Laura", "Gomez", "laura.gomez@uptc.edu.co", 99L);
        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(programRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void update_shouldUpdateStudent_whenDataIsValid() {
        StudentRequest request = new StudentRequest("Laura", "Gomez Updated", "laura.gomez@uptc.edu.co", 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(request.email())).thenReturn(Optional.of(student));
        when(programRepository.findById(1L)).thenReturn(Optional.of(program));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentResponse response = studentService.update(1L, request);

        assertThat(response).isNotNull();
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void update_shouldThrowDuplicateResource_whenEmailBelongsToAnotherStudent() {
        Student otherStudent = new Student(2L, "Andres", "Vargas", "andres.vargas@uptc.edu.co", program);
        StudentRequest request = new StudentRequest("Laura", "Gomez", "andres.vargas@uptc.edu.co", 1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(request.email())).thenReturn(Optional.of(otherStudent));

        assertThatThrownBy(() -> studentService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_shouldRemoveStudent_whenExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenStudentDoesNotExist() {
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}