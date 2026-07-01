package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.service.AuthService;
import jakarta.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;
    @Value("${application.security.defaultPasswordLength}")
    private int defaultPasswordLength;


    @Override
    public @Nonnull String generateUsername(@Nonnull String firstName, @Nonnull String lastName) {
        var initial = firstName + "." + lastName;
        var candidate = initial;
        var counter = 1;
        while (traineeRepository.findByUsername(candidate).isPresent() || trainerRepository.findByUsername(
                candidate).isPresent()) {
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
    public @Nonnull Credentials generateCredentials(@Nonnull String firstName, @Nonnull String lastName) {
        var username = generateUsername(firstName, lastName);
        var password = generatePassword();
        var passwordHash = passwordEncoder.encode(password);

        return new Credentials(username, password, passwordHash);
    }
}
