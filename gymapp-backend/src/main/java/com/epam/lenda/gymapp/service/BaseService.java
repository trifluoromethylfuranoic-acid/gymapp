package com.epam.lenda.gymapp.service;

import jakarta.annotation.Nonnull;
import java.util.List;

public interface BaseService<T> {
    @Nonnull
    List<T> findAll();

    @Nonnull
    T findById(long id);
}
