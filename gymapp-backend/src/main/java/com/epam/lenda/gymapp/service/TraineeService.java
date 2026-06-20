package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.trainee.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.trainee.PatchTraineeRequest;
import com.epam.lenda.gymapp.dto.trainee.SearchTraineeRequest;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TraineeService {
    @Nonnull
    Page<TraineeResponse> search(@Nullable SearchTraineeRequest request, @Nullable Pageable pageable);

    @Nonnull
    FullTraineeResponse getTraineeProfile(@Nonnull String username);

    @Nonnull
    FullTraineeResponse patchTraineeProfile(@Nonnull String username, @Valid @Nullable PatchTraineeRequest request);

    void deleteTrainee(@Nonnull String username);

    @Nonnull
    List<TrainerResponse> getAssignedTrainers(@Nonnull String username);

    @Nonnull
    List<TrainerResponse> assignTrainer(@Nonnull String traineeUsername, @Nonnull String trainerUsername);

    @Nonnull
    List<TrainerResponse> unassignTrainer(@Nonnull String traineeUsername, @Nonnull String trainerUsername);
}
