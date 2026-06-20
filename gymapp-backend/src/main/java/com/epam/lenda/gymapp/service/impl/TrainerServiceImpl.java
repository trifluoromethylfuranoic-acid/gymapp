package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.trainer.PatchTrainerRequest;
import com.epam.lenda.gymapp.dto.trainer.SearchTrainerRequest;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.mapper.sort.TrainerSortMapper;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.TraineeService;
import com.epam.lenda.gymapp.service.TrainerService;
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
public class TrainerServiceImpl implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainerSortMapper trainerSortMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeService traineeService;

    @Override
    public @Nonnull Page<TrainerResponse> search(@Nullable SearchTrainerRequest request, @Nullable Pageable pageable) {
        final var sanitizedPageable = trainerSortMapper.mapPageable(pageable);
        return Page.of(trainerRepository.search(request, sanitizedPageable).map(userMapper::toResponseDto));
    }

    @Nonnull
    @Override
    public TrainerResponse getTrainerProfile(@Nonnull String username) {
        return userMapper.toResponseDto(trainerRepository.findByUsername(username).orElseThrow(
                ResourceNotFoundException::new));
    }

    @Override
    public @Nonnull FullTrainerResponse getFullTrainerProfile(@Nonnull String username) {
        return userMapper.toFullResponseDto(trainerRepository.findByUsername(username).orElseThrow(
                ResourceNotFoundException::new));
    }

    @Override
    public @Nonnull FullTrainerResponse patchTrainerProfile(@Nonnull String username,
                                                            @Nullable PatchTrainerRequest request) {
        final var entity = trainerRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

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
        if (request.specialization() != null) {
            var specialization = trainingTypeRepository.findOrCreate(request.specialization());
            entity.setSpecialization(specialization);
        }

        trainerRepository.save(entity);
        return userMapper.toFullResponseDto(entity);
    }

    @Override
    public void deleteTrainer(@Nonnull String username) {
        final var entityOpt = trainerRepository.findByUsername(username);

        if (entityOpt.isEmpty()) {
            return;
        }

        final var entity = entityOpt.get();

        for (var t2t : entity.getTraineeAssignments()) {
            final var trainee = t2t.getTrainee();
            trainee.getTrainerAssignments().removeIf(t2t2 -> t2t2.getTrainer().getId().equals(entity.getId()));
        }

        for (var training : entity.getTrainings()) {
            training.setTrainer(null);
        }

        trainerRepository.delete(entity);
    }

    @Override
    public @Nonnull List<TraineeResponse> getAssignedTrainees(@Nonnull String username) {
        return trainerRepository.findByUsername(username).orElseThrow(
                ResourceNotFoundException::new).getTraineeAssignments().stream().map(
                        userMapper::toTraineeResponseDto).toList();
    }

    @Override
    public @Nonnull List<TraineeResponse> assignTrainee(@Nonnull String trainerUsername,
                                                        @Nonnull String traineeUsername) {
        traineeService.assignTrainer(traineeUsername, trainerUsername);
        return getAssignedTrainees(trainerUsername);
    }

    @Override
    public @Nonnull List<TraineeResponse> unassignTrainee(@Nonnull String trainerUsername,
                                                          @Nonnull String traineeUsername) {
        traineeService.unassignTrainer(traineeUsername, trainerUsername);
        return getAssignedTrainees(trainerUsername);
    }
}
