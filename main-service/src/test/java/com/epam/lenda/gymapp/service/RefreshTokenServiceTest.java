package com.epam.lenda.gymapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.TestConfig;
import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.RefreshToken;
import com.epam.lenda.gymapp.repository.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({TestConfig.class})
@ActiveProfiles("test")
class RefreshTokenServiceTest {
    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private SecureRandom secureRandom;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    void create_success() {
        final var trainee = Util.trainee("john.doe");

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(trainee.getUser()));

        final var tokenPair = refreshTokenService.create("john.doe");
        final var token = tokenPair.first();

        verify(refreshTokenRepository).save(token);

        assertFalse(token.getRevoked());
    }

    @Test
    void create_throwsOnMissingUser() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> refreshTokenService.create("john.doe"));
    }

    @Test
    void validateAndRotate_success() {
        final var trainee = Util.trainee("john.doe");
        final var rawToken = "banana";
        final var tokenHash = refreshTokenService.hashToken(rawToken);
        final var token = RefreshToken
                .builder()
                .issued(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusSeconds(100))
                .user(trainee.getUser())
                .revoked(false)
                .tokenHash(tokenHash)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(trainee.getUser()));

        final var tokenPairOpt = refreshTokenService.validateAndRotate(rawToken);

        assertTrue(tokenPairOpt.isPresent());

        final var newToken = tokenPairOpt.get().first();

        verify(refreshTokenRepository).save(newToken);
        assertFalse(newToken.getRevoked());
    }

    @Test
    void validateAndRotate_rejectsExpiredToken() {
        final var trainee = Util.trainee("john.doe");
        final var rawToken = "banana";
        final var tokenHash = refreshTokenService.hashToken(rawToken);
        final var token = RefreshToken
                .builder()
                .issued(LocalDateTime.now().minusSeconds(200))
                .validUntil(LocalDateTime.now().minusSeconds(100))
                .user(trainee.getUser())
                .revoked(false)
                .tokenHash(tokenHash)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        final var tokenPairOpt = refreshTokenService.validateAndRotate(rawToken);

        assertTrue(tokenPairOpt.isEmpty());
    }

    @Test
    void validateAndRotate_rejectsRevokedToken() {
        final var trainee = Util.trainee("john.doe");
        final var rawToken = "banana";
        final var tokenHash = refreshTokenService.hashToken(rawToken);
        final var token = RefreshToken
                .builder()
                .issued(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusSeconds(100))
                .user(trainee.getUser())
                .revoked(true)
                .tokenHash(tokenHash)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        final var tokenPairOpt = refreshTokenService.validateAndRotate(rawToken);

        assertTrue(tokenPairOpt.isEmpty());
    }


}
