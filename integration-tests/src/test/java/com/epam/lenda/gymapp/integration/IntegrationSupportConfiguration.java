package com.epam.lenda.gymapp.integration;

import static org.mockito.Mockito.mock;

import com.netflix.discovery.EurekaClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class IntegrationSupportConfiguration {
    @Bean
    EurekaClient eurekaClient() {
        return mock(EurekaClient.class);
    }
}
