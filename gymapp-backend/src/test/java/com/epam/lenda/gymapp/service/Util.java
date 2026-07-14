package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import java.util.Date;

public class Util {
    static Trainee trainee(long id, String username) {
        return Trainee.builder().user(user(username)).dateOfBirth(new Date(631152000000L)).address("Address").build();
    }

    static Trainer trainer(long id, String username) {
        return Trainer.builder().user(user(username)).specialization(trainingType("Strength")).build();
    }

    static TrainingType trainingType(String name) {
        return new TrainingType(name);
    }

    private static User user(String username) {
        return User.builder().firstName("First").lastName("Last").username(username).password("password").isActive(
                true).build();
    }
}
