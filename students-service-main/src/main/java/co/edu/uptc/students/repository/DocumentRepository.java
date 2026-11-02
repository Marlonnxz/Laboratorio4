package co.edu.uptc.students.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uptc.students.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}