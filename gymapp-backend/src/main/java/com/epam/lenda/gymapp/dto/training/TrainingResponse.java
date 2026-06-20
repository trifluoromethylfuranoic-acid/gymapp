package com.epam.lenda.gymapp.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record TrainingResponse(
                               @NotBlank String trainee,
                               @NotBlank String trainer,
                               @NotBlank String name,
                               @NotBlank String trainingType,
                               @NotNull ZonedDateTime datetime,
                               @NotNull @Positive Duration duration
) {
}
