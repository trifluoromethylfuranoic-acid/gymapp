package com.epam.lenda.gymapp.common.security;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Slf4j
@Import(JwtAuthConverter.class)
public class ResourceServerConfig {
    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
        final var issuerUri = properties.getJwt().getIssuerUri();
        log.info("Configuring lazy JwtDecoder for issuer {}", issuerUri);

        return new SupplierJwtDecoder(() -> {
            final var decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

            final var audienceValidator = new JwtClaimValidator<>(
                    JwtClaimNames.AUD,
                    aud -> aud instanceof List<?> list && list.contains("internal-services"));

            final var withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));

            return decoder;
        });
    }
}
