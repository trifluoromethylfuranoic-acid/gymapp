package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.request.CreateTrainerRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTrainerRequest;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TrainerService extends BaseUserService<Trainer, CreateTrainerRequest, UpdateTrainerRequest> {
    @Nonnull
    List<Trainee> getTraineeList(@Nonnull String username);
}
