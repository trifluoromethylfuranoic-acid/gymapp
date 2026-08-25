package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.dto.request.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.request.SearchTrainingRequest;
import com.epam.lenda.gymapp.model.Training;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TrainingService extends BaseService<Training, UUID> {
    @Nonnull
    Training create(@NotNull @Valid CreateTrainingRequest request);

    @Nonnull
    List<Training> search(@Nonnull SearchTrainingRequest request);
}
