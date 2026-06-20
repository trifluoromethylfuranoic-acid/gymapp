package com.epam.lenda.gymapp.dto.trainer;

import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FullTrainerResponse(
                                  @NotBlank String username,
                                  @NotBlank String firstName,
                                  @NotBlank String lastName,
                                  @NotBlank String specialization,
                                  boolean isActive,
                                  @NotNull List<TraineeResponse> trainees
) {
}
