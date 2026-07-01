package com.epam.lenda.gymapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.config.DataConfig;
import com.epam.lenda.gymapp.init.DataInitializer;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.repository.impl.TraineeRepositoryImpl;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import java.time.LocalDate;
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
public class TraineeRepositoryTest {
    @Autowired
    private MapBasedStorage<Trainee> traineesData;

    private TraineeRepository traineeRepository;

    @BeforeEach
    public void setup() {
        traineeRepository = new TraineeRepositoryImpl(traineesData);
    }

    @Test
    public void saveAndFindById_success() {
        var trainee = Trainee.builder().firstName("Kekz").lastName("Lols").username("kekz.lols").password(
                "{noop}123").address("Kek St").dateOfBirth(LocalDate.of(1990, 11, 15)).isActive(true).build();

        var saved = traineeRepository.save(trainee);
        var loadedOpt = traineeRepository.findById(saved.getId());

        assertTrue(loadedOpt.isPresent());
        assertEquals("kekz.lols", loadedOpt.get().getUsername());
    }

    @Test
    public void save_nullAddress() {
        var trainee = Trainee.builder().firstName("Kekz").lastName("Lols").username("kekz.lols").password(
                "{noop}123").address(null).dateOfBirth(LocalDate.of(1990, 11, 15)).build();

        assertThrows(IllegalStateException.class, () -> traineeRepository.save(trainee));
    }

    @Test
    public void findById_noSuchElement() {
        var loadedOpt = traineeRepository.findById(6969696);

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void findByUsername_success() {
        var username = "john.smith";
        var loadedOpt = traineeRepository.findByUsername(username);

        assertTrue(loadedOpt.isPresent());
        assertEquals(username, loadedOpt.get().getUsername());
    }

    @Test
    public void findByUsername_noSuchElement() {
        var loadedOpt = traineeRepository.findByUsername("Virgil");

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void delete_success() {
        var username = "john.smith";
        var loadedOpt = traineeRepository.findByUsername(username);

        assertTrue(loadedOpt.isPresent());
        traineeRepository.delete(loadedOpt.get());

        var loadedOpt2 = traineeRepository.findByUsername(username);
        assertTrue(loadedOpt2.isEmpty());
    }

    @Test
    public void delete_noSuchElement() {
        var id = 696969;
        traineeRepository.deleteById(id);
        var loadedOpt = traineeRepository.findById(id);

        assertTrue(loadedOpt.isEmpty());
    }
}
