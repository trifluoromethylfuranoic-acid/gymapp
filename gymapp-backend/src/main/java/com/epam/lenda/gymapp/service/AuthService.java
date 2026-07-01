package com.epam.lenda.gymapp.service;

import jakarta.annotation.Nonnull;

public interface AuthService {
    @Nonnull
    String generateUsername(@Nonnull String firstName, @Nonnull String lastName);

    @Nonnull
    String generatePassword();

    @Nonnull
    Credentials generateCredentials(@Nonnull String firstName, @Nonnull String lastName);

    record Credentials(@Nonnull String username, @Nonnull String password, @Nonnull String passwordHash) {
    }
}
