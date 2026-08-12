package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.impl.AccessTokenServiceImpl;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AccessTokenServiceTest {
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private JwtEncoder jwtEncoder;

    private static final GymUserDetails USER_DETAILS = GymUserDetails.builder().username("john.doe").role(
            Role.ROLE_TRAINEE).isActive(true).id(UUID.randomUUID()).build();


    @Test
    void encodesAndDecodes_success() {
        final var accessToken = accessTokenService.encode(USER_DETAILS);
        final var decodedOpt = accessTokenService.decode(accessToken);

        assertTrue(decodedOpt.isPresent());
        final var decoded = decodedOpt.get();
        assertEquals(USER_DETAILS.getUsername(), decoded.getUsername());
        assertEquals(USER_DETAILS.getRole(), decoded.getRole());
        assertTrue(decoded.isActive());
        assertEquals(USER_DETAILS.getId(), decoded.getId());
    }

    @Test
    void encodesAndDecodes_rejectsInvalidSignature() {
        final var bytes = Base64.getDecoder().decode("someothersecretdfsasfdgasfsdfgsdfsdafgrrgbdfgkdfhgkdfhgdfgkg");
        final var secretKey = new SecretKeySpec(bytes, "HmacSHA256");
        final var jwtEncoder = NimbusJwtEncoder.withSecretKey(secretKey).build();
        final var jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
        final var otherAccessTokenService = new AccessTokenServiceImpl(jwtEncoder, jwtDecoder, "hello", 100);

        final var accessToken = otherAccessTokenService.encode(USER_DETAILS);
        final var decoded = accessTokenService.decode(accessToken);

        assertThat(decoded).isEmpty();
    }

    @Test
    void encodesAndDecodes_rejectsExpiredToken() {
        final var jwtClaims = JwtClaimsSet
                .builder()
                .issuer("hello")
                .issuedAt(Instant.now().minusSeconds(200))
                .expiresAt(Instant.now().minusSeconds(100))
                .subject(USER_DETAILS.getUsername())
                .claim("id", USER_DETAILS.getId().toString())
                .claim("role", USER_DETAILS.getRole().toString())
                .build();

        final var encoded = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaims)).getTokenValue();
        final var decoded = accessTokenService.decode(encoded);

        assertThat(decoded).isEmpty();
    }
}
