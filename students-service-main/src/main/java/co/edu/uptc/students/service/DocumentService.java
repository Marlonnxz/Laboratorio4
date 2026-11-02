package co.edu.uptc.students.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.students.dto.DocumentRequest;
import co.edu.uptc.students.dto.DocumentResponse;
import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.entity.Document;
import co.edu.uptc.students.entity.Student;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.DocumentRepository;
import co.edu.uptc.students.repository.StudentRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;

    public DocumentService(DocumentRepository documentRepository, StudentRepository studentRepository) {
        this.documentRepository = documentRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> getAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Document> page = documentRepository.findAll(pageable);

        List<DocumentResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(Long id) {
        return toResponse(findDocumentOrThrow(id));
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request) {
        Student student = findStudentOrThrow(request.studentId());
        Document document = new Document(null, request.type(), request.number(), request.issueDate(), student);

        return toResponse(documentRepository.save(document));
    }

    @Transactional
    public DocumentResponse update(Long id, DocumentRequest request) {
        Document document = findDocumentOrThrow(id);
        Student student = findStudentOrThrow(request.studentId());

        document.setType(request.type());
        document.setNumber(request.number());
        document.setIssueDate(request.issueDate());
        document.setStudent(student);

        return toResponse(documentRepository.save(document));
    }

    @Transactional
    public void delete(Long id) {
        documentRepository.delete(findDocumentOrThrow(id));
    }

    private Document findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document with id " + id + " was not found"));
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " was not found"));
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getStudent().getId(),
                document.getType(),
                document.getNumber(),
                document.getIssueDate());
    }
}