package com.epam.lenda.gymapp.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record TrainingAction(
        @NotNull TrainerRequest trainer,
        @NotNull ZonedDateTime datetime,
        @Positive @Max(24 * 60) long durationMinutes,
        @NotNull Action action
        ) {
}
