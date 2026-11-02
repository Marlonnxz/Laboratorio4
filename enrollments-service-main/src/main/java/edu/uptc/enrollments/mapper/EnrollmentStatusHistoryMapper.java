package edu.uptc.enrollments.mapper;

import edu.uptc.enrollments.dto.response.EnrollmentStatusHistoryResponse;
import edu.uptc.enrollments.entity.EnrollmentStatusHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnrollmentStatusHistoryMapper {

    public EnrollmentStatusHistoryResponse toResponse(EnrollmentStatusHistory history) {
        if (history == null) {
            return null;
        }
        return new EnrollmentStatusHistoryResponse(
                history.getId(),
                history.getEnrollment() != null ? history.getEnrollment().getId() : null,
                history.getStatus(),
                history.getDate(),
                history.getReason()
        );
    }

    public List<EnrollmentStatusHistoryResponse> toResponseList(List<EnrollmentStatusHistory> histories) {
        if (histories == null) {
            return List.of();
        }
        return histories.stream()
                .map(this::toResponse)
                .toList();
    }
}
