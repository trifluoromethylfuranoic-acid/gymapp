package com.epam.lenda.gymapp.dto.request;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record RefreshRequest(@NotNull String refreshToken) {
    @Override
    @Nonnull
    public String toString() {
        return "RefreshRequest[]";
    }
}
