package com.epam.lenda.gymapp.dto.trainer;

import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import com.epam.lenda.gymapp.validation.annotation.Username;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record PatchTrainerRequest(
                                  @NullableNotBlank @Username String username,
                                  @NullableNotBlank @FirstAndLastName String firstName,
                                  @NullableNotBlank @FirstAndLastName String lastName,
                                  @NullableNotBlank String specialization,
                                  Boolean isActive
) {
}