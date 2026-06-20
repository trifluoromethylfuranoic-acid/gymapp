package com.epam.lenda.gymapp.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record TrainerResponse(
                              @NotBlank String username,
                              @NotBlank String firstName,
                              @NotBlank String lastName,
                              @NotBlank String specialization,
                              boolean isActive
) {
}
