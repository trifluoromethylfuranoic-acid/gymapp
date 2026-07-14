package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainer;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TrainerService extends BaseUserService<Trainer, TrainerService.TrainerCreateRequest, TrainerService.TrainerUpdateRequest> {
    @Nonnull
    List<Trainer> findNotAssignedToTrainee(@Nonnull String traineeUsername);

    @Getter
    @SuperBuilder
    @Jacksonized
    class TrainerCreateRequest extends UserCreateRequest {
        @NotBlank
        String specialization;
    }

    @Getter
    @SuperBuilder
    @Jacksonized
    class TrainerUpdateRequest extends UserUpdateRequest {
        @NotBlank
        String specialization;
    }
}
