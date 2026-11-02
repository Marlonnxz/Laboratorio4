package edu.uptc.enrollments.repository.specification;

import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.EnrollmentStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EnrollmentSpecification {

    private EnrollmentSpecification() {
    }

    public static Specification<Enrollment> withFilters(
            Long studentId,
            Long courseId,
            EnrollmentStatus status,
            String period
    ) {
        return (root, query, cb) -> {
            // Eager fetch grades during select queries to eliminate N+1 latency for API Gateway
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("grades", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }
            if (courseId != null) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (period != null && !period.isBlank()) {
                predicates.add(cb.equal(root.get("period"), period.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
