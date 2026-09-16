package com.epam.lenda.gymapp.integration;

import java.time.ZonedDateTime;

class ScenarioState {
    String trainerUsername;
    String trainerAccessToken;
    String traineeUsername;
    String traineeAccessToken;
    ZonedDateTime trainingDate;

    void clear() {
        trainerUsername = null;
        trainerAccessToken = null;
        traineeUsername = null;
        traineeAccessToken = null;
        trainingDate = null;
    }
}
