package com.epam.lenda.gymapp.dto.request;

import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Past;
import java.util.Date;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class CreateTraineeRequest extends CreateUserRequest {
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    Date dateOfBirth;

    @NullableNotBlank
    String address;
}
