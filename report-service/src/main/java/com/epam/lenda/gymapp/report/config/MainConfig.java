package com.epam.lenda.gymapp.report.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
