package com.epam.lenda.gymapp.report.config;

import com.epam.lenda.gymapp.report.util.MonthKeySerializer;
import java.time.Month;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {
    @Bean
    public SimpleModule monthKeyModule() {
        final var module = new SimpleModule();
        module.addKeySerializer(Month.class, new MonthKeySerializer());
        return module;
    }
}
