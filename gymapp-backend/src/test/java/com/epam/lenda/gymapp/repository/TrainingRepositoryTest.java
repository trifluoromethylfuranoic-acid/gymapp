package com.epam.lenda.gymapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.config.DataConfig;
import com.epam.lenda.gymapp.init.DataInitializer;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.impl.TrainingRepositoryImpl;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({TestConfig.class, DataConfig.class, DataInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TrainingRepositoryTest {
    @Autowired
    private MapBasedStorage<Training> trainingsData;

    private TrainingRepository trainingRepository;

    @BeforeEach
    public void setup() {
        trainingRepository = new TrainingRepositoryImpl(trainingsData);
    }

    @Test
    public void saveAndFindById_success() {
        var name = "New Training";
        var training = training(name);

        var saved = trainingRepository.save(training);
        var loadedOpt = trainingRepository.findById(saved.getId());

        assertTrue(loadedOpt.isPresent());
        assertEquals(name, loadedOpt.get().getName());
    }

    @Test
    public void save_nullName() {
        var training = training(null);

        assertThrows(IllegalStateException.class, () -> trainingRepository.save(training));
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
        trainingRepository.deleteById(id);
        var loadedOpt = trainingRepository.findById(id);

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void removeTraineeRefs_success() {
        var training = training("training");
        var trainee = trainee("trainee");
        training.setTrainee(trainee);

        trainingRepository.save(training);

        trainingRepository.removeTraineeRefs(trainee.getId());

        assertNull(training.getTrainee());
    }

    private Training training(String name) {
        return Training.builder().trainee(null).trainer(null).name(name).type(TrainingType.STRENGTH).datetime(
                ZonedDateTime.of(2026, 7, 4, 9, 0, 0, 0, ZoneId.of("Europe/Kiev"))).duration(Duration.ofMinutes(
                        30)).build();
    }

    private static Trainee trainee(String username) {
        return Trainee.builder().id(1L).username(username).firstName("First").lastName(
                "Last").isActive(true).dateOfBirth(LocalDate.of(1990, 1, 1)).address(
                        "Address").build();
    }
}
