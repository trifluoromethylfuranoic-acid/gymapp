package com.epam.lenda.gymapp.init;

import com.epam.lenda.gymapp.model.*;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
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
    @Value("${application.data.init.file.trainings:#{null}}")
    private String pathToTrainings;

    private final MapBasedStorage<Trainee> traineesData;
    private final MapBasedStorage<Trainer> trainersData;
    private final MapBasedStorage<Training> trainingsData;

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void loadData() throws IOException {
        loadTrainees();
        loadTrainers();
        loadTrainings();
    }

    private <E extends HasId, S> void loadEntities(String path, String entityNamePlural,
                                                   TypeReference<List<S>> typeReference,
                                                   MapBasedStorage<E> data,
                                                   Function<S, E> converter) throws IOException {
        if (path == null) {
            return;
        }

        log.info("Loading default {}...", entityNamePlural);

        var resource = new ClassPathResource(path);
        var entities = objectMapper.readValue(
                resource.getInputStream(), typeReference
        );
        entities.forEach(seed -> data.save(converter.apply(seed)));

        log.info("Loaded {} {}", traineesData.getData().size(), entityNamePlural);
    }

    private void loadTrainees() throws IOException {
        loadEntities(pathToTrainees, "trainees", new TypeReference<>() {
        }, traineesData, Function.identity());
    }

    private void loadTrainers() throws IOException {
        loadEntities(pathToTrainers, "trainers", new TypeReference<>() {
        }, trainersData, Function.identity());
    }

    private void loadTrainings() throws IOException {
        loadEntities(pathToTrainings, "trainings", new TypeReference<>() {
        }, trainingsData, (TrainingSeed seed) -> {
            var trainee = getRequired(traineesData.getData(), seed.traineeId(), "trainee");
            var trainer = getRequired(trainersData.getData(), seed.trainerId(), "trainer");

            return Training.builder().id(seed.id()).trainee(trainee).trainer(trainer).name(seed.name()).type(
                    seed.type).datetime(seed.datetime()).duration(seed.duration()).build();
        });
    }

    private <T> T getRequired(Map<Long, T> data, Long id, String entityName) {
        var entity = data.get(id);
        if (entity == null) {
            throw new IllegalStateException("No " + entityName + " found for id " + id);
        }
        return entity;
    }

    @Jacksonized
    @Builder
    private record TrainingSeed(
                                Long id,
                                Long traineeId,
                                Long trainerId,
                                String name,
                                TrainingType type,
                                ZonedDateTime datetime,
                                Duration duration
    ) {
    }
}
