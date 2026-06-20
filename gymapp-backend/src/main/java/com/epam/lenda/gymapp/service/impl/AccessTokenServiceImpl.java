package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccessTokenServiceImpl implements AccessTokenService {
    private final SecretKey key;
    @Value("${application.security.jwt.accessTokenLifetimeSeconds}")
    private int accessTokenLifetimeSeconds;

    private AccessTokenServiceImpl(@Value("${application.security.jwt.secret}") String jwtSecret) {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateAccessToken(UserDetails user) {
        var issued = Date.from(Instant.now());
        var expires = Date.from(Instant.now().plusSeconds(accessTokenLifetimeSeconds));

        var token = Jwts.builder().subject(user.getUsername()).claim("id", user.getId()).claim("role",
                user.getAuthorities().stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "Role not defined")).getAuthority()).issuedAt(issued).expiration(expires).signWith(
                                key).compact();

        log.debug("Generated access token for {}, expires {}", user.getUsername(), expires);

        return token;
    }

    @Override
    public Optional<UserDetails> parse(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            log.debug("Unable to parse access token");
            return Optional.empty();
        }
        var user = UserDetails.builder().username(claims.getSubject()).role(Role.valueOf(claims.get("role",
                String.class))).id(claims.get("id", Long.class)).isActive(true).build();

        log.debug("Parsed access token for {}, expires {}", user.getUsername(), claims.getExpiration());

        return Optional.of(user);
    }
}
