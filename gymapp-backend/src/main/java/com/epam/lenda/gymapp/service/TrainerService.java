package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.trainer.PatchTrainerRequest;
import com.epam.lenda.gymapp.dto.trainer.SearchTrainerRequest;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TrainerService {
    @Nonnull
    Page<TrainerResponse> search(@Nullable SearchTrainerRequest request, @Nullable Pageable pageable);

    @Nonnull
    TrainerResponse getTrainerProfile(@Nonnull String username);

    @Nonnull
    FullTrainerResponse getFullTrainerProfile(@Nonnull String username);

    @Nonnull
    FullTrainerResponse patchTrainerProfile(@Nonnull String username, @Valid @Nullable PatchTrainerRequest request);

    void deleteTrainer(@Nonnull String username);

    @Nonnull
    List<TraineeResponse> getAssignedTrainees(@Nonnull String username);

    @Nonnull
    List<TraineeResponse> assignTrainee(@Nonnull String trainerUsername, @Nonnull String traineeUsername);

    @Nonnull
    List<TraineeResponse> unassignTrainee(@Nonnull String trainerUsername, @Nonnull String traineeUsername);
}
