package com.epam.lenda.gymapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingAssignment;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TraineeRepository traineeRepository;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;
    @Autowired
    private TrainingAssignmentRepository trainingAssignmentRepository;
    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    void userRepository_findsAndChecksUserByUsername() {
        var user = entityManager.persistAndFlush(user("Alice", "Apple", "alice.apple"));
        entityManager.clear();

        assertThat(userRepository.findByUsername("alice.apple")).contains(user);
        assertThat(userRepository.existsByUsername("alice.apple")).isTrue();
        assertThat(userRepository.findByUsername("missing.user")).isEmpty();
        assertThat(userRepository.existsByUsername("missing.user")).isFalse();
    }

    @Test
    void traineeRepository_findsAndChecksTraineeByNestedUsername() {
        var trainee = entityManager.persistAndFlush(trainee("Tina", "Trainee", "tina.trainee"));
        entityManager.clear();

        assertThat(traineeRepository.findByUsername("tina.trainee")).contains(trainee);
        assertThat(traineeRepository.existsByUsername("tina.trainee")).isTrue();
        assertThat(traineeRepository.findByUsername("missing.trainee")).isEmpty();
        assertThat(traineeRepository.existsByUsername("missing.trainee")).isFalse();
    }

    @Test
    void trainerRepository_findsTrainersByNestedUsernameList() {
        var strength = entityManager.persistAndFlush(new TrainingType("Strength"));
        var trainer1 = entityManager.persistAndFlush(trainer("Tom", "Trainer", "tom.trainer", strength));
        var trainer2 = entityManager.persistAndFlush(trainer("Sara", "Trainer", "sara.trainer", strength));
        entityManager.persistAndFlush(trainer("Mira", "Trainer", "mira.trainer", strength));
        entityManager.clear();

        assertThat(trainerRepository.findByUsername("tom.trainer")).contains(trainer1);
        assertThat(trainerRepository.existsByUsername("tom.trainer")).isTrue();
        assertThat(trainerRepository.findByUsernames(List.of("tom.trainer", "sara.trainer"))).containsExactlyInAnyOrder(
                trainer1, trainer2);
    }

    @Test
    void trainingTypeRepository_findsByNameIgnoringCase() {
        var trainingType = entityManager.persistAndFlush(new TrainingType("Cardio"));
        entityManager.clear();

        assertThat(trainingTypeRepository.findByNameIgnoreCase("cArDiO")).contains(trainingType);
        assertThat(trainingTypeRepository.findByNameIgnoreCase("Yoga")).isEmpty();
    }

    @Test
    void trainingAssignmentRepository_findsAssignmentsAndUnassignedTrainers() {
        var strength = entityManager.persistAndFlush(new TrainingType("Strength"));
        var trainee = entityManager.persistAndFlush(trainee("Tina", "Trainee", "tina.trainee"));
        var assignedTrainer = entityManager.persistAndFlush(trainer("Tom", "Trainer", "tom.trainer", strength));
        var unassignedTrainer = entityManager.persistAndFlush(trainer("Sara", "Trainer", "sara.trainer", strength));
        var assignment = entityManager.persistAndFlush(new TrainingAssignment(trainee, assignedTrainer));
        entityManager.clear();

        assertThat(trainingAssignmentRepository.findByTraineeId(trainee.getId())).containsExactly(assignment);
        assertThat(trainingAssignmentRepository.findActiveTrainersNotAssignedToTrainee("tina.trainee")).containsExactly(
                unassignedTrainer);
    }

    @Test
    void trainingAssignmentRepository_deletesByTraineeIdAndTrainerIds() {
        var strength = entityManager.persistAndFlush(new TrainingType("Strength"));
        var trainee = entityManager.persistAndFlush(trainee("Tina", "Trainee", "tina.trainee"));
        var trainerToKeep = entityManager.persistAndFlush(trainer("Tom", "Trainer", "tom.trainer", strength));
        var trainerToDelete = entityManager.persistAndFlush(trainer("Sara", "Trainer", "sara.trainer", strength));
        var keptAssignment = entityManager.persistAndFlush(new TrainingAssignment(trainee, trainerToKeep));
        entityManager.persistAndFlush(new TrainingAssignment(trainee, trainerToDelete));

        trainingAssignmentRepository.deleteByTraineeIdAndTrainerIds(trainee.getId(), List.of(trainerToDelete.getId()));
        entityManager.flush();
        entityManager.clear();

        assertThat(trainingAssignmentRepository.findByTraineeId(trainee.getId())).containsExactly(keptAssignment);
    }

    @Test
    void trainingRepository_deletesByTraineeId() {
        var strength = entityManager.persistAndFlush(new TrainingType("Strength"));
        var trainee = entityManager.persistAndFlush(trainee("Tina", "Trainee", "tina.trainee"));
        var otherTrainee = entityManager.persistAndFlush(trainee("Olga", "Trainee", "olga.trainee"));
        var trainer = entityManager.persistAndFlush(trainer("Tom", "Trainer", "tom.trainer", strength));
        entityManager.persistAndFlush(training(trainee, trainer, strength, "Training to delete"));
        var remainingTraining = entityManager.persistAndFlush(training(otherTrainee, trainer, strength,
                "Training to keep"));

        trainingRepository.deleteByTraineeId(trainee.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(trainingRepository.findAll()).containsExactly(remainingTraining);
    }

    private static User user(String firstName, String lastName, String username) {
        return User.builder().firstName(firstName).lastName(lastName).username(username).password("password").isActive(
                true).build();
    }

    private static Trainee trainee(String firstName, String lastName, String username) {
        return Trainee.builder().user(user(firstName, lastName, username)).dateOfBirth(Date.valueOf(LocalDate.of(1990,
                1, 1))).address("Address").build();
    }

    private static Trainer trainer(String firstName, String lastName, String username, TrainingType specialization) {
        return Trainer.builder().user(user(firstName, lastName, username)).specialization(specialization).build();
    }

    private static Training training(Trainee trainee, Trainer trainer, TrainingType type, String name) {
        return Training.builder().trainee(trainee).trainer(trainer).name(name).type(type).datetime(Date.valueOf(
                LocalDate.of(2026, 7, 14))).durationMinutes(60).build();
    }
}
