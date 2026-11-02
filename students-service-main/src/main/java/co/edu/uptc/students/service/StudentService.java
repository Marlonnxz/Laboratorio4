package co.edu.uptc.students.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.dto.StudentRequest;
import co.edu.uptc.students.dto.StudentResponse;
import co.edu.uptc.students.entity.Program;
import co.edu.uptc.students.entity.Student;
import co.edu.uptc.students.exception.BadRequestException;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.ProgramRepository;
import co.edu.uptc.students.repository.StudentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ProgramRepository programRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    public StudentService(StudentRepository studentRepository,
                          ProgramRepository programRepository,
                          AzureBlobStorageService azureBlobStorageService) {
        this.studentRepository = studentRepository;
        this.programRepository = programRepository;
        this.azureBlobStorageService = azureBlobStorageService;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getAll(String firstName, String lastName, Long programId, String email,
            int pageNumber, int pageSize, String sortBy, String sortDirection) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortDirection));
        Specification<Student> specification = buildSpecification(firstName, lastName, programId, email);

        Page<Student> page = studentRepository.findAll(specification, pageable);

        List<StudentResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(findStudentOrThrow(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Student with email " + request.email() + " already exists");
        }

        Program program = findProgramOrThrow(request.programId());
        Student student = new Student(null, request.firstName(), request.lastName(), request.email(), program);

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudentOrThrow(id);

        studentRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Student with email " + request.email() + " already exists");
            }
        });

        Program program = findProgramOrThrow(request.programId());

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setEmail(request.email());
        student.setProgram(program);

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        studentRepository.delete(findStudentOrThrow(id));
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " was not found"));
    }

    private Program findProgramOrThrow(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program with id " + programId + " was not found"));
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        String property = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private Specification<Student> buildSpecification(String firstName, String lastName, Long programId,
            String email) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (firstName != null && !firstName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (programId != null) {
                predicates.add(cb.equal(root.get("program").get("id"), programId));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public StudentResponse uploadPhoto(Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No image file provided");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BadRequestException("Image file size exceeds maximum limit of 2 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/png") && !contentType.equalsIgnoreCase("image/jpg"))) {
            throw new BadRequestException("Only JPG and PNG images are allowed");
        }

        Student student = findStudentOrThrow(id);

        if (student.getPhotoBlobName() != null) {
            azureBlobStorageService.deleteFile(student.getPhotoBlobName());
        }

        try {
            String blobName = azureBlobStorageService.uploadFile(
                    "students/photos",
                    file.getOriginalFilename(),
                    contentType,
                    file.getBytes()
            );

            student.setPhotoBlobName(blobName);
            student.setPhotoOriginalName(file.getOriginalFilename());
            student.setPhotoContentType(contentType);
            student.setPhotoSize(file.getSize());
            student.setPhotoUploadedAt(java.time.LocalDateTime.now());

            return toResponse(studentRepository.save(student));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read image bytes: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deletePhoto(Long id) {
        Student student = findStudentOrThrow(id);
        if (student.getPhotoBlobName() != null) {
            azureBlobStorageService.deleteFile(student.getPhotoBlobName());
            student.setPhotoBlobName(null);
            student.setPhotoOriginalName(null);
            student.setPhotoContentType(null);
            student.setPhotoSize(null);
            student.setPhotoUploadedAt(null);
            studentRepository.save(student);
        }
    }

    @Transactional(readOnly = true)
    public String getPhotoSasUrl(Long id) {
        Student student = findStudentOrThrow(id);
        if (student.getPhotoBlobName() == null) {
            throw new ResourceNotFoundException("Student with id " + id + " does not have a photo");
        }
        return azureBlobStorageService.generateReadSasUrl(student.getPhotoBlobName());
    }

    private StudentResponse toResponse(Student student) {
        String photoUrl = azureBlobStorageService.generateReadSasUrl(student.getPhotoBlobName());
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getProgram().getId(),
                photoUrl,
                student.getPhotoOriginalName());
    }
}