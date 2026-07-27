package com.epam.lenda.gymapp.dto.response;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;


@Builder
@Jacksonized
public record SignupResponse(
                             @Nonnull String username,
                             @Nonnull String password
) {
    @Override
    public @Nonnull String toString() {
        return "SignupResponse[username=%s]".formatted(username);
    }
}
