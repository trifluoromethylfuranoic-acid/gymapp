package com.epam.lenda.gymapp.config;

import com.epam.lenda.gymapp.model.*;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig {
    @Bean
    public MapBasedStorage<Trainee> traineesData() {
        return new MapBasedStorage<>();
    }

    @Bean
    public MapBasedStorage<Trainer> trainersData() {
        return new MapBasedStorage<>();
    }

    @Bean
    public MapBasedStorage<Training> trainingsData() {
        return new MapBasedStorage<>();
    }
}
