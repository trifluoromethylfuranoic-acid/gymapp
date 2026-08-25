package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.util.Pair;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public interface BaseUserService<T, C, U> extends BaseService<T, UUID> {
    @Nonnull
    T findByUsername(@Nonnull String username);

    @Nonnull
    Pair<T, String> create(@NotNull @Valid C createRequest);

    @Nonnull
    T update(@Nonnull String username, @NotNull @Valid U updateRequest);

    @Nonnull
    T updateActiveStatus(@Nonnull String username, boolean isActive);
}
