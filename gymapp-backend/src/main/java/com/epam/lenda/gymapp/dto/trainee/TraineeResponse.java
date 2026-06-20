package com.epam.lenda.gymapp.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record TraineeResponse(
                              @NotBlank String username,
                              @NotBlank String firstName,
                              @NotBlank String lastName,
                              @Past LocalDate dateOfBirth,
                              String address,
                              boolean isActive
) {
}
