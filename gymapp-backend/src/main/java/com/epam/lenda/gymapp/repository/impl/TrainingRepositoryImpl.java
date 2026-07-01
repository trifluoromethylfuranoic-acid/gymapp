package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingRepositoryImpl extends BaseRepositoryImpl<Training> implements TrainingRepository {

    public TrainingRepositoryImpl(MapBasedStorage<Training> trainingsData) {
        super(trainingsData);
    }

    @Override
    protected void checkEntity(@Nonnull Training entity) {
        if (entity.getName() == null || entity.getType() == null || entity.getDatetime() == null || entity.getDuration() == null) {
            throw new IllegalStateException("Non nullable field is null in Training entity");
        }
    }

    @Override
    public void removeTraineeRefs(long traineeId) {
        getStorage().getData().values().stream().filter(
                training -> training.getTrainee().getId().equals(traineeId)).forEach(
                        training -> training.setTrainee(null));
    }

    @Override
    public @NonNull List<Training> findByTraineeUsername(@NonNull String username) {
        return getStorage().getData().values().stream().filter(
                training -> training.getTrainee().getUsername().equals(username)).toList();
    }

    @Override
    public @NonNull List<Training> findByTrainerUsername(@NonNull String username) {
        return getStorage().getData().values().stream().filter(
                training -> training.getTrainer().getUsername().equals(username)).toList();
    }
}
