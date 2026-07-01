package com.epam.lenda.gymapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.config.DataConfig;
import com.epam.lenda.gymapp.init.DataInitializer;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.impl.TrainerRepositoryImpl;
import com.epam.lenda.gymapp.util.MapBasedStorage;
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
public class TrainerRepositoryTest {
    @Autowired
    private MapBasedStorage<Trainer> trainersData;

    private TrainerRepository trainerRepository;

    @BeforeEach
    public void setup() {
        trainerRepository = new TrainerRepositoryImpl(trainersData);
    }

    @Test
    public void saveAndFindById_success() {
        var trainer = Trainer.builder().firstName("Kekz").lastName("Lols").username("kekz.lols").password(
                "{noop}123").specialization(TrainingType.YOGA).isActive(true).build();

        var saved = trainerRepository.save(trainer);
        var loadedOpt = trainerRepository.findById(saved.getId());

        assertTrue(loadedOpt.isPresent());
        assertEquals("kekz.lols", loadedOpt.get().getUsername());
    }

    @Test
    public void save_nullFirstName() {
        var trainer = Trainer.builder().firstName(null).lastName("Lols").username("kekz.lols").password(
                "{noop}123").specialization(TrainingType.YOGA).build();

        assertThrows(IllegalStateException.class, () -> trainerRepository.save(trainer));
    }

    @Test
    public void findById_noSuchElement() {
        var loadedOpt = trainerRepository.findById(6969696);

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void findByUsername_success() {
        var username = "sarah.connor";
        var loadedOpt = trainerRepository.findByUsername(username);

        assertTrue(loadedOpt.isPresent());
        assertEquals(username, loadedOpt.get().getUsername());
    }

    @Test
    public void findByUsername_noSuchElement() {
        var loadedOpt = trainerRepository.findByUsername("Virgil");

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void delete_success() {
        var username = "sarah.connor";
        var loadedOpt = trainerRepository.findByUsername(username);

        assertTrue(loadedOpt.isPresent());
        trainerRepository.delete(loadedOpt.get());

        var loadedOpt2 = trainerRepository.findByUsername(username);
        assertTrue(loadedOpt2.isEmpty());
    }

    @Test
    public void delete_noSuchElement() {
        var id = 696969;
        trainerRepository.deleteById(id);
        var loadedOpt = trainerRepository.findById(id);

        assertTrue(loadedOpt.isEmpty());
    }
}
