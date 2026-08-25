package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.service.AccessTokenService;
import jakarta.annotation.Nonnull;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final String issuer;
    private final long accessTokenLifetimeSeconds;

    public AccessTokenServiceImpl(JwtEncoder jwtEncoder,
                                  Clock clock,
                                  @Value("${application.security.accessToken.issuer}") String issuer,
                                  @Value("${application.security.accessToken.lifetimeSeconds}") long accessTokenLifetimeSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.issuer = issuer;
        this.accessTokenLifetimeSeconds = accessTokenLifetimeSeconds;
    }

    @Nonnull
    @Override
    public String encode(@Nonnull GymUserDetails userDetails) {
        final var now = Instant.now(clock);
        final var jwtClaims = JwtClaimsSet
                .builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenLifetimeSeconds))
                .subject(userDetails.getUsername())
                .claim("id", userDetails.getId().toString())
                .claim("role", userDetails.getRole().toString())
                .claim("type", "user")
                .audience( List.of("internal-services"))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaims)).getTokenValue();
    }
}
