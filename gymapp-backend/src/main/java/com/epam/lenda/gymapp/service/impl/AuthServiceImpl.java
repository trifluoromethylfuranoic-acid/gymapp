package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.auth.*;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.exception.WrongPasswordException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.AuthService;
import jakarta.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final SecureRandom secureRandom;
    @Value("${application.security.defaultPasswordLength}")
    private int defaultPasswordLength;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;
    private final UserMapper userMapper;

    @Override
    @Nonnull
    public SignupResponse signupTrainer(@Nonnull SignupTrainerRequest request) {
        final var username = generateUsername(request.firstName(), request.lastName());
        final var password = generatePassword();
        final var passwordHash = passwordEncoder.encode(password);
        final var specialization = trainingTypeRepository.findOrCreate(request.specialization());
        final var userEntity = User.builder().firstName(request.firstName()).lastName(request.lastName()).username(
                username).password(passwordHash).isActive(true).build();
        final var trainerEntity = Trainer.builder().user(userEntity).specialization(specialization).traineeAssignments(
                new ArrayList<>()).trainings(new ArrayList<>()).build();
        trainerRepository.save(trainerEntity);
        final var accessToken = accessTokenService.generateAccessToken(userMapper.toUserDetails(trainerEntity));

        log.debug("Signed up trainer {}", username);

        return SignupResponse.builder().username(username).password(password).accessToken(accessToken).build();
    }

    @Override
    @Nonnull
    public SignupResponse signupTrainee(@Nonnull SignupTraineeRequest request) {
        final var username = generateUsername(request.firstName(), request.lastName());
        final var password = generatePassword();
        final var passwordHash = passwordEncoder.encode(password);
        final var userEntity = User.builder().firstName(request.firstName()).lastName(request.lastName()).username(
                username).password(passwordHash).isActive(true).build();
        final var traineeEntity = Trainee.builder().user(userEntity).dateOfBirth(request.dateOfBirth()).address(
                request.address()).trainerAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
        traineeRepository.save(traineeEntity);
        final var accessToken = accessTokenService.generateAccessToken(userMapper.toUserDetails(traineeEntity));

        log.debug("Signed up trainee {}", username);

        return SignupResponse.builder().username(username).password(password).accessToken(accessToken).build();
    }

    @Override
    @Nonnull
    public LoginResponse login(@Nonnull LoginRequest request) {
        final var auth = authenticate(request.username(), request.password()).orElseThrow(
                () -> new WrongPasswordException("Incorrect credentials"));
        final var accessToken = accessTokenService.generateAccessToken((UserDetails) auth.getPrincipal());

        log.debug("Logged in user {}", request.username());

        return LoginResponse.builder().accessToken(accessToken).build();
    }

    @Override
    public void changePassword(@Nonnull String username, @Nonnull PasswordChangeRequest request) {
        if (!checkCredentials(username, request.oldPassword())) {
            log.debug("Unable to change password, wrong password");
            throw new WrongPasswordException("Incorrect password!");
        }
        final var userEntity = userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);

        log.debug("Changed password for user {}", username);

        userEntity.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    public @Nonnull String generateUsername(@Nonnull String firstName, @Nonnull String lastName) {
        var initial = firstName + "." + lastName;
        var candidate = initial;
        var counter = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = initial + counter++;
        }

        log.trace("Generated username {}", candidate);

        return candidate;
    }

    @Override
    public @Nonnull String generatePassword() {
        var bytes = new byte[(defaultPasswordLength * 3 + 3) / 4];
        secureRandom.nextBytes(bytes);
        var base64 = new String(Base64.getEncoder().encode(bytes));
        return base64.substring(0, defaultPasswordLength);
    }

    @Override
    public boolean checkCredentials(@Nonnull String username, @Nonnull String password) {
        return authenticate(username, password).isPresent();
    }

    @Override
    @Nonnull
    public Optional<Authentication> authenticate(@Nonnull String username, @Nonnull String password) {
        var token = new UsernamePasswordAuthenticationToken(username, password);
        try {
            return Optional.of(authenticationManager.authenticate(token));
        } catch (AuthenticationException e) {
            return Optional.empty();
        }
    }
}
