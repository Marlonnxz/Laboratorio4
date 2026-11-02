package edu.uptc.enrollments.repository;

import edu.uptc.enrollments.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {

    @Query("SELECT DISTINCT e FROM Enrollment e LEFT JOIN FETCH e.grades WHERE e.studentId = :studentId")
    List<Enrollment> findByStudentIdWithGrades(@Param("studentId") Long studentId);

    @Query("SELECT DISTINCT e FROM Enrollment e LEFT JOIN FETCH e.grades LEFT JOIN FETCH e.statusHistory WHERE e.id = :id")
    Optional<Enrollment> findByIdWithDetails(@Param("id") Long id);

    boolean existsByStudentIdAndCourseIdAndPeriod(Long studentId, Long courseId, String period);
}
