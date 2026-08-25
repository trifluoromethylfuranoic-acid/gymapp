package com.epam.lenda.gymapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.model.Role;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AccessTokenServiceTest {
    @Autowired
    private AccessTokenService accessTokenService;
    @MockitoBean
    private Clock clock;
    @Autowired
    private JWKSource<SecurityContext> jwkSource;
    @Autowired
    private Environment environment;

    private static final GymUserDetails USER_DETAILS = GymUserDetails.builder().username("john.doe").role(
            Role.ROLE_TRAINEE).isActive(true).id(UUID.randomUUID()).build();

    @BeforeEach
    void defaultClock() {
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    void encodesAndDecodes_success() {
        final var accessToken = accessTokenService.encode(USER_DETAILS);

        final var jwt = jwtDecoder().decode(accessToken);

        assertEquals(issuer(), String.valueOf(jwt.getIssuer()));
        assertEquals(USER_DETAILS.getUsername(), jwt.getSubject());
        assertEquals(USER_DETAILS.getId().toString(), jwt.getClaimAsString("id"));
        assertEquals(USER_DETAILS.getRole().toString(), jwt.getClaimAsString("role"));
        assertEquals("user", jwt.getClaimAsString("type"));
        assertTrue(jwt.getAudience().contains("internal-services"));
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void rejectsTamperedSignature() {
        final var accessToken = accessTokenService.encode(USER_DETAILS);

        final var parts = accessToken.split("\\.");
        final var payload = parts[1];
        parts[1] = (payload.charAt(0) == 'A' ? 'B' : 'A') + payload.substring(1);
        final var tampered = String.join(".", parts);

        assertThrows(JwtException.class, () -> jwtDecoder().decode(tampered));
    }

    @Test
    void rejectsExpiredToken() {
        final var now = Instant.now();
        // 1st call -> issuedAt, 2nd call -> expiresAt basis: exp lands 100s in the past
        when(clock.instant()).thenReturn(now.minusSeconds(1300), now.minusSeconds(700));

        final var accessToken = accessTokenService.encode(USER_DETAILS);

        assertThrows(JwtException.class, () -> jwtDecoder().decode(accessToken));
    }

    private JwtDecoder jwtDecoder() {
        final var decoder = NimbusJwtDecoder.withJwkSource(jwkSource).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer()),
                new JwtClaimValidator<>(JwtClaimNames.AUD,
                        aud -> aud instanceof List<?> list && list.contains("internal-services"))));
        return decoder;
    }

    private String issuer() {
        return environment.getProperty("application.security.accessToken.issuer");
    }
}
