package com.epam.lenda.gymapp.dto.training;

import java.time.Duration;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SearchTrainingRequest(
                                    String traineeQuery,
                                    String trainerQuery,
                                    String nameQuery,
                                    String trainingTypeQuery,
                                    ZonedDateTime datetimeMin,
                                    ZonedDateTime dateTimeMax,
                                    Duration durationMin,
                                    Duration durationMax
) {
}
