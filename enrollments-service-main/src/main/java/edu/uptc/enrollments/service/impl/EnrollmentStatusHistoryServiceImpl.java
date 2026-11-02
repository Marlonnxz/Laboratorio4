package edu.uptc.enrollments.service.impl;

import edu.uptc.enrollments.dto.common.PagedResponse;
import edu.uptc.enrollments.dto.request.CreateStatusHistoryRequest;
import edu.uptc.enrollments.dto.response.EnrollmentStatusHistoryResponse;
import edu.uptc.enrollments.entity.Enrollment;
import edu.uptc.enrollments.entity.EnrollmentStatusHistory;
import edu.uptc.enrollments.exception.ResourceNotFoundException;
import edu.uptc.enrollments.mapper.EnrollmentStatusHistoryMapper;
import edu.uptc.enrollments.repository.EnrollmentRepository;
import edu.uptc.enrollments.repository.EnrollmentStatusHistoryRepository;
import edu.uptc.enrollments.service.EnrollmentStatusHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EnrollmentStatusHistoryServiceImpl implements EnrollmentStatusHistoryService {

    private final EnrollmentStatusHistoryRepository historyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentStatusHistoryMapper historyMapper;

    public EnrollmentStatusHistoryServiceImpl(
            EnrollmentStatusHistoryRepository historyRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentStatusHistoryMapper historyMapper
    ) {
        this.historyRepository = historyRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.historyMapper = historyMapper;
    }

    @Override
    @Transactional
    public EnrollmentStatusHistoryResponse createHistory(CreateStatusHistoryRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + request.enrollmentId() + " was not found"));

        EnrollmentStatusHistory history = new EnrollmentStatusHistory(
                enrollment,
                request.status(),
                LocalDateTime.now(),
                request.reason().trim()
        );

        enrollment.setStatus(request.status());
        enrollment.addStatusHistory(history);
        EnrollmentStatusHistory savedHistory = historyRepository.save(history);

        return historyMapper.toResponse(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentStatusHistoryResponse> getHistory(
            Long enrollmentId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String validSortBy = (sortBy == null || sortBy.isBlank()) ? "date" : sortBy;
        Sort sort = Sort.by(direction, validSortBy);

        int validPageNumber = Math.max(0, pageNumber);
        int validPageSize = pageSize > 0 ? pageSize : 10;
        Pageable pageable = PageRequest.of(validPageNumber, validPageSize, sort);

        Page<EnrollmentStatusHistory> page;
        if (enrollmentId != null) {
            page = historyRepository.findByEnrollmentId(enrollmentId, pageable);
        } else {
            page = historyRepository.findAll(pageable);
        }

        return PagedResponse.fromPage(page.map(historyMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentStatusHistoryResponse getHistoryById(Long id) {
        EnrollmentStatusHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EnrollmentStatusHistory with id " + id + " was not found"));
        return historyMapper.toResponse(history);
    }
}
