package com.epam.lenda.gymapp.dto.auth;

import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SignupTrainerRequest(
                                   @NotBlank @FirstAndLastName String firstName,
                                   @NotBlank @FirstAndLastName String lastName,
                                   @NotBlank String specialization
) {
}
