package com.epam.lenda.gymapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class CreateTrainerRequest extends CreateUserRequest {
    @NotBlank
    String specialization;
}
