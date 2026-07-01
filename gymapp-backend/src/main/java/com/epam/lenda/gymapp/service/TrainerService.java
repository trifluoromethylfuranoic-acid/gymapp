package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.util.Pair;
import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import com.epam.lenda.gymapp.validation.annotation.Password;
import com.epam.lenda.gymapp.validation.annotation.Username;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TrainerService extends UpdatableUserService<Trainer, TrainerService.UpdateRequest> {
    /**
     * Creates and saves new trainer with generated credentials
     *
     * @return pair of created trainer and their password
     */
    @Nonnull
    Pair<Trainer, String> create(@NotNull @Valid @FirstAndLastName String firstName,
                                 @NotNull @Valid @FirstAndLastName String lastname,
                                 @NotNull TrainingType specialization);

    @Builder
    record UpdateRequest(
                         @NotNull @Username String username,
                         @NotNull @Password String password,
                         boolean isActive,
                         @NotNull @FirstAndLastName String firstName,
                         @NotNull @FirstAndLastName String lastName,
                         @NotNull TrainingType specialization
    ) {
    }
}
