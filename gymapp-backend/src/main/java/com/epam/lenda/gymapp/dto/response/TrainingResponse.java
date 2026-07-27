package com.epam.lenda.gymapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nonnull;
import java.util.Date;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record TrainingResponse(
                               @Nonnull String name,
                               @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING) @Nonnull Date datetime,
                               @Nonnull String type,
                               int durationMinutes,
                               @Nonnull String trainee,
                               @Nonnull String trainer
) {
}
