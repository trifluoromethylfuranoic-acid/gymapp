package com.epam.lenda.gymapp.dto.request;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class UpdateTrainerRequest extends UpdateUserRequest {
}
