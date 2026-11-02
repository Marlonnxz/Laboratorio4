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

import co.edu.uptc.students.dto.ContactRequest;
import co.edu.uptc.students.dto.ContactResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.entity.Contact;
import co.edu.uptc.students.entity.Program;
import co.edu.uptc.students.entity.Student;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.ContactRepository;
import co.edu.uptc.students.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private StudentRepository studentRepository;

    private ContactService contactService;

    private Student student;
    private Contact contact;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository, studentRepository);
        Program program = new Program(1L, "Ingeniería de Sistemas", "IS-01");
        student = new Student(1L, "Laura", "Gomez", "laura.gomez@uptc.edu.co", program);
        contact = new Contact(1L, "Calle 1 # 2-3", "3001234567", "Ana Gomez", "3007654321", student);
    }

    @Test
    void getAll_shouldReturnPageResponse_whenContactsExist() {
        Page<Contact> page = new PageImpl<>(List.of(contact));
        when(contactRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<ContactResponse> response = contactService.getAll(0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).studentId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldReturnContact_whenExists() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        ContactResponse response = contactService.getById(1L);

        assertThat(response.address()).isEqualTo("Calle 1 # 2-3");
    }

    @Test
    void getById_shouldThrowResourceNotFound_whenContactDoesNotExist() {
        when(contactRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldSaveContact_whenStudentExistsAndHasNoContact() {
        ContactRequest request = new ContactRequest(1L, "Calle 1 # 2-3", "3001234567", "Ana Gomez", "3007654321");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(contactRepository.existsByStudentId(1L)).thenReturn(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        ContactResponse response = contactService.create(request);

        assertThat(response.studentId()).isEqualTo(1L);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void create_shouldThrowResourceNotFound_whenStudentDoesNotExist() {
        ContactRequest request = new ContactRequest(99L, "Calle 1 # 2-3", "3001234567", "Ana Gomez", "3007654321");
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void create_shouldThrowDuplicateResource_whenStudentAlreadyHasContact() {
        ContactRequest request = new ContactRequest(1L, "Calle 1 # 2-3", "3001234567", "Ana Gomez", "3007654321");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(contactRepository.existsByStudentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> contactService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void update_shouldUpdateContact_whenDataIsValid() {
        ContactRequest request = new ContactRequest(1L, "Calle Nueva", "3001234567", "Ana Gomez", "3007654321");
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(contactRepository.findByStudentId(1L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        ContactResponse response = contactService.update(1L, request);

        assertThat(response).isNotNull();
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void delete_shouldRemoveContact_whenExists() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        contactService.delete(1L);

        verify(contactRepository, times(1)).delete(contact);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenContactDoesNotExist() {
        when(contactRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}