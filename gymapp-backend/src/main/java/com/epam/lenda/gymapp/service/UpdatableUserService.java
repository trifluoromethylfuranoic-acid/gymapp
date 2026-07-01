package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UpdatableUserService<T extends User, R> extends BaseUserService<T> {
    @Nonnull
    T update(@Nonnull String username, @NotNull @Valid R updateRequest);
}
