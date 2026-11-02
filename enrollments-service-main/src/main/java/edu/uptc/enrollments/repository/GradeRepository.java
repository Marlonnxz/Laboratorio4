package edu.uptc.enrollments.repository;

import edu.uptc.enrollments.entity.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByEnrollmentId(Long enrollmentId);

    Page<Grade> findByEnrollmentId(Long enrollmentId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(g.percentage), 0) FROM Grade g WHERE g.enrollment.id = :enrollmentId")
    BigDecimal sumPercentageByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

    @Query("SELECT COALESCE(SUM(g.percentage), 0) FROM Grade g WHERE g.enrollment.id = :enrollmentId AND g.id <> :excludeGradeId")
    BigDecimal sumPercentageByEnrollmentIdExcludingGrade(@Param("enrollmentId") Long enrollmentId, @Param("excludeGradeId") Long excludeGradeId);
}
