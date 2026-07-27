package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.request.CreateTraineeRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTraineeRequest;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TraineeService extends BaseUserService<Trainee, CreateTraineeRequest, UpdateTraineeRequest>, DeletableUserService {
    @Nonnull
    List<Trainer> updateTrainerList(@Nonnull String traineeUsername, @Nonnull List<String> trainerUsernames);

    @Nonnull
    List<Trainer> getTrainerList(@Nonnull String username);

    @Nonnull
    List<Trainer> findActiveTrainersNotAssignedToTrainee(@Nonnull String traineeUsername);
}
