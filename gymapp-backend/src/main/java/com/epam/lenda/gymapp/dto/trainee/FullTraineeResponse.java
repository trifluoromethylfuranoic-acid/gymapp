package com.epam.lenda.gymapp.dto.trainee;

import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FullTraineeResponse(
                                  @NotBlank String username,
                                  @NotBlank String firstName,
                                  @NotBlank String lastName,
                                  @Past LocalDate dateOfBirth,
                                  String address,
                                  boolean isActive,
                                  @NotNull List<TrainerResponse> trainers
) {
}
