package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TrainingService extends BaseService<Training> {
    @Nonnull
    Training create(@NotNull @Valid Trainee trainee,
                    @NotNull @Valid Trainer trainer,
                    @NotBlank @Valid String name,
                    @NotNull @Valid TrainingType type,
                    @NotNull @Valid ZonedDateTime datetime,
                    @NotNull @Valid Duration duration);

    @Nonnull
    List<Training> findByTraineeUsername(@Nonnull String username);

    @Nonnull
    List<Training> findByTrainerUsername(@Nonnull String username);
}
