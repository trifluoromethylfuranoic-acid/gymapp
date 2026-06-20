package com.epam.lenda.gymapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.impl.TrainingRepositoryImpl;
import com.epam.lenda.gymapp.repository.impl.TrainingTypeRepositoryImpl;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TrainingRepositoryTest {
    private TrainingRepository trainingRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private final MapBasedStorage<Training> trainingsDataInitial = new MapBasedStorage<>();
    private final MapBasedStorage<TrainingType> trainingTypesDataInitial = new MapBasedStorage<>();

    private Trainee aliceTrainee;
    private Trainee bobTrainee;
    private Trainer aliceTrainer;
    private Trainer charlieTrainer;
    private TrainingType fitness;
    private TrainingType yoga;

    @BeforeAll
    public void setupInitialData() {
        fitness = TrainingType.builder().id(trainingTypesDataInitial.nextId()).name("fitness").build();
        yoga = TrainingType.builder().id(trainingTypesDataInitial.nextId()).name("yoga").build();
        trainingTypesDataInitial.getData().put(fitness.getId(), fitness);
        trainingTypesDataInitial.getData().put(yoga.getId(), yoga);

        aliceTrainee = trainee(1L, "Alice", "Apple", "alice.trainee");
        bobTrainee = trainee(2L, "Bob", "Barron", "bob.trainee");
        aliceTrainer = trainer(1L, "Alice", "Trainer", "alice.trainer", fitness);
        charlieTrainer = trainer(2L, "Charlie", "Cart", "charlie.trainer", yoga);

        addTraining(1L, aliceTrainee, aliceTrainer, "Morning Fitness", fitness, ZonedDateTime.of(2026, 7, 1, 10, 0, 0,
                0, ZoneId.of("Europe/Kiev")), Duration.ofHours(1));
        addTraining(2L, bobTrainee, charlieTrainer, "Evening Yoga", yoga, ZonedDateTime.of(2026, 7, 2, 18, 30, 0, 0,
                ZoneId.of("Europe/Kiev")), Duration.ofMinutes(45));
        addTraining(3L, aliceTrainee, charlieTrainer, "Advanced Yoga", yoga, ZonedDateTime.of(2026, 7, 3, 8, 0, 0, 0,
                ZoneId.of("Europe/Kiev")), Duration.ofMinutes(75));
    }

    @BeforeEach
    public void setupRepositories() {
        trainingTypeRepository = new TrainingTypeRepositoryImpl(trainingTypesDataInitial.clone());
        trainingRepository = new TrainingRepositoryImpl(trainingsDataInitial.clone(), trainingTypeRepository);
    }

    @Test
    public void saveAndFindById_success() {
        var training = Training.builder().trainee(aliceTrainee).trainer(aliceTrainer).name("New Training").type(
                fitness).datetime(ZonedDateTime.of(2026, 7, 4, 9, 0, 0, 0, ZoneId.of("Europe/Kiev"))).duration(
                        Duration.ofMinutes(30)).build();

        var saved = trainingRepository.save(training);
        var loadedOpt = trainingRepository.findById(saved.getId());

        assertTrue(loadedOpt.isPresent());
        assertEquals("New Training", loadedOpt.get().getName());
    }

    @Test
    public void findById_noSuchElement() {
        var loadedOpt = trainingRepository.findById(6969696);

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void delete_success() {
        var loadedOpt = trainingRepository.findById(1L);

        assertTrue(loadedOpt.isPresent());
        trainingRepository.delete(loadedOpt.get());

        var loadedOpt2 = trainingRepository.findById(1L);
        assertTrue(loadedOpt2.isEmpty());
    }

    @Test
    public void delete_noSuchElement() {
        var id = 696969;
        trainingRepository.delete(id);
        var loadedOpt = trainingRepository.findById(id);

        assertTrue(loadedOpt.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("searchRequests")
    public void search_success(SearchTrainingRequest req, Pageable pageable, int nFound, int totalPages) {
        var page = trainingRepository.searchForTrainee(req, pageable);

        assertEquals(nFound, page.getTotalElements());
        assertEquals(totalPages, page.getTotalPages());
    }

    public Stream<Arguments> searchRequests() {
        return Stream.of(
                Arguments.of(SearchTrainingRequest.builder().traineeQuery("alice").build(), null, 2, 1), Arguments.of(
                        SearchTrainingRequest.builder().trainerQuery("charlie").trainingTypeQuery("yog").build(), null,
                        2, 1), Arguments.of(SearchTrainingRequest.builder().nameQuery("morning").datetimeMin(
                                ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/Kiev"))).dateTimeMax(
                                        ZonedDateTime.of(2026, 7, 1, 23, 59, 0, 0, ZoneId.of(
                                                "Europe/Kiev"))).durationMin(Duration.ofMinutes(45)).durationMax(
                                                        Duration.ofMinutes(75)).build(), null, 1, 1), Arguments.of(
                                                                SearchTrainingRequest.builder().build(),
                                                                Pageable.ofSize(2), 3, 2), Arguments.of(
                                                                        SearchTrainingRequest.builder().nameQuery(
                                                                                "missing").build(), null, 0, 1)
        );
    }

    private void addTraining(Long id, Trainee trainee, Trainer trainer, String name, TrainingType type,
                             ZonedDateTime datetime, Duration duration) {
        var training = Training.builder().id(id).trainee(trainee).trainer(trainer).name(name).type(type).datetime(
                datetime).duration(duration).build();
        trainingsDataInitial.getData().put(id, training);
        trainingsDataInitial.refreshNextId();
    }

    private static Trainee trainee(Long id, String firstName, String lastName, String username) {
        return Trainee.builder().id(id).user(User.builder().id(id).firstName(firstName).lastName(lastName).username(
                username).isActive(true).build()).address("Address").dateOfBirth(LocalDate.of(1990, 1,
                        1)).trainerAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
    }

    private static Trainer trainer(Long id, String firstName, String lastName, String username,
                                   TrainingType specialization) {
        return Trainer.builder().id(id).user(User.builder().id(id + 10).firstName(firstName).lastName(
                lastName).username(username).isActive(true).build()).specialization(specialization).traineeAssignments(
                        new ArrayList<>()).trainings(new ArrayList<>()).build();
    }
}
