package com.epam.lenda.gymapp.dto.auth;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record LoginResponse(
                            @NotBlank String accessToken
) {
    @Override
    @Nonnull
    public String toString() {
        return "LoginResponse[]";
    }
}
