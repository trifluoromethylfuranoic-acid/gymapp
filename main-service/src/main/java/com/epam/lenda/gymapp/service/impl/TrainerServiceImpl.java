package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.request.CreateTrainerRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTrainerRequest;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingAssignment;
import com.epam.lenda.gymapp.repository.*;
import com.epam.lenda.gymapp.service.CredentialsService;
import com.epam.lenda.gymapp.service.TrainerService;
import com.epam.lenda.gymapp.util.Pair;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TrainerServiceImpl extends BaseUserServiceImpl<Trainer, CreateTrainerRequest, UpdateTrainerRequest> implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final CredentialsService credentialsService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingAssignmentRepository trainingAssignmentRepository;

    public TrainerServiceImpl(TrainerRepository trainerRepository, CredentialsService credentialsService,
                              TrainingTypeRepository trainingTypeRepository,
                              TrainingAssignmentRepository trainingAssignmentRepository,
                              MeterRegistry meterRegistry) {
        super(meterRegistry);
        this.trainerRepository = trainerRepository;
        this.credentialsService = credentialsService;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingAssignmentRepository = trainingAssignmentRepository;
    }

    @Override
    @Transactional
    public @Nonnull Pair<Trainer, String> create(@Nonnull CreateTrainerRequest request) {
        final var pair = createUser(request, Role.ROLE_TRAINER);
        final var user = pair.first();
        final var password = pair.second();
        final var trainingTypeName = request.getSpecialization().trim();
        final var specialization = trainingTypeRepository.findByNameIgnoreCase(trainingTypeName).orElseThrow(
                () -> new ResourceNotFoundException("training type",
                        trainingTypeName));

        final var trainer = new Trainer(user, specialization);

        try {
            trainerRepository.saveAndFlush(trainer);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUsernameException();
        }

        return Pair.of(trainer, password);
    }

    @Override
    @Transactional
    public @Nonnull Trainer update(@Nonnull String username, @Nonnull UpdateTrainerRequest request) {
        final var trainer = findByUsername(username);

        updateUser(trainer.getUser(), request);

        return trainer;
    }

    @Override
    public @NonNull List<Trainee> getTraineeList(@NonNull String username) {
        final var trainer = findByUsername(username);
        return trainingAssignmentRepository.findByTrainerId(trainer.getId()).stream().map(
                TrainingAssignment::getTrainee).toList();
    }

    @Override
    protected @Nonnull BaseUserRepository<Trainer> getRepository() {
        return trainerRepository;
    }

    @Override
    protected @NonNull String getResourceName() {
        return "trainer";
    }

    @Override
    protected @Nonnull CredentialsService getCredentialsService() {
        return credentialsService;
    }
}
