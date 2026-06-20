package com.epam.lenda.gymapp.dto.auth;

import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SignupTraineeRequest(
                                   @NotBlank @FirstAndLastName String firstName,
                                   @NotBlank @FirstAndLastName String lastName,
                                   @Past LocalDate dateOfBirth,
                                   String address
) {
}
