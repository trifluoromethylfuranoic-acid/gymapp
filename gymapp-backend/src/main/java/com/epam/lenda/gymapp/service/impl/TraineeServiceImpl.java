package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.service.AuthService;
import com.epam.lenda.gymapp.service.TraineeService;
import com.epam.lenda.gymapp.util.Pair;
import com.epam.lenda.gymapp.util.UserUtil;
import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TraineeServiceImpl extends BaseUserServiceImpl<Trainee> implements TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final AuthService authService;
    private final TrainingRepository trainingRepository;
    private final PasswordEncoder passwordEncoder;

    public TraineeServiceImpl(TraineeRepository traineeRepository, TrainerRepository trainerRepository,
                              AuthService authService, TrainingRepository trainingRepository,
                              PasswordEncoder passwordEncoder) {
        super(traineeRepository);
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.authService = authService;
        this.trainingRepository = trainingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public @Nonnull Pair<Trainee, String> create(@Nonnull String firstName, @Nonnull String lastName,
                                                 @Nonnull LocalDate dateOfBirth,
                                                 @Nonnull String address) {
        var credentials = authService.generateCredentials(firstName, lastName);
        var trainee = Trainee.builder().dateOfBirth(dateOfBirth).address(address).firstName(firstName).lastName(
                lastName).username(credentials.username()).password(credentials.passwordHash()).isActive(
                        true).build();
        traineeRepository.save(trainee);

        return Pair.of(trainee, credentials.password());
    }

    @Override
    public void delete(@Nonnull String username) {
        traineeRepository.findByUsername(username).ifPresent(trainee -> {
            traineeRepository.delete(trainee);
            trainingRepository.removeTraineeRefs(trainee.getId());
        });
    }

    @Override
    public @Nonnull Trainee update(@Nonnull String username, @Nonnull TraineeService.UpdateRequest updateRequest) {
        var trainee = traineeRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

        if (UserUtil.isUsernameTaken(updateRequest.username(), trainee.getId(), traineeRepository, trainerRepository)) {
            throw new DuplicateUsernameException();
        }

        var passwordHash = passwordEncoder.encode(updateRequest.password());

        trainee.setUsername(updateRequest.username());
        trainee.setFirstName(updateRequest.firstName());
        trainee.setLastName(updateRequest.lastName());
        trainee.setPassword(passwordHash);
        trainee.setIsActive(updateRequest.isActive());
        trainee.setAddress(updateRequest.address());
        trainee.setDateOfBirth(updateRequest.dateOfBirth());

        return trainee;
    }
}
