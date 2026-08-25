package com.epam.lenda.gymapp.dto.response;

import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FullTrainerResponse(
                                  @Nonnull String username,
                                  @Nonnull String firstName,
                                  @Nonnull String lastName,
                                  String specialization,
                                  boolean isActive,
                                  @Nonnull List<TraineeResponse> trainees
) {
}
