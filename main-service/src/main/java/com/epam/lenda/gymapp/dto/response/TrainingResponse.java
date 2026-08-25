package com.epam.lenda.gymapp.dto.response;

import jakarta.annotation.Nonnull;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record TrainingResponse(
                               @Nonnull String name,
                               @Nonnull ZonedDateTime datetime,
                               @Nonnull String type,
                               int durationMinutes,
                               String trainee,
                               @Nonnull String trainer
) {
}
