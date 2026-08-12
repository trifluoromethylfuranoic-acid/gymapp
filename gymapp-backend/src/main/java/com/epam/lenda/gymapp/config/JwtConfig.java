package com.epam.lenda.gymapp.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {
    @Bean
    public SecretKey accessTokenSecretKey(@Value("${application.security.accessToken.secret}") String secret) {
        final var bytes = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey accessTokenSecretKey) {
        return NimbusJwtEncoder.withSecretKey(accessTokenSecretKey).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey accessTokenSecretKey) {
        return NimbusJwtDecoder.withSecretKey(accessTokenSecretKey).build();
    }
}
