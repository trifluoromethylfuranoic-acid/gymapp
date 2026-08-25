package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.event.TrainingActionEvent;
import com.epam.lenda.gymapp.dto.request.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.request.SearchTrainingRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.TrainingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingServiceImpl extends BaseServiceImpl<Training, UUID> implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final Counter trainingsCreatedCounter;
    private final TrainingReportNotifier trainingReportNotifier;

    public TrainingServiceImpl(TrainingRepository trainingRepository, TraineeRepository traineeRepository,
                               TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository,
                               MeterRegistry meterRegistry, TrainingReportNotifier trainingReportNotifier) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingsCreatedCounter = meterRegistry.counter("trainings.created");
        this.trainingReportNotifier = trainingReportNotifier;
    }

    @Override
    @Transactional
    public @Nonnull Training create(@Nonnull CreateTrainingRequest request) {
        final var trainee = traineeRepository.findByUsername(request.getTrainee()).orElseThrow(
                () -> new ResourceNotFoundException("trainee", request.getTrainee()));
        final var trainer = trainerRepository.findByUsername(request.getTrainer()).orElseThrow(
                () -> new ResourceNotFoundException("trainer", request.getTrainer()));
        final var trainingTypeName = request.getType().trim();
        final var trainingType = trainingTypeRepository.findByNameIgnoreCase(trainingTypeName).orElseThrow(
                () -> new ResourceNotFoundException("training type", trainingTypeName));

        var training = Training.builder().trainee(trainee).trainer(trainer).name(request.getName()).type(
                trainingType).datetime(request.getDatetime()).durationMinutes(
                        request.getDurationMinutes()).build();

        trainingsCreatedCounter.increment();

        final var saved = trainingRepository.save(training);
        trainingReportNotifier.notify(toEvent(saved, TrainingActionEvent.Action.CREATE));

        return saved;
    }

    static @Nonnull TrainingActionEvent toEvent(@Nonnull Training training,
                                                @Nonnull TrainingActionEvent.Action action) {
        final var trainerUser = training.getTrainer().getUser();
        return TrainingActionEvent
                .builder()
                .trainer(TrainingActionEvent.TrainerInfo
                        .builder()
                        .username(trainerUser.getUsername())
                        .firstName(trainerUser.getFirstName())
                        .lastName(trainerUser.getLastName())
                        .isActive(trainerUser.getIsActive())
                        .build())
                .datetime(training.getDatetime())
                .durationMinutes(training.getDurationMinutes())
                .action(action)
                .build();
    }

    @Override
    public @Nonnull List<Training> search(@Nonnull SearchTrainingRequest request) {
        return trainingRepository.findAll(request.toSpecification());
    }

    @Override
    protected @Nonnull ListCrudRepository<Training, UUID> getRepository() {
        return trainingRepository;
    }

    @Override
    protected @NonNull String getResourceName() {
        return "training";
    }
}
