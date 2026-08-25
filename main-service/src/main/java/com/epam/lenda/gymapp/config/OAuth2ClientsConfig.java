package com.epam.lenda.gymapp.config;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
public class OAuth2ClientsConfig {
    @Bean
    public RegisteredClientRepository registeredClientRepository(
            PasswordEncoder passwordEncoder,
            @Value("${application.security.clients.secrets.gatewayService}") String gatewayServiceSecret,
            @Value("${application.security.clients.secrets.mainService}") String mainServiceSecret,
            @Value("${application.security.clients.secrets.reportService}") String reportServiceSecret,
            @Value("${application.security.interserviceToken.lifetimeSeconds}") long interserviceTokenLifetimeSeconds) {

        final var gateway = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("gateway")
                .clientSecret(passwordEncoder.encode(gatewayServiceSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("internal.call")
                .tokenSettings(TokenSettings
                                       .builder()
                                       .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                                       .accessTokenTimeToLive(Duration.ofSeconds(interserviceTokenLifetimeSeconds))
                                       .build())
                .build();

        final var mainService = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("main-service")
                .clientSecret(passwordEncoder.encode(mainServiceSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("internal.call")
                .tokenSettings(TokenSettings
                                       .builder()
                                       .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                                       .accessTokenTimeToLive(Duration.ofSeconds(interserviceTokenLifetimeSeconds))
                                       .build())
                .build();

        final var reportService = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("report-service")
                .clientSecret(passwordEncoder.encode(reportServiceSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("internal.call")
                .tokenSettings(TokenSettings
                                       .builder()
                                       .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                                       .accessTokenTimeToLive(Duration.ofSeconds(interserviceTokenLifetimeSeconds))
                                       .build())
                .build();

        return new InMemoryRegisteredClientRepository(gateway, mainService, reportService);
    }
}
