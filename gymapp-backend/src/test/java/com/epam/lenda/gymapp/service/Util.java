package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import java.time.LocalDate;

public class Util {
    static Trainee trainee(long id, String username) {
        return Trainee.builder().id(id).username(username).firstName("First").lastName(
                "Last").isActive(true).dateOfBirth(LocalDate.of(1990, 1, 1)).address(
                        "Address").build();
    }

    static Trainer trainer(long id, String username) {
        return Trainer.builder().id(id).username(username).firstName("First").lastName(
                "Last").isActive(true).specialization(TrainingType.STRENGTH).build();
    }
}
