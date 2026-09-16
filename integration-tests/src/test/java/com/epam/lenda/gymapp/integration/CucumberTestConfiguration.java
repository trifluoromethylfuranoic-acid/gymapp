package com.epam.lenda.gymapp.integration;

import io.cucumber.spring.ScenarioScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CucumberTestConfiguration {
    @Bean
    @ScenarioScope
    ScenarioState scenarioState() {
        return new ScenarioState();
    }
}
