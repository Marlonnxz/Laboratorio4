package edu.uptc.enrollments.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public static <T> PagedResponse<T> unpaged(List<T> list) {
        return new PagedResponse<>(
                list,
                0,
                list.size(),
                list.size(),
                list.isEmpty() ? 0 : 1
        );
    }
}
