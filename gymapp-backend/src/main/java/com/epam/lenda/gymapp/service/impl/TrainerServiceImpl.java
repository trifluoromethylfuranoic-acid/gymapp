package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.service.AuthService;
import com.epam.lenda.gymapp.service.TrainerService;
import com.epam.lenda.gymapp.util.Pair;
import com.epam.lenda.gymapp.util.UserUtil;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Slf4j
public class TrainerServiceImpl extends BaseUserServiceImpl<Trainer> implements TrainerService {
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public TrainerServiceImpl(TrainerRepository trainerRepository, TraineeRepository traineeRepository,
                              AuthService authService, PasswordEncoder passwordEncoder) {
        super(trainerRepository);
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public @Nonnull Pair<Trainer, String> create(@NonNull String firstName, @NonNull String lastName,
                                                 @Nonnull TrainingType specialization) {
        var credentials = authService.generateCredentials(firstName, lastName);
        var trainer = Trainer.builder().specialization(specialization).firstName(firstName).lastName(
                lastName).username(credentials.username()).password(credentials.passwordHash()).isActive(
                        true).build();
        trainerRepository.save(trainer);

        return Pair.of(trainer, credentials.password());
    }

    @Override
    public @Nonnull Trainer update(@Nonnull String username, @Nonnull TrainerService.UpdateRequest updateRequest) {
        var trainee = trainerRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

        if (UserUtil.isUsernameTaken(updateRequest.username(), trainee.getId(), trainerRepository, traineeRepository)) {
            throw new DuplicateUsernameException();
        }

        var passwordHash = passwordEncoder.encode(updateRequest.password());

        trainee.setUsername(updateRequest.username());
        trainee.setFirstName(updateRequest.firstName());
        trainee.setLastName(updateRequest.lastName());
        trainee.setPassword(passwordHash);
        trainee.setIsActive(updateRequest.isActive());
        trainee.setSpecialization(updateRequest.specialization());

        return trainee;
    }
}
