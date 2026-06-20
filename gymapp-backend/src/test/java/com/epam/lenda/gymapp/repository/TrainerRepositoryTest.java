package com.epam.lenda.gymapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.dto.trainer.SearchTrainerRequest;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.impl.TrainerRepositoryImpl;
import com.epam.lenda.gymapp.repository.impl.TrainingTypeRepositoryImpl;
import com.epam.lenda.gymapp.repository.impl.UserRepositoryImpl;
import com.epam.lenda.gymapp.util.MapBasedStorage;
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
public class TrainerRepositoryTest {
    private TrainerRepository trainerRepository;
    private UserRepository userRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private final MapBasedStorage<User> usersDataInitial = new MapBasedStorage<>();
    private final MapBasedStorage<Trainer> trainersDataInitial = new MapBasedStorage<>();
    private final MapBasedStorage<TrainingType> trainingTypesDataInitial = new MapBasedStorage<>();

    @BeforeAll
    public void setupInitialData() {
        Util.loadTrainers("trainers.csv", usersDataInitial, trainersDataInitial, trainingTypesDataInitial);
    }

    @BeforeEach
    public void setupRepositories() {
        userRepository = new UserRepositoryImpl(usersDataInitial.clone());
        trainingTypeRepository = new TrainingTypeRepositoryImpl(trainingTypesDataInitial.clone());
        trainerRepository = new TrainerRepositoryImpl(trainersDataInitial.clone(), userRepository,
                trainingTypeRepository);
    }

    @Test
    public void saveAndFindById_success() {
        var user = User.builder().firstName("Kekz").lastName("Lols").username("kekz.lols").password(
                "{noop}passss").isActive(true).build();
        var specialization = TrainingType.builder().name("dancing").build();
        var trainer = Trainer.builder().user(user).specialization(specialization).trainings(
                new ArrayList<>()).traineeAssignments(new ArrayList<>()).build();

        trainingTypeRepository.save(specialization);

        var saved = trainerRepository.save(trainer);
        var loadedOpt = trainerRepository.findById(saved.getId());

        assertTrue(loadedOpt.isPresent());
        assertEquals("kekz.lols", loadedOpt.get().getUser().getUsername());
    }

    @Test
    public void save_nullUser() {
        var trainer = Trainer.builder().trainings(new ArrayList<>()).traineeAssignments(new ArrayList<>()).build();

        assertThrows(IllegalStateException.class, () -> trainerRepository.save(trainer));
    }

    @Test
    public void findById_noSuchElement() {
        var loadedOpt = trainerRepository.findById(6969696);

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void findByUsername_success() {
        var loadedOpt = trainerRepository.findByUsername("alice.apple");

        assertTrue(loadedOpt.isPresent());
        assertEquals("alice.apple", loadedOpt.get().getUser().getUsername());
    }

    @Test
    public void findByUsername_noSuchElement() {
        var loadedOpt = trainerRepository.findByUsername("Virgil");

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void delete_success() {
        var username = "alice.apple";
        var loadedOpt = trainerRepository.findByUsername(username);

        assertTrue(loadedOpt.isPresent());
        trainerRepository.delete(loadedOpt.get());

        var loadedOpt2 = trainerRepository.findByUsername(username);
        assertTrue(loadedOpt2.isEmpty());
    }

    @Test
    public void delete_noSuchElement() {
        var id = 696969;
        trainerRepository.delete(id);
        var loadedOpt = trainerRepository.findById(id);

        assertTrue(loadedOpt.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("searchRequests")
    public void search_success(SearchTrainerRequest req, Pageable pageable, int nFound, int totalPages) {
        var page = trainerRepository.search(req, pageable);

        assertEquals(nFound, page.getTotalElements());
        assertEquals(totalPages, page.getTotalPages());
    }

    public Stream<Arguments> searchRequests() {
        return Stream.of(
                Arguments.of(new SearchTrainerRequest(
                        "i", null, null, null), null, 3, 1),

                Arguments.of(new SearchTrainerRequest(
                        null, "Zumb", null, null), null, 2, 1),

                Arguments.of(new SearchTrainerRequest(
                        null, null, null, null), Pageable.ofSize(1), 5, 5),

                Arguments.of(new SearchTrainerRequest(
                        "Dante", null, null, null), null, 0, 1)
        );

    }
}
