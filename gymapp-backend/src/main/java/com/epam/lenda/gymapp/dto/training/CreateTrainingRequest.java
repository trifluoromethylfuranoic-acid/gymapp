package com.epam.lenda.gymapp.dto.training;

import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record CreateTrainingRequest(
                                    @NullableNotBlank String trainee,
                                    @NullableNotBlank String trainer,
                                    @NotBlank String name,
                                    @NotBlank String trainingType,
                                    @NotNull ZonedDateTime datetime,
                                    @NotNull Duration duration
) {
}