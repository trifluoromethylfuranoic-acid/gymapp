package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final long accessTokenLifetimeSeconds;

    public AccessTokenServiceImpl(JwtEncoder jwtEncoder,
                                  JwtDecoder jwtDecoder,
                                  @Value("${application.security.accessToken.issuer}") String issuer,
                                  @Value("${application.security.accessToken.lifetimeSeconds}") long accessTokenLifetimeSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.issuer = issuer;
        this.accessTokenLifetimeSeconds = accessTokenLifetimeSeconds;
    }

    @Nonnull
    @Override
    public String encode(@Nonnull GymUserDetails userDetails) {
        final var jwtClaims = JwtClaimsSet
                .builder()
                .issuer(issuer)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(accessTokenLifetimeSeconds))
                .subject(userDetails.getUsername())
                .claim("id", userDetails.getId().toString())
                .claim("role", userDetails.getRole().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaims)).getTokenValue();
    }

    @Nonnull
    @Override
    public Optional<GymUserDetails> decode(@Nonnull String accessToken) {
        try {
            final var jwt = jwtDecoder.decode(accessToken);
            final var userDetails = GymUserDetails
                    .builder()
                    .id(UUID.fromString(Objects.requireNonNull(jwt.getClaim("id"))))
                    .username(Objects.requireNonNull(jwt.getSubject()))
                    .isActive(true)
                    .isLocked(false)
                    .password(null)
                    .role(Role.valueOf(Objects.requireNonNull(jwt.getClaim("role"))))
                    .build();

            return Optional.of(userDetails);

        } catch (JwtException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
