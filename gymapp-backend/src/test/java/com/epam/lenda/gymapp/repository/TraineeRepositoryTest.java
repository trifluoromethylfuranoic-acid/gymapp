package com.epam.lenda.gymapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.epam.lenda.gymapp.dto.trainee.SearchTraineeRequest;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.impl.TraineeRepositoryImpl;
import com.epam.lenda.gymapp.repository.impl.UserRepositoryImpl;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import java.time.LocalDate;
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
public class TraineeRepositoryTest {
    private TraineeRepository traineeRepository;
    private UserRepository userRepository;
    private final MapBasedStorage<User> usersDataInitial = new MapBasedStorage<>();
    private final MapBasedStorage<Trainee> traineesDataInitial = new MapBasedStorage<>();

    @BeforeAll
    public void setupInitialData() {
        Util.loadTrainees("trainees.csv", usersDataInitial, traineesDataInitial);
    }

    @BeforeEach
    public void setupRepositories() {
        userRepository = new UserRepositoryImpl(usersDataInitial.clone());
        traineeRepository = new TraineeRepositoryImpl(traineesDataInitial.clone(), userRepository);
    }

    @Test
    public void saveAndFindById_success() {
        var user = User.builder().firstName("Kekz").lastName("Lols").username("kekz.lols").password(
                "{noop}passss").isActive(true).build();
        var trainee = Trainee.builder().user(user).address("Kek St").dateOfBirth(LocalDate.of(1990, 11, 15)).trainings(
                new ArrayList<>()).trainerAssignments(new ArrayList<>()).build();

        var saved = traineeRepository.save(trainee);
        var loadedOpt = traineeRepository.findById(saved.getId());

        assertTrue(loadedOpt.isPresent());
        assertEquals("kekz.lols", loadedOpt.get().getUser().getUsername());
    }

    @Test
    public void save_nullUser() {
        var trainee = Trainee.builder().address("Kek St").dateOfBirth(LocalDate.of(1990, 11, 15)).trainings(
                new ArrayList<>()).trainerAssignments(new ArrayList<>()).build();

        assertThrows(IllegalStateException.class, () -> traineeRepository.save(trainee));
    }

    @Test
    public void findById_noSuchElement() {
        var loadedOpt = traineeRepository.findById(6969696);

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void findByUsername_success() {
        var loadedOpt = traineeRepository.findByUsername("alice.apple");

        assertTrue(loadedOpt.isPresent());
        assertEquals("alice.apple", loadedOpt.get().getUser().getUsername());
    }

    @Test
    public void findByUsername_noSuchElement() {
        var loadedOpt = traineeRepository.findByUsername("Virgil");

        assertTrue(loadedOpt.isEmpty());
    }

    @Test
    public void delete_success() {
        var username = "alice.apple";
        var loadedOpt = traineeRepository.findByUsername(username);

        assertTrue(loadedOpt.isPresent());
        traineeRepository.delete(loadedOpt.get());

        var loadedOpt2 = traineeRepository.findByUsername(username);
        assertTrue(loadedOpt2.isEmpty());
    }

    @Test
    public void delete_noSuchElement() {
        var id = 696969;
        traineeRepository.delete(id);
        var loadedOpt = traineeRepository.findById(id);

        assertTrue(loadedOpt.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("searchRequests")
    public void search_success(SearchTraineeRequest req, Pageable pageable, int nFound, int totalPages) {
        var page = traineeRepository.search(req, pageable);

        assertEquals(nFound, page.getTotalElements());
        assertEquals(totalPages, page.getTotalPages());
    }

    public Stream<Arguments> searchRequests() {
        return Stream.of(
                Arguments.of(new SearchTraineeRequest(
                        "i", LocalDate.of(1950, 1, 1), null, null, null, null), null, 2, 1),

                Arguments.of(new SearchTraineeRequest(
                        null, null, null, "Bottom", null, null), null, 1, 1),

                Arguments.of(new SearchTraineeRequest(
                        null, LocalDate.of(1950, 1, 1), LocalDate.of(2000, 1, 1), null, null, null), Pageable.ofSize(1),
                        2, 2),

                Arguments.of(new SearchTraineeRequest(
                        "Dante", null, null, null, null, null), null, 0, 1)
        );

    }
}
