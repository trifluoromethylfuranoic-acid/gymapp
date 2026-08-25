package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.RefreshToken;
import com.epam.lenda.gymapp.util.Pair;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenService extends BaseService<RefreshToken, UUID> {

    @Transactional
    @Nonnull
    Pair<RefreshToken, String> create(String username);

    @Transactional
    @Nonnull
    Optional<Pair<RefreshToken, String>> validateAndRotate(String rawToken);

    @Nonnull
    Optional<RefreshToken> findToken(@Nonnull String token);

    @Transactional
    void revoke(@Nonnull String rawToken);

    @Transactional
    void revokeAllForUser(@Nonnull String username);

    @Nonnull
    String hashToken(@Nonnull String token);
}
