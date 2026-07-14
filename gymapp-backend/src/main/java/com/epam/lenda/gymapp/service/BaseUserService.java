package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.util.Pair;
import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import com.epam.lenda.gymapp.validation.annotation.Password;
import com.epam.lenda.gymapp.validation.annotation.Username;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

public interface BaseUserService<T, C, U> extends BaseService<T, UUID> {
    @Nonnull
    T findByUsername(@Nonnull String username);

    @Nonnull
    Pair<T, String> create(@NotNull @Valid C createRequest);

    @Nonnull
    T update(@Nonnull String username, @NotNull @Valid U updateRequest);

    @Nonnull
    T updatePassword(@Nonnull String username, @NotNull @Valid PasswordChangeRequest request);

    @Nonnull
    T toggleActivation(@Nonnull String username);

    @Getter
    @AllArgsConstructor
    @SuperBuilder
    @Jacksonized
    class UserCreateRequest {
        @NotBlank
        @FirstAndLastName
        String firstName;

        @NotBlank
        @FirstAndLastName
        String lastName;
    }

    @Getter
    @AllArgsConstructor
    @SuperBuilder
    @Jacksonized
    class UserUpdateRequest {
        @NotBlank
        @FirstAndLastName
        String firstName;

        @NotBlank
        @FirstAndLastName
        String lastName;

        @NotBlank
        @Username
        String username;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    @Jacksonized
    class PasswordChangeRequest {
        @NotNull String oldPassword;

        @NotBlank
        @Password
        String newPassword;
    }
}
