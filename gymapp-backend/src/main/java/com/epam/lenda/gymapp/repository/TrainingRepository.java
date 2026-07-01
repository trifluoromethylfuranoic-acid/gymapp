package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Training;
import jakarta.annotation.Nonnull;
import java.util.List;


public interface TrainingRepository extends BaseRepository<Training> {
    void removeTraineeRefs(long traineeId);

    @Nonnull
    List<Training> findByTraineeUsername(@Nonnull String username);

    @Nonnull
    List<Training> findByTrainerUsername(@Nonnull String username);
}
