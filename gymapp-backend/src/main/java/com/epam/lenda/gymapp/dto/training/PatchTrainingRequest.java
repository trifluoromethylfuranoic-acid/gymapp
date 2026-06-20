package com.epam.lenda.gymapp.dto.training;

import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import java.time.Duration;
import java.time.ZonedDateTime;

public record PatchTrainingRequest(
                                   @NullableNotBlank String trainee,
                                   @NullableNotBlank String trainer,
                                   @NullableNotBlank String name,
                                   @NullableNotBlank String trainingType,
                                   ZonedDateTime datetime,
                                   Duration duration
) {
}