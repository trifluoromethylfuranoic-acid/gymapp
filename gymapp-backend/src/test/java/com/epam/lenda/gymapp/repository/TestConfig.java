package com.epam.lenda.gymapp.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import tools.jackson.databind.ObjectMapper;

@TestConfiguration
@PropertySource(value = {"classpath:/application.yaml", "classpath:/application-test.yaml"}, factory = YamlPropertySourceFactory.class)
public class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
