package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import jakarta.annotation.Nonnull;

public interface AccessTokenService {
    @Nonnull
    String encode(GymUserDetails userDetails);
}
