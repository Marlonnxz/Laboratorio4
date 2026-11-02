package co.edu.uptc.students.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.students.dto.ContactRequest;
import co.edu.uptc.students.dto.ContactResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.entity.Contact;
import co.edu.uptc.students.entity.Student;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.ContactRepository;
import co.edu.uptc.students.repository.StudentRepository;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final StudentRepository studentRepository;

    public ContactService(ContactRepository contactRepository, StudentRepository studentRepository) {
        this.contactRepository = contactRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactResponse> getAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Contact> page = contactRepository.findAll(pageable);

        List<ContactResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {
        return toResponse(findContactOrThrow(id));
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {
        Student student = findStudentOrThrow(request.studentId());

        if (contactRepository.existsByStudentId(request.studentId())) {
            throw new DuplicateResourceException(
                    "Student with id " + request.studentId() + " already has a contact");
        }

        Contact contact = new Contact(null, request.address(), request.phone(), request.emergencyContactName(),
                request.emergencyContactPhone(), student);

        return toResponse(contactRepository.save(contact));
    }

    @Transactional
    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = findContactOrThrow(id);
        Student student = findStudentOrThrow(request.studentId());

        contactRepository.findByStudentId(request.studentId()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException(
                        "Student with id " + request.studentId() + " already has a contact");
            }
        });

        contact.setAddress(request.address());
        contact.setPhone(request.phone());
        contact.setEmergencyContactName(request.emergencyContactName());
        contact.setEmergencyContactPhone(request.emergencyContactPhone());
        contact.setStudent(student);

        return toResponse(contactRepository.save(contact));
    }

    @Transactional
    public void delete(Long id) {
        contactRepository.delete(findContactOrThrow(id));
    }

    private Contact findContactOrThrow(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact with id " + id + " was not found"));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " was not found"));
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getStudent().getId(),
                contact.getAddress(),
                contact.getPhone(),
                contact.getEmergencyContactName(),
                contact.getEmergencyContactPhone());
    }
}