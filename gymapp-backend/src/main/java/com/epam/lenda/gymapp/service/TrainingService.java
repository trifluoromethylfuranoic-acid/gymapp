package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.training.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.training.PatchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.TrainingResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

public interface TrainingService {
    Page<TrainingResponse> search(SearchTrainingRequest request, Pageable pageable);

    Page<TrainingResponse> searchForTrainee(SearchTrainingRequest request, Pageable pageable, String traineeUsername);

    TrainingResponse createTraining(@Valid CreateTrainingRequest request);

    TrainingResponse getTraining(long id);

    @Nonnull
    TrainingResponse patchTrainingVerifyOwner(long id, @Valid @Nullable PatchTrainingRequest request,
                                              @Nonnull String username);

    TrainingResponse patchTraining(long id, @Valid PatchTrainingRequest request);

    void deleteTrainingVerifyOwner(long id, @Nonnull String username);

    void deleteTraining(long id);
}
