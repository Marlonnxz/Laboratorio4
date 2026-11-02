package edu.uptc.subjects.service;

import edu.uptc.subjects.dto.PageResponse;
import edu.uptc.subjects.dto.SubjectDocumentResponse;
import edu.uptc.subjects.dto.SubjectRequest;
import edu.uptc.subjects.dto.SubjectResponse;
import edu.uptc.subjects.entity.Subject;
import edu.uptc.subjects.entity.SubjectDocument;
import edu.uptc.subjects.exception.BadRequestException;
import edu.uptc.subjects.exception.ConflictException;
import edu.uptc.subjects.exception.ResourceNotFoundException;
import edu.uptc.subjects.mapper.SubjectMapper;
import edu.uptc.subjects.repository.CourseRepository;
import edu.uptc.subjects.repository.SubjectDocumentRepository;
import edu.uptc.subjects.repository.SubjectRepository;
import edu.uptc.subjects.repository.SubjectSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository repository;
    private final SubjectMapper mapper;
    private final CourseRepository courseRepository;
    private final SubjectDocumentRepository documentRepository;
    private final AzureBlobStorageService blobStorageService;

    public SubjectService(SubjectRepository repository,
                          SubjectMapper mapper,
                          CourseRepository courseRepository,
                          SubjectDocumentRepository documentRepository,
                          AzureBlobStorageService blobStorageService) {
        this.repository = repository;
        this.mapper = mapper;
        this.courseRepository = courseRepository;
        this.documentRepository = documentRepository;
        this.blobStorageService = blobStorageService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SubjectResponse> findAll(PageRequest pageRequest, String name, String program, Integer credits) {
        Specification<Subject> spec = SubjectSpecifications.withFilters(name, program, credits);
        Page<Subject> page = repository.findAll(spec, pageRequest);
        List<SubjectResponse> content = page.getContent().stream()
                .map(this::mapSubjectWithDocument)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        Subject subject = findByIdOrThrow(id);
        return mapSubjectWithDocument(subject);
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        Subject subject = mapper.toEntity(request);
        return mapper.toResponse(repository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = findByIdOrThrow(id);
        subject.setName(request.name());
        subject.setCredits(request.credits());
        subject.setProgram(request.program());
        return mapSubjectWithDocument(subject);
    }

    @Transactional
    public void delete(Long id) {
        findByIdOrThrow(id);
        if (courseRepository.existsBySubjectId(id)) {
            throw new ConflictException("Subject with id " + id + " has associated courses and cannot be deleted");
        }
        // Delete all documents from blob storage
        List<SubjectDocument> docs = documentRepository.findBySubjectIdOrderByUploadedAtDesc(id);
        for (SubjectDocument doc : docs) {
            blobStorageService.deleteFile(doc.getBlobName());
        }
        documentRepository.deleteAll(docs);
        repository.deleteById(id);
    }

    @Transactional
    public SubjectDocumentResponse uploadPdf(Long subjectId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No PDF file provided");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("PDF file size exceeds maximum limit of 10 MB");
        }
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("application/pdf") && (filename == null || !filename.toLowerCase().endsWith(".pdf")))) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        Subject subject = findByIdOrThrow(subjectId);

        try {
            String blobName = blobStorageService.uploadFile(
                    "subjects/syllabi",
                    filename != null ? filename : "syllabus.pdf",
                    "application/pdf",
                    file.getBytes()
            );

            SubjectDocument doc = new SubjectDocument(
                    subject,
                    blobName,
                    filename != null ? filename : "syllabus.pdf",
                    "application/pdf",
                    file.getSize()
            );

            doc = documentRepository.save(doc);

            String sasUrl = blobStorageService.generateReadSasUrl(blobName);

            return new SubjectDocumentResponse(
                    doc.getId(),
                    subjectId,
                    doc.getOriginalName(),
                    doc.getContentType(),
                    doc.getFileSize(),
                    doc.getUploadedAt(),
                    sasUrl
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF bytes: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SubjectDocumentResponse> listDocuments(Long subjectId) {
        findByIdOrThrow(subjectId);
        return documentRepository.findBySubjectIdOrderByUploadedAtDesc(subjectId).stream()
                .map(doc -> new SubjectDocumentResponse(
                        doc.getId(),
                        subjectId,
                        doc.getOriginalName(),
                        doc.getContentType(),
                        doc.getFileSize(),
                        doc.getUploadedAt(),
                        blobStorageService.generateReadSasUrl(doc.getBlobName())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public String getLatestPdfSasUrl(Long subjectId) {
        findByIdOrThrow(subjectId);
        SubjectDocument doc = documentRepository.findFirstBySubjectIdOrderByUploadedAtDesc(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("No PDF syllabus found for subject " + subjectId));
        return blobStorageService.generateReadSasUrl(doc.getBlobName());
    }

    @Transactional
    public void deleteDocument(Long subjectId, Long documentId) {
        findByIdOrThrow(subjectId);
        SubjectDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document with id " + documentId + " not found"));
        if (!doc.getSubject().getId().equals(subjectId)) {
            throw new BadRequestException("Document does not belong to subject " + subjectId);
        }
        blobStorageService.deleteFile(doc.getBlobName());
        documentRepository.delete(doc);
    }

    private Subject findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with id " + id + " was not found"));
    }

    private SubjectResponse mapSubjectWithDocument(Subject subject) {
        Optional<SubjectDocument> latestDoc = documentRepository.findFirstBySubjectIdOrderByUploadedAtDesc(subject.getId());
        String pdfUrl = latestDoc.map(doc -> blobStorageService.generateReadSasUrl(doc.getBlobName())).orElse(null);
        String pdfOriginalName = latestDoc.map(SubjectDocument::getOriginalName).orElse(null);
        return mapper.toResponse(subject, pdfUrl, pdfOriginalName);
    }
}
