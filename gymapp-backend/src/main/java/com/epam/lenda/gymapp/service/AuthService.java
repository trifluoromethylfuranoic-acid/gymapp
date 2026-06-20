package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.auth.*;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public interface AuthService {
    @Nonnull
    SignupResponse signupTrainer(@Nonnull @Valid SignupTrainerRequest request);

    @Nonnull
    SignupResponse signupTrainee(@Nonnull @Valid SignupTraineeRequest request);

    @Nonnull
    LoginResponse login(@Nonnull @Valid LoginRequest request);

    void changePassword(@Nonnull String username, @Nonnull @Valid PasswordChangeRequest request);

    @Nonnull
    String generateUsername(@Nonnull String firstName, @Nonnull String lastName);

    @Nonnull
    String generatePassword();

    boolean checkCredentials(@Nonnull String username, @Nonnull String password);

    @Nonnull
    Optional<Authentication> authenticate(@Nonnull String username, @Nonnull String password);
}
