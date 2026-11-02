package edu.uptc.subjects.repository;

import edu.uptc.subjects.entity.SubjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectDocumentRepository extends JpaRepository<SubjectDocument, Long> {
    List<SubjectDocument> findBySubjectIdOrderByUploadedAtDesc(Long subjectId);
    Optional<SubjectDocument> findFirstBySubjectIdOrderByUploadedAtDesc(Long subjectId);
}
