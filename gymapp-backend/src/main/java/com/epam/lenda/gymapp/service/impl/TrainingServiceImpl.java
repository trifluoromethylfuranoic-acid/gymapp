package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.service.TrainingService;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
public class TrainingServiceImpl extends BaseServiceImpl<Training> implements TrainingService {
    private final TrainingRepository trainingRepository;

    public TrainingServiceImpl(TrainingRepository trainingRepository) {
        super(trainingRepository);
        this.trainingRepository = trainingRepository;
    }

    @Override
    public @Nonnull Training create(@Nonnull Trainee trainee,
                                    @Nonnull Trainer trainer,
                                    @Nonnull String name,
                                    @Nonnull TrainingType type,
                                    @Nonnull ZonedDateTime datetime,
                                    @Nonnull Duration duration) {
        var training = Training.builder().trainee(trainee).trainer(trainer).name(name).type(
                type).datetime(datetime).duration(duration).build();

        trainingRepository.save(training);
        return training;
    }

    @Override
    public @NonNull List<Training> findByTraineeUsername(@NonNull String username) {
        return trainingRepository.findByTraineeUsername(username);
    }

    @Override
    public @NonNull List<Training> findByTrainerUsername(@NonNull String username) {
        return trainingRepository.findByTrainerUsername(username);
    }

}
