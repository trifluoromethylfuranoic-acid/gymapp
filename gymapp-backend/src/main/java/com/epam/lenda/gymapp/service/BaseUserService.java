package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;

public interface BaseUserService<T extends User> extends BaseService<T> {
    @Nonnull
    T findByUsername(@Nonnull String username);
}
