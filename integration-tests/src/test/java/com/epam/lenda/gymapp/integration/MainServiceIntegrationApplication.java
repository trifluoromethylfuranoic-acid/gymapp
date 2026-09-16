package com.epam.lenda.gymapp.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.epam.lenda.gymapp.aspect",
        "com.epam.lenda.gymapp.common",
        "com.epam.lenda.gymapp.config",
        "com.epam.lenda.gymapp.controller.rest",
        "com.epam.lenda.gymapp.mapper",
        "com.epam.lenda.gymapp.service",
        "com.epam.lenda.gymapp.validation"
})
@EntityScan("com.epam.lenda.gymapp.model")
@EnableJpaRepositories("com.epam.lenda.gymapp.repository")
@Import(IntegrationSupportConfiguration.class)
class MainServiceIntegrationApplication {
}
