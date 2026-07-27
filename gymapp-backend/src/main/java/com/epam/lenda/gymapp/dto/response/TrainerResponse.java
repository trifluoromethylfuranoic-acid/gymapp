package com.epam.lenda.gymapp.dto.response;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;


@Builder
@Jacksonized
public record TrainerResponse(
                              @Nonnull String username,
                              @Nonnull String firstName,
                              @Nonnull String lastName,
                              String specialization,
                              boolean isActive
) {
}
