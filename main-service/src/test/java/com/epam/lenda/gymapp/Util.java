package com.epam.lenda.gymapp;

import com.epam.lenda.gymapp.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Util {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Trainee trainee(String username) {
        return Trainee
                .builder()
                .user(user(username))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Address")
                .build();
    }

    public static Trainer trainer(String username) {
        return Trainer
                .builder()
                .user(user(username))
                .specialization(trainingType("Strength"))
                .build();
    }

    public static TrainingType trainingType(String name) {
        return new TrainingType(name);
    }

    public static Training training() {
        final var type = trainingType("Fitness");
        return Training
                .builder()
                .trainee(new Trainee("John", "Doe", "john.doe",
                                     "password", true, null, "Main Street"))
                .trainer(new Trainer("Jane", "Smith", "jane.smith",
                                     "password", true, type))
                .name("Morning workout")
                .type(type)
                .datetime(dateTime("2026-07-27 10:00:00"))
                .durationMinutes(60)
                .build();
    }

    public static ZonedDateTime dateTime(String value) {
        return LocalDateTime.parse(value, DATE_TIME_FORMAT).atZone(ZoneOffset.UTC);
    }

    public static LocalDate localDate(String value) {
        return LocalDate.parse(value);
    }

    private static User user(String username) {
        return User
                .builder()
                .firstName("First")
                .lastName("Last")
                .username(username)
                .password("password")
                .isActive(true)
                .failedLoginAttempts(0)
                .role(Role.ROLE_ADMIN)
                .build();
    }
}
