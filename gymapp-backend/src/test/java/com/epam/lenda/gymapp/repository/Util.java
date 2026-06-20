package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static void loadTrainees(String path, MapBasedStorage<User> users, MapBasedStorage<Trainee> trainees) {
        var file = Util.class.getClassLoader().getResource(path).getFile();
        for (var row : loadCsv(file)) {
            var trainee = loadTrainee(row);

            var nextUserId = users.nextId();
            trainee.getUser().setId(nextUserId);
            users.getData().put(nextUserId, trainee.getUser());

            var nextTraineeId = trainees.nextId();
            trainee.setId(nextTraineeId);
            trainees.getData().put(nextTraineeId, trainee);
        }
    }

    public static Trainee loadTrainee(String[] row) {
        var user = User.builder().firstName(row[0]).lastName(row[1]).username(row[2]).password(row[3]).isActive(
                Boolean.parseBoolean(row[4])).build();
        var trainee = Trainee.builder().user(user).address(row[5]).dateOfBirth(LocalDate.parse(row[6])).trainings(
                new ArrayList<>()).trainerAssignments(new ArrayList<>()).build();
        return trainee;
    }

    public static void loadTrainers(String path, MapBasedStorage<User> users, MapBasedStorage<Trainer> trainers,
                                    MapBasedStorage<TrainingType> trainingTypes) {
        var file = Util.class.getClassLoader().getResource(path).getFile();
        for (var row : loadCsv(file)) {
            var specializationOpt = trainingTypes.getData().values().stream().filter(
                    tt -> tt.getName().equalsIgnoreCase(row[5])).findAny();
            TrainingType specialization;
            if (specializationOpt.isEmpty()) {
                specialization = TrainingType.builder().id(trainingTypes.nextId()).name(row[5]).build();
                trainingTypes.getData().put(specialization.getId(), specialization);
            } else {
                specialization = specializationOpt.get();
            }

            var trainer = loadTrainer(row, specialization);

            var nextUserId = users.nextId();
            trainer.getUser().setId(nextUserId);
            users.getData().put(nextUserId, trainer.getUser());

            var nextTrainerId = trainers.nextId();
            trainer.setId(nextTrainerId);
            trainers.getData().put(nextTrainerId, trainer);
        }
    }

    public static Trainer loadTrainer(String[] row, TrainingType specialization) {
        var user = User.builder().firstName(row[0]).lastName(row[1]).username(row[2]).password(row[3]).isActive(
                Boolean.parseBoolean(row[4])).build();
        var trainer = Trainer.builder().user(user).specialization(specialization).trainings(
                new ArrayList<>()).traineeAssignments(new ArrayList<>()).build();
        return trainer;
    }


    private static List<String[]> loadCsv(String path) {
        try (CSVReader reader = new CSVReader(
                new FileReader(path))) {
            var csvRows = reader.readAll();
            csvRows.remove(0);
            return csvRows;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
