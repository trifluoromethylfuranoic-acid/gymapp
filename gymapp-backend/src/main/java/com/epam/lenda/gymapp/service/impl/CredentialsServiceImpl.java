package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.request.CreateUserRequest;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.CredentialsService;
import jakarta.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
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
public class CredentialsServiceImpl implements CredentialsService {
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;
    @Value("${application.security.defaultPasswordLength}")
    private int defaultPasswordLength;

    @Override
    public @Nonnull String generateUsername(@Nonnull CreateUserRequest request) {
        var initial = request.getFirstName() + "." + request.getLastName();
        var candidate = initial;
        var counter = 1;
        while (userRepository.existsByUsername(candidate)) {
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
    public @Nonnull String encodePassword(@Nonnull String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public @Nonnull Credentials generateCredentials(@Nonnull CreateUserRequest request) {
        var username = generateUsername(request);
        var password = generatePassword();
        var passwordHash = encodePassword(password);

        return new Credentials(username, password, passwordHash);
    }

    @Override
    public boolean isUsernameTaken(@Nonnull String newUsername, @Nonnull UUID id) {
        final var userOpt = userRepository.findByUsername(newUsername);
        return userOpt.isPresent() && !Objects.equals(userOpt.get().getId(), id);
    }
}
