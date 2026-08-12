package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.RefreshToken;
import com.epam.lenda.gymapp.repository.RefreshTokenRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.RefreshTokenService;
import com.epam.lenda.gymapp.util.Pair;
import jakarta.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    private final long refreshTokenLifetimeSeconds;
    private final int refreshTokenLengthBytes;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   UserRepository userRepository,
                                   SecureRandom secureRandom,
                                   @Value("${application.security.refreshToken.lifetimeSeconds}") long refreshTokenLifetimeSeconds,
                                   @Value("${application.security.refreshToken.lengthBytes}") int refreshTokenLengthBytes) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.secureRandom = secureRandom;
        this.refreshTokenLifetimeSeconds = refreshTokenLifetimeSeconds;
        this.refreshTokenLengthBytes = refreshTokenLengthBytes;
    }

    @Transactional
    @Nonnull
    @Override
    public Pair<RefreshToken, String> create(String username) {
        final var user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException(
                "user", username));

        final var issued = LocalDateTime.now();
        final var expires = LocalDateTime.now().plusSeconds(refreshTokenLifetimeSeconds);
        final var rawToken = generateRandomString(refreshTokenLengthBytes);
        final var tokenHash = hashToken(rawToken);

        final var token = RefreshToken
                .builder()
                .issued(issued)
                .validUntil(expires)
                .user(user)
                .revoked(false)
                .tokenHash(tokenHash)
                .build();

        refreshTokenRepository.save(token);

        return Pair.of(token, rawToken);
    }

    @Transactional
    @Nonnull
    @Override
    public Optional<Pair<RefreshToken, String>> validateAndRotate(String rawToken) {
        return findToken(rawToken).map(token -> {
            final var now = LocalDateTime.now();
            if (now.isAfter(token.getValidUntil()) || token.getRevoked()) {
                return null;
            }

            token.setRevoked(true);

            return create(token.getUser().getUsername());
        });
    }

    @Override
    @Transactional
    public @Nonnull List<RefreshToken> findAll() {
        return refreshTokenRepository.findAll();
    }

    @Override
    @Transactional
    public @Nonnull RefreshToken findById(UUID uuid) {
        return refreshTokenRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("refresh token",
                uuid));
    }

    @Nonnull
    @Override
    @Transactional
    public Optional<RefreshToken> findToken(@Nonnull String token) {
        final var tokenHash = hashToken(token);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }

    @Transactional
    @Override
    public void revoke(@Nonnull String rawToken) {
        findToken(rawToken).ifPresent(token -> token.setRevoked(true));
    }

    @Transactional
    @Override
    public void revokeAllForUser(@Nonnull String username) {
        refreshTokenRepository.revokeByUsername(username);
    }

    @Nonnull
    @Override
    public String hashToken(@Nonnull String token) {
        try {
            final var digest = MessageDigest.getInstance("SHA-256");
            final var hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRandomString(int nBytes) {
        var bytes = new byte[nBytes];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
