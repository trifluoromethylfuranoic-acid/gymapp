package com.epam.lenda.gymapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Builder
@Jacksonized
public class CreateTrainingRequest {
    @NotBlank
    String trainee;

    @NotBlank
    String trainer;

    @NotBlank
    String name;

    @NotBlank
    String type;

    @NotNull ZonedDateTime datetime;

    @Positive @Max(24 * 60)
    int durationMinutes;
}
