package com.epam.lenda.gymapp.dto.request;

import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@SuperBuilder
@Jacksonized
public class UpdateUserRequest {
    @NotBlank
    @FirstAndLastName
    String firstName;

    @NotBlank
    @FirstAndLastName
    String lastName;

    @NotNull Boolean isActive;
}
