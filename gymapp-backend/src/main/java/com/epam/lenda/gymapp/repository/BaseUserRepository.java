package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;
import java.util.Optional;

public interface BaseUserRepository<T extends User> extends BaseRepository<T> {
    @Nonnull
    Optional<T> findByUsername(@Nonnull String username);
}
