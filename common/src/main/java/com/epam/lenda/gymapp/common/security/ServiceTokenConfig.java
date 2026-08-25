package com.epam.lenda.gymapp.common.security;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ServiceTokenClient.class)
public class ServiceTokenConfig {
    @Bean
    public RequestInterceptor requestInterceptor(ServiceTokenClient serviceTokenClient) {
        return (template) ->
                template.header("X-Service-Token", "Bearer " + serviceTokenClient.getToken());
    }
}
