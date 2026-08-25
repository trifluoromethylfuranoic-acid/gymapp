package com.epam.lenda.gymapp.dto.response;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
    @Override
    public @Nonnull String toString() {
        return "LoginResponse[]";
    }
}
