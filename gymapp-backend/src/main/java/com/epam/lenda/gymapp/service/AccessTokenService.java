package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import jakarta.annotation.Nonnull;
import java.util.Optional;

public interface AccessTokenService {
    @Nonnull
    String encode(GymUserDetails userDetails);

    @Nonnull
    Optional<GymUserDetails> decode(@Nonnull String accessToken);
}
