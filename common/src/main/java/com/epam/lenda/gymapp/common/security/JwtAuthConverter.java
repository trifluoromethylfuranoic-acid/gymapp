package com.epam.lenda.gymapp.common.security;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (!"user".equals(jwt.getClaimAsString("type"))) {
            return null;
        }

        UserPrincipal principal;
        try {
            principal = UserPrincipal
                    .builder()
                    .id(UUID.fromString(Objects.requireNonNull(jwt.getClaimAsString("id"))))
                    .username(Objects.requireNonNull(jwt.getSubject()))
                    .role(Role.valueOf(Objects.requireNonNull(jwt.getClaim("role"))))
                    .build();
        } catch (NullPointerException | IllegalArgumentException ignored) {
            log.warn("Invalid JWT format with valid signature: {}", jwt);
            return null;
        }
        final var authorities = List.of(principal.getRole());

        return new JwtAuthenticationToken(jwt, authorities, principal.getUsername()) {
            @Override
            public @NonNull Object getPrincipal() {
                return principal;
            }
        };
    }
}
