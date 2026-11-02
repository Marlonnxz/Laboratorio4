package co.edu.uptc.students.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uptc.students.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsByStudentId(Long studentId);

    Optional<Contact> findByStudentId(Long studentId);
}