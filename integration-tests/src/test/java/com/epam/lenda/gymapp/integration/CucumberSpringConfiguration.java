package com.epam.lenda.gymapp.integration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = CucumberTestConfiguration.class)
class CucumberSpringConfiguration {
}
