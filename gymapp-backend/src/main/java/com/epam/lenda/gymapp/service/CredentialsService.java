package com.epam.lenda.gymapp.service;

import jakarta.annotation.Nonnull;
import java.util.UUID;

public interface CredentialsService {
    @Nonnull
    String generateUsername(@Nonnull BaseUserService.UserCreateRequest request);

    @Nonnull
    String generatePassword();

    @Nonnull
    String encodePassword(@Nonnull String password);

    @Nonnull
    Credentials generateCredentials(@Nonnull BaseUserService.UserCreateRequest request);

    boolean isUsernameTaken(@Nonnull String newUsername, @Nonnull UUID id);

    record Credentials(@Nonnull String username, @Nonnull String password, @Nonnull String passwordHash) {
    }
}
