package com.epam.lenda.gymapp.init;

import com.epam.lenda.gymapp.model.*;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    @Value("${application.data.init.file.trainees:#{null}}")
    private String pathToTrainees;
    @Value("${application.data.init.file.trainers:#{null}}")
    private String pathToTrainers;
    @Value("${application.data.init.file.trainingTypes:#{null}}")
    private String pathToTrainingTypes;
    @Value("${application.data.init.file.trainerAssignments:#{null}}")
    private String pathToTrainerAssignments;
    @Value("${application.data.init.file.trainings:#{null}}")
    private String pathToTrainings;

    private final MapBasedStorage<User> usersData;
    private final MapBasedStorage<Trainee> traineesData;
    private final MapBasedStorage<Trainer> trainersData;
    private final MapBasedStorage<Training> trainingsData;
    private final MapBasedStorage<TrainingType> trainingTypesData;

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void loadData() throws IOException {
        loadTrainingTypes();
        loadTrainees();
        loadTrainers();
        loadTrainerAssignments();
        loadTrainings();
    }

    private void loadTrainees() throws IOException {
        if (pathToTrainees == null) {
            return;
        }

        log.info("Loading default trainees...");

        var resource = new ClassPathResource(pathToTrainees);
        var trainees = objectMapper.readValue(
                resource.getInputStream(), new TypeReference<List<Trainee>>() {
                }
        );
        trainees.forEach(t -> {
            traineesData.getData().put(t.getId(), t);
            usersData.getData().put(t.getUser().getId(), t.getUser());
        });
        traineesData.refreshNextId();
        usersData.refreshNextId();

        log.info("Loaded {} trainees", traineesData.getData().size());
    }

    private void loadTrainers() throws IOException {
        if (pathToTrainers == null) {
            return;
        }

        log.info("Loading default trainers...");

        var resource = new ClassPathResource(pathToTrainers);
        var trainers = objectMapper.readValue(
                resource.getInputStream(), new TypeReference<List<TrainerSeed>>() {
                }
        );
        trainers.forEach(t -> {
            var trainer = Trainer.builder().id(t.id()).user(t.user()).specialization(getRequired(
                    trainingTypesData.getData(), t.specializationId(), "training type")).traineeAssignments(
                            new ArrayList<>()).trainings(new ArrayList<>()).build();
            trainersData.getData().put(trainer.getId(), trainer);
            usersData.getData().put(trainer.getUser().getId(), trainer.getUser());
        });
        trainersData.refreshNextId();
        usersData.refreshNextId();

        log.info("Loaded {} trainers", trainersData.getData().size());
    }

    private void loadTrainingTypes() throws IOException {
        if (pathToTrainingTypes == null) {
            return;
        }

        log.info("Loading default training types...");

        var resource = new ClassPathResource(pathToTrainingTypes);
        var trainingTypes = objectMapper.readValue(
                resource.getInputStream(), new TypeReference<List<TrainingType>>() {
                }
        );
        trainingTypes.forEach(t -> trainingTypesData.getData().put(t.getId(), t));
        trainingTypesData.refreshNextId();

        log.info("Loaded {} training types", trainingTypesData.getData().size());
    }

    private void loadTrainerAssignments() throws IOException {
        if (pathToTrainerAssignments == null) {
            return;
        }

        log.info("Loading default training assignments...");

        var resource = new ClassPathResource(pathToTrainerAssignments);
        var trainerAssignments = objectMapper.readValue(
                resource.getInputStream(), new TypeReference<List<TrainerAssignmentSeed>>() {
                }
        );
        trainerAssignments.forEach(a -> {
            var trainee = getRequired(traineesData.getData(), a.traineeId(), "trainee");
            var trainer = getRequired(trainersData.getData(), a.trainerId(), "trainer");
            var trainerAssignment = Trainee2Trainer.builder().id(a.id()).trainee(trainee).trainer(trainer).build();

            trainee.getTrainerAssignments().add(trainerAssignment);
            trainer.getTraineeAssignments().add(trainerAssignment);
        });

        log.info("Loaded {} training assignments", trainerAssignments.size());
    }

    private void loadTrainings() throws IOException {
        if (pathToTrainings == null) {
            return;
        }

        log.info("Loading default trainings...");

        var resource = new ClassPathResource(pathToTrainings);
        var trainings = objectMapper.readValue(
                resource.getInputStream(), new TypeReference<List<TrainingSeed>>() {
                }
        );
        trainings.forEach(t -> {
            var trainee = getRequired(traineesData.getData(), t.traineeId(), "trainee");
            var trainer = getRequired(trainersData.getData(), t.trainerId(), "trainer");
            var training = Training.builder().id(t.id()).trainee(trainee).trainer(trainer).name(t.name()).type(
                    getRequired(trainingTypesData.getData(), t.typeId(), "training type")).datetime(
                            t.datetime()).duration(t.duration()).build();

            trainingsData.getData().put(training.getId(), training);
            trainee.getTrainings().add(training);
            trainer.getTrainings().add(training);
        });
        trainingsData.refreshNextId();

        log.info("Loaded {} trainings", trainingsData.getData().size());
    }

    private <T> T getRequired(Map<Long, T> data, Long id, String entityName) {
        var entity = data.get(id);
        if (entity == null) {
            throw new IllegalStateException("No " + entityName + " found for id " + id);
        }
        return entity;
    }

    private record TrainerSeed(
                               Long id,
                               User user,
                               Long specializationId
    ) {
    }

    private record TrainerAssignmentSeed(
                                         Long id,
                                         Long traineeId,
                                         Long trainerId
    ) {
    }

    private record TrainingSeed(
                                Long id,
                                Long traineeId,
                                Long trainerId,
                                String name,
                                Long typeId,
                                ZonedDateTime datetime,
                                Duration duration
    ) {
    }
}
