package com.epam.lenda.gymapp;

import com.epam.lenda.gymapp.model.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static Trainee trainee(String username) {
        return Trainee
                .builder()
                .user(user(username))
                .dateOfBirth(new Date(631152000000L))
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

    public static Training training() throws ParseException {
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

    public static java.util.Date dateTime(String value) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
    }

    public static java.util.Date date(String value) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(value);
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
