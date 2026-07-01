package com.epam.lenda.gymapp.repository;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface BaseRepository<T> {
    @Nonnull
    List<T> findAll();

    @Nonnull
    Optional<T> findById(long id);

    @Nonnull
    T save(@Nonnull T entity);

    void delete(T entity);

    default void deleteById(long id) {
        findById(id).ifPresent(this::delete);
    }
}
