package com.epam.lenda.gymapp.report.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TrainerRequest(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        boolean isActive
) {
}
