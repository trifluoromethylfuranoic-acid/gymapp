package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.request.CreateTraineeRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTraineeRequest;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingAssignment;
import com.epam.lenda.gymapp.repository.*;
import com.epam.lenda.gymapp.service.CredentialsService;
import com.epam.lenda.gymapp.service.TraineeService;
import com.epam.lenda.gymapp.util.Pair;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TraineeServiceImpl extends BaseUserServiceImpl<Trainee, CreateTraineeRequest, UpdateTraineeRequest> implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingAssignmentRepository trainingAssignmentRepository;
    private final CredentialsService credentialsService;
    private final TrainingRepository trainingRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TraineeServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository,
                              TrainingAssignmentRepository trainingAssignmentRepository,
                              CredentialsService credentialsService, TrainingRepository trainingRepository,
                              MeterRegistry meterRegistry, RefreshTokenRepository refreshTokenRepository) {
        super(meterRegistry);
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingAssignmentRepository = trainingAssignmentRepository;
        this.credentialsService = credentialsService;
        this.trainingRepository = trainingRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public @Nonnull Pair<Trainee, String> create(@Nonnull CreateTraineeRequest request) {
        final var pair = createUser(request, Role.ROLE_TRAINEE);
        final var user = pair.first();
        final var password = pair.second();

        final var trainee = Trainee.builder().user(user).dateOfBirth(request.getDateOfBirth()).address(
                request.getAddress()).build();

        try {
            traineeRepository.saveAndFlush(trainee);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUsernameException();
        }

        return Pair.of(trainee, password);
    }

    @Override
    @Transactional
    public @Nonnull Trainee update(@Nonnull String username, @Nonnull UpdateTraineeRequest updateRequest) {
        final var trainee = findByUsername(username);

        updateUser(trainee.getUser(), updateRequest);
        trainee.setAddress(updateRequest.getAddress());
        trainee.setDateOfBirth(updateRequest.getDateOfBirth());

        return trainee;
    }

    @Override
    @Transactional
    public void delete(@Nonnull String username) {
        final var trainee = findByUsername(username);
        trainingRepository.deleteByTraineeId(trainee.getId());
        trainingAssignmentRepository.deleteByTraineeId(trainee.getId());
        refreshTokenRepository.deleteByUsername(trainee.getUser().getUsername());
        traineeRepository.delete(trainee);
    }

    @Override
    public @Nonnull List<Trainer> findActiveTrainersNotAssignedToTrainee(@Nonnull String traineeUsername) {
        findByUsername(traineeUsername);
        return trainingAssignmentRepository.findActiveTrainersNotAssignedToTrainee(traineeUsername);
    }

    @Override
    @Transactional
    public @Nonnull List<Trainer> updateTrainerList(@Nonnull String traineeUsername,
                                                    @Nonnull List<String> trainerUsernames) {
        final var trainee = findByUsername(traineeUsername);
        final var newTrainers = trainerRepository.findByUsernames(trainerUsernames);

        if (newTrainers.size() != trainerUsernames.size()) {
            var foundNamesSet = newTrainers.stream().map(trainer -> trainer.getUser().getUsername()).collect(
                    Collectors.toSet());
            var missingNames = trainerUsernames.stream().filter(trainer -> !foundNamesSet.contains(trainer)).toList();
            throw new ResourceNotFoundException(getResourceName(), missingNames.toArray());
        }

        final var existingAssignments = trainingAssignmentRepository.findByTraineeId(trainee.getId());
        final var existingTrainerIds = existingAssignments.stream().map(TrainingAssignment::getTrainerId).collect(
                Collectors.toSet());

        removeTrainingAssignments(trainee, existingTrainerIds, newTrainers);
        addTrainingAssignments(trainee, existingTrainerIds, newTrainers);

        return newTrainers;
    }

    @Override
    @Transactional
    public @Nonnull List<Trainer> getTrainerList(@NonNull String username) {
        final var trainee = findByUsername(username);
        return trainingAssignmentRepository.findByTraineeId(trainee.getId()).stream().map(
                TrainingAssignment::getTrainer).toList();
    }

    private void removeTrainingAssignments(@Nonnull Trainee trainee, @Nonnull Collection<UUID> existingTrainerIds,
                                           @Nonnull Collection<Trainer> newTrainers) {
        final var newTrainerIds = newTrainers.stream().map(Trainer::getId).collect(Collectors.toSet());

        final var toRemoveTrainerIds = new HashSet<>(existingTrainerIds);
        toRemoveTrainerIds.removeAll(newTrainerIds);

        if (!toRemoveTrainerIds.isEmpty()) {
            trainingAssignmentRepository.deleteByTraineeIdAndTrainerIds(trainee.getId(), toRemoveTrainerIds);
        }
    }

    private void addTrainingAssignments(@Nonnull Trainee trainee, @Nonnull Collection<UUID> existingTrainerIds,
                                        @Nonnull Collection<Trainer> newTrainers) {
        final var toAddTrainers = newTrainers
                .stream()
                .filter(trainer -> !existingTrainerIds.contains(trainer.getId())).toList();


        if (!toAddTrainers.isEmpty()) {
            final var toAddAssignments = toAddTrainers.stream().map(trainer -> new TrainingAssignment(trainee,
                    trainer)).toList();
            trainingAssignmentRepository.saveAll(toAddAssignments);
        }
    }

    @Override
    protected @Nonnull BaseUserRepository<Trainee> getRepository() {
        return traineeRepository;
    }

    @Override
    protected @NonNull String getResourceName() {
        return "trainee";
    }

    @Override
    protected @Nonnull CredentialsService getCredentialsService() {
        return credentialsService;
    }
}
