package com.epam.lenda.gymapp.util;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationUtil {
    private SpecificationUtil() {
    }

    public static <E, T extends Comparable<? super T>> Specification<E> fieldBetweenInclusive(String field,
                                                                                              T start,
                                                                                              T end) {
        final Specification<E> lowerBound = fieldGreaterThanOrEqualTo(field, start);
        final Specification<E> upperBound = fieldLessThanOrEqualTo(field, end);
        return lowerBound.and(upperBound);
    }

    public static <E, T extends Comparable<? super T>> Specification<E> fieldGreaterThanOrEqualTo(String field,
                                                                                                  T start) {
        return (root, query, cb) -> {
            if (start == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get(field), start);
        };
    }

    public static <E, T extends Comparable<? super T>> Specification<E> fieldLessThanOrEqualTo(String field, T end) {
        return (root, query, cb) -> {
            if (end == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get(field), end);
        };
    }
}
