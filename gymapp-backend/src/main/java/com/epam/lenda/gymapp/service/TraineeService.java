package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Past;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TraineeService extends BaseUserService<Trainee, TraineeService.TraineeCreateRequest, TraineeService.TraineeUpdateRequest>, DeletableUserService {
    @Nonnull
    List<Trainer> updateTrainerList(@Nonnull String traineeUsername, @Nonnull List<String> trainerUsernames);

    @Getter
    @SuperBuilder
    @Jacksonized
    class TraineeCreateRequest extends UserCreateRequest {
        @Past
        Date dateOfBirth;

        @NullableNotBlank
        String address;
    }

    @Getter
    @SuperBuilder
    @Jacksonized
    class TraineeUpdateRequest extends UserUpdateRequest {
        @Past
        Date dateOfBirth;

        @NullableNotBlank
        String address;
    }
}
