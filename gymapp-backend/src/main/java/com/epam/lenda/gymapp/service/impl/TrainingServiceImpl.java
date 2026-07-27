package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.request.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.request.SearchTrainingRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.TrainingService;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl extends BaseServiceImpl<Training, UUID> implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

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

        return trainingRepository.save(training);
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
