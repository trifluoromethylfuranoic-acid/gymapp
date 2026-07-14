package com.epam.lenda.gymapp.service.impl;

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
    public @Nonnull Training create(@Nonnull TrainingCreateRequest request) {
        final var trainee = traineeRepository.findByUsername(request.getTrainee()).orElseThrow(
                ResourceNotFoundException::new);
        final var trainer = trainerRepository.findByUsername(request.getTrainer()).orElseThrow(
                ResourceNotFoundException::new);
        final var trainingType = trainingTypeRepository.findByNameIgnoreCase(request.getType()).orElseThrow(
                ResourceNotFoundException::new);


        var training = Training.builder().trainee(trainee).trainer(trainer).name(request.getName()).type(
                trainingType).datetime(request.getDatetime()).durationMinutes(
                        request.getDurationMinutes()).build();

        return trainingRepository.save(training);
    }

    @Override
    public @Nonnull List<Training> search(@Nonnull TrainingSearchRequest request) {
        return trainingRepository.findAll(request.toSpecification());
    }

    @Override
    protected @Nonnull ListCrudRepository<Training, UUID> getRepository() {
        return trainingRepository;
    }
}
