package com.epam.lenda.gymapp.dto;

import java.util.List;

public record Page<T>(long totalPages, long totalElements, long currentPage, List<T> elements) {
    public static <T> Page<T> of(org.springframework.data.domain.Page<T> page) {
        return new Page<>(page.getTotalPages(), page.getTotalElements(), page.getNumber(), page.getContent());
    }
}
