package edu.uptc.enrollments.repository;

import edu.uptc.enrollments.entity.EnrollmentStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentStatusHistoryRepository extends JpaRepository<EnrollmentStatusHistory, Long> {

    List<EnrollmentStatusHistory> findByEnrollmentIdOrderByDateDesc(Long enrollmentId);

    Page<EnrollmentStatusHistory> findByEnrollmentId(Long enrollmentId, Pageable pageable);
}
