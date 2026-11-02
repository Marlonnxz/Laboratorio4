package co.edu.uptc.students.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.uptc.students.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);
}