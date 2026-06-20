package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.training.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.training.PatchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.TrainingResponse;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.TrainingMapper;
import com.epam.lenda.gymapp.mapper.sort.TrainingSortMapper;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.TrainingService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final TrainingSortMapper trainingSortMapper;
    private final TrainingMapper trainingMapper;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    public @Nonnull Page<TrainingResponse> search(@Nullable SearchTrainingRequest request,
                                                  @Nullable Pageable pageable) {
        final var sanitizedPageable = trainingSortMapper.mapPageable(pageable);
        return Page.of(trainingRepository.searchForTrainee(request, sanitizedPageable).map(
                trainingMapper::toResponseDto));
    }

    @Override
    public Page<TrainingResponse> searchForTrainee(@Nullable SearchTrainingRequest request, @Nullable Pageable pageable,
                                                   @Nonnull String traineeUsername) {
        final var sanitizedPageable = trainingSortMapper.mapPageable(pageable);
        return Page.of(trainingRepository.searchForTrainee(request, sanitizedPageable, traineeUsername).map(
                trainingMapper::toResponseDto));
    }

    @Override
    public @Nonnull TrainingResponse createTraining(@Nonnull CreateTrainingRequest request) {
        var trainee = traineeRepository.findByUsername(request.trainee()).orElseThrow(ResourceNotFoundException::new);
        var trainer = trainerRepository.findByUsername(request.trainer()).orElseThrow(ResourceNotFoundException::new);
        var trainingType = trainingTypeRepository.findOrCreate(request.trainingType());

        var training = Training.builder().trainee(trainee).trainer(trainer).name(request.name()).type(
                trainingType).datetime(request.datetime()).duration(request.duration()).build();

        trainingRepository.save(training);
        trainee.getTrainings().add(training);
        trainer.getTrainings().add(training);
        return trainingMapper.toResponseDto(training);
    }

    @Override
    public @Nonnull TrainingResponse getTraining(long id) {
        return trainingMapper.toResponseDto(getTrainingEntity(id));
    }

    @Nonnull
    @Override
    public TrainingResponse patchTrainingVerifyOwner(long id, @Nullable PatchTrainingRequest request,
                                                     @Nonnull String username) {
        var training = getTrainingEntity(id);

        if (training.getTrainee() != null && training.getTrainee().getUser().getUsername().equals(username)) {
            if (request == null || request.trainee() == null || username.equals(request.trainee())) {
                return patchTraining(id, request);
            }
            throw new AccessDeniedException("Unable to reassign training to another trainee");
        }

        if (training.getTrainer() != null && training.getTrainer().getUser().getUsername().equals(username)) {
            if (request == null || request.trainer() == null || username.equals(request.trainer())) {
                return patchTraining(id, request);
            }
            throw new AccessDeniedException("Unable to reassign training to another trainer");
        }

        throw new ResourceNotFoundException();
    }

    @Override
    public @Nonnull TrainingResponse patchTraining(long id, @Nullable PatchTrainingRequest request) {
        var training = getTrainingEntity(id);

        if (request == null) {
            return trainingMapper.toResponseDto(training);
        }

        if (request.trainee() != null) {
            var trainee = traineeRepository.findByUsername(request.trainee()).orElseThrow(
                    ResourceNotFoundException::new);
            if (training.getTrainee() == null) {
                training.setTrainee(trainee);
                trainee.getTrainings().add(training);
            } else if (!trainee.getId().equals(training.getTrainee().getId())) {
                training.getTrainee().getTrainings().removeIf(t -> t.getId().equals(training.getId()));
                training.setTrainee(trainee);
                trainee.getTrainings().add(training);
            }
        }

        if (request.trainer() != null) {
            var trainer = trainerRepository.findByUsername(request.trainer()).orElseThrow(
                    ResourceNotFoundException::new);
            if (training.getTrainer() == null) {
                training.setTrainer(trainer);
                trainer.getTrainings().add(training);
            } else if (!trainer.getId().equals(training.getTrainer().getId())) {
                training.getTrainer().getTrainings().removeIf(t -> t.getId().equals(training.getId()));
                training.setTrainer(trainer);
                trainer.getTrainings().add(training);
            }
        }

        if (request.name() != null) {
            training.setName(request.name());
        }
        if (request.trainingType() != null) {
            training.setType(trainingTypeRepository.findOrCreate(request.trainingType()));
        }
        if (request.datetime() != null) {
            training.setDatetime(request.datetime());
        }
        if (request.duration() != null) {
            training.setDuration(request.duration());
        }

        trainingRepository.save(training);
        return trainingMapper.toResponseDto(training);
    }

    @Override
    public void deleteTrainingVerifyOwner(long id, @Nonnull String username) {
        var trainingOpt = trainingRepository.findById(id);

        if (trainingOpt.isEmpty()) {
            return;
        }

        var training = trainingOpt.get();

        if (Objects.equals(training.getTrainee().getUser().getUsername(), username) || Objects.equals(
                training.getTrainer().getUser().getUsername(), username)) {
            deleteTraining(id);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public void deleteTraining(long id) {
        var trainingOpt = trainingRepository.findById(id);

        if (trainingOpt.isEmpty()) {
            return;
        }

        var training = trainingOpt.get();
        if (training.getTrainee() != null) {
            training.getTrainee().getTrainings().removeIf(t -> t.getId().equals(training.getId()));
        }
        if (training.getTrainer() != null) {
            training.getTrainer().getTrainings().removeIf(t -> t.getId().equals(training.getId()));
        }
        trainingRepository.delete(training);
    }

    private Training getTrainingEntity(long id) {
        return trainingRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
