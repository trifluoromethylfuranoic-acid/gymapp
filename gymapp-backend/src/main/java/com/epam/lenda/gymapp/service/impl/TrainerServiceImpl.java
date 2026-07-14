package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.repository.*;
import com.epam.lenda.gymapp.service.AuthService;
import com.epam.lenda.gymapp.service.CredentialsService;
import com.epam.lenda.gymapp.service.TrainerService;
import com.epam.lenda.gymapp.util.Pair;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerServiceImpl extends BaseUserServiceImpl<Trainer, TrainerService.TrainerCreateRequest, TrainerService.TrainerUpdateRequest> implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final CredentialsService credentialsService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthService authService;
    private final TrainingAssignmentRepository trainingAssignmentRepository;

    @Override
    @Transactional
    public @Nonnull Pair<Trainer, String> create(@Nonnull TrainerCreateRequest request) {
        final var pair = createUser(request);
        final var user = pair.first();
        final var password = pair.second();
        final var specialization = trainingTypeRepository.findByNameIgnoreCase(
                request.getSpecialization().trim()).orElseThrow(ResourceNotFoundException::new);

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
    public @Nonnull Trainer update(@Nonnull String username, @Nonnull TrainerUpdateRequest request) {
        final var trainer = findByUsername(username);
        final var specialization = trainingTypeRepository.findByNameIgnoreCase(
                request.getSpecialization().trim()).orElseThrow(ResourceNotFoundException::new);
        updateUser(trainer.getUser(), request);
        trainer.setSpecialization(specialization);

        try {
            trainerRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUsernameException();
        }

        return trainer;
    }

    @Override
    public @NonNull List<Trainer> findNotAssignedToTrainee(@NonNull String traineeUsername) {
        return trainingAssignmentRepository.findTrainersNotAssignedToTrainee(traineeUsername);
    }

    @Override
    protected @Nonnull BaseUserRepository<Trainer> getRepository() {
        return trainerRepository;
    }

    @Override
    protected @Nonnull CredentialsService getCredentialsService() {
        return credentialsService;
    }

    @Override
    protected @NonNull AuthService getAuthService() {
        return authService;
    }
}
