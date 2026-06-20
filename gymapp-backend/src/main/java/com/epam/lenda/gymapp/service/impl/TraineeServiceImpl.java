package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.trainee.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.trainee.PatchTraineeRequest;
import com.epam.lenda.gymapp.dto.trainee.SearchTraineeRequest;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.mapper.sort.TraineeSortMapper;
import com.epam.lenda.gymapp.model.Trainee2Trainer;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.TraineeService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class TraineeServiceImpl implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final TraineeSortMapper traineeSortMapper;
    private final UserMapper userMapper;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    @Override
    public @Nonnull Page<TraineeResponse> search(SearchTraineeRequest request, Pageable pageable) {
        final var sanitizedPageable = traineeSortMapper.mapPageable(pageable);
        return Page.of(traineeRepository.search(request, sanitizedPageable).map(userMapper::toResponseDto));
    }

    @Override
    public @Nonnull FullTraineeResponse getTraineeProfile(@Nonnull String username) {
        return userMapper.toFullResponseDto(traineeRepository.findByUsername(username).orElseThrow(
                ResourceNotFoundException::new));
    }

    @Override
    public @Nonnull FullTraineeResponse patchTraineeProfile(@Nonnull String username,
                                                            @Nullable PatchTraineeRequest request) {
        final var entity = traineeRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

        if (request == null) {
            return userMapper.toFullResponseDto(entity);
        }

        if (request.firstName() != null) {
            entity.getUser().setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            entity.getUser().setLastName(request.lastName());
        }
        if (request.username() != null) {
            final var userWithThatUsernameOpt = userRepository.findByUsername(request.username());
            if (userWithThatUsernameOpt.isPresent() && !userWithThatUsernameOpt.get().getId().equals(entity.getId())) {
                throw new DuplicateUsernameException("Username already exists!");
            }
            entity.getUser().setUsername(request.username());
        }
        if (request.isActive() != null) {
            entity.getUser().setIsActive(request.isActive());
        }
        if (request.address() != null) {
            entity.setAddress(request.address());
        }
        if (request.dateOfBirth() != null) {
            entity.setDateOfBirth(request.dateOfBirth());
        }

        traineeRepository.save(entity);
        return userMapper.toFullResponseDto(entity);
    }

    @Override
    public void deleteTrainee(@Nonnull String username) {
        final var entityOpt = traineeRepository.findByUsername(username);

        if (entityOpt.isEmpty()) {
            return;
        }

        final var entity = entityOpt.get();

        for (var t2t : entity.getTrainerAssignments()) {
            final var trainer = t2t.getTrainer();
            trainer.getTraineeAssignments().removeIf(t2t2 -> t2t2.getTrainee().getId().equals(entity.getId()));
        }

        for (var training : entity.getTrainings()) {
            training.setTrainee(null);
        }

        traineeRepository.delete(entity);
    }

    @Override
    public @Nonnull List<TrainerResponse> getAssignedTrainers(@Nonnull String username) {
        return traineeRepository.findByUsername(username).orElseThrow(
                ResourceNotFoundException::new).getTrainerAssignments().stream().map(
                        userMapper::toTrainerResponseDto).toList();
    }

    @Override
    public @Nonnull List<TrainerResponse> assignTrainer(@Nonnull String traineeUsername,
                                                        @Nonnull String trainerUsername) {
        final var trainee = traineeRepository.findByUsername(traineeUsername).orElseThrow(
                ResourceNotFoundException::new);
        final var trainer = trainerRepository.findByUsername(trainerUsername).orElseThrow(
                ResourceNotFoundException::new);

        if (trainee.getTrainerAssignments().stream().noneMatch(t2t -> t2t.getTrainer().getId().equals(
                trainer.getId()))) {
            final var t2t = Trainee2Trainer.builder().trainee(trainee).trainer(trainer).build();

            trainee.getTrainerAssignments().add(t2t);
            trainer.getTraineeAssignments().add(t2t);
        }

        return getAssignedTrainers(traineeUsername);
    }

    @Override
    public @Nonnull List<TrainerResponse> unassignTrainer(@Nonnull String traineeUsername,
                                                          @Nonnull String trainerUsername) {
        final var trainee = traineeRepository.findByUsername(traineeUsername).orElseThrow(
                ResourceNotFoundException::new);
        final var trainer = trainerRepository.findByUsername(trainerUsername).orElseThrow(
                ResourceNotFoundException::new);

        trainee.getTrainerAssignments().removeIf(t2t -> t2t.getTrainer().getId().equals(trainer.getId()));
        trainer.getTraineeAssignments().removeIf(t2t -> t2t.getTrainee().getId().equals(trainee.getId()));

        return getAssignedTrainers(traineeUsername);
    }
}
