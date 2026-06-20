package com.epam.lenda.gymapp.mapper.sort;

import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SortMapper {
    Optional<String> mapPropertyName(String externalName);

    default Sort mapSort(Sort externalSort) {
        if (externalSort == null || !externalSort.isSorted()) {
            return Sort.unsorted();
        }

        final var order = externalSort.iterator().next();
        final var internalNameOpt = mapPropertyName(order.getProperty());

        return internalNameOpt.map(s -> Sort.by(order.getDirection(), s)).orElseGet(Sort::unsorted);
    }

    default Pageable mapPageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return Pageable.unpaged();
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapSort(pageable.getSort()));
    }
}
