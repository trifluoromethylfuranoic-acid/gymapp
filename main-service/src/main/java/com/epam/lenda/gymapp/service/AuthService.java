package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.dto.request.AuthenticationRequest;
import com.epam.lenda.gymapp.dto.request.ChangePasswordRequest;
import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AuthService {
    Optional<GymUserDetails> authenticate(AuthenticationRequest authenticationRequest);

    GymUserDetails requireAuthentication(AuthenticationRequest authenticationRequest);

    @Nonnull
    User updatePassword(@Nonnull String username, @NotNull @Valid ChangePasswordRequest request);
}
