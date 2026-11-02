package co.edu.uptc.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

import co.edu.uptc.students.dto.DocumentRequest;
import co.edu.uptc.students.dto.DocumentResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.entity.Document;
import co.edu.uptc.students.entity.Program;
import co.edu.uptc.students.entity.Student;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.DocumentRepository;
import co.edu.uptc.students.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private StudentRepository studentRepository;

    private DocumentService documentService;

    private Student student;
    private Document document;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(documentRepository, studentRepository);
        Program program = new Program(1L, "Ingeniería de Sistemas", "IS-01");
        student = new Student(1L, "Laura", "Gomez", "laura.gomez@uptc.edu.co", program);
        document = new Document(1L, "CC", "1002003000", LocalDate.of(2020, 5, 10), student);
    }

    @Test
    void getAll_shouldReturnPageResponse_whenDocumentsExist() {
        Page<Document> page = new PageImpl<>(List.of(document));
        when(documentRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<DocumentResponse> response = documentService.getAll(0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).number()).isEqualTo("1002003000");
    }

    @Test
    void getById_shouldReturnDocument_whenExists() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        DocumentResponse response = documentService.getById(1L);

        assertThat(response.type()).isEqualTo("CC");
    }

    @Test
    void getById_shouldThrowResourceNotFound_whenDocumentDoesNotExist() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldSaveDocument_whenStudentExists() {
        DocumentRequest request = new DocumentRequest(1L, "CC", "1002003000", LocalDate.of(2020, 5, 10));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        DocumentResponse response = documentService.create(request);

        assertThat(response.studentId()).isEqualTo(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void create_shouldThrowResourceNotFound_whenStudentDoesNotExist() {
        DocumentRequest request = new DocumentRequest(99L, "CC", "1002003000", LocalDate.of(2020, 5, 10));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void update_shouldUpdateDocument_whenDataIsValid() {
        DocumentRequest request = new DocumentRequest(1L, "TI", "1002003000", LocalDate.of(2020, 5, 10));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        DocumentResponse response = documentService.update(1L, request);

        assertThat(response).isNotNull();
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void delete_shouldRemoveDocument_whenExists() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        documentService.delete(1L);

        verify(documentRepository, times(1)).delete(document);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenDocumentDoesNotExist() {
        when(documentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}