package co.edu.uptc.students.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uptc.students.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    boolean existsByCode(String code);
}