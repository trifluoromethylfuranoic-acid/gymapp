package com.epam.lenda.gymapp.dto.trainee;

import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import com.epam.lenda.gymapp.validation.annotation.Username;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record PatchTraineeRequest(
                                  @NullableNotBlank @Username String username,
                                  @NullableNotBlank @FirstAndLastName String firstName,
                                  @NullableNotBlank @FirstAndLastName String lastName,
                                  @Past LocalDate dateOfBirth,
                                  @NullableNotBlank String address,
                                  Boolean isActive
) {
}
