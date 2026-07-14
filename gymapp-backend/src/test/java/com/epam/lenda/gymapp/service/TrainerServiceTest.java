package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingAssignmentRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.impl.TrainerServiceImpl;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({TestConfig.class, TrainerServiceImpl.class})
class TrainerServiceTest {
    @MockitoBean
    private TraineeRepository traineeRepository;
    @MockitoBean
    private TrainerRepository trainerRepository;
    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;
    @MockitoBean
    private TrainingAssignmentRepository trainingAssignmentRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private CredentialsService credentialsService;
    @MockitoBean
    private AuthService authService;

    @Autowired
    private TrainerService trainerService;

    @Test
    void create_createsActiveTrainer() {
        var yoga = Util.trainingType("Yoga");
        when(credentialsService.generateCredentials(any())).thenReturn(new CredentialsService.Credentials("Alice.Apple",
                "password",
                "encoded-password"));
        when(trainingTypeRepository.findByNameIgnoreCase("Yoga")).thenReturn(Optional.of(yoga));

        trainerService.create(TrainerService.TrainerCreateRequest.builder().firstName("Alice").lastName(
                "Apple").specialization("Yoga").build());

        var captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUser().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
        assertThat(captor.getValue().getSpecialization()).isEqualTo(yoga);
    }

    @Test
    void update_successWhenChangingUsername() {
        var trainer = Util.trainer(1, "old.username");
        var yoga = Util.trainingType("Yoga");
        var request = TrainerService.TrainerUpdateRequest.builder().username("new.username").firstName(
                "NewFirst").lastName("NewLast").specialization("Yoga").build();

        when(trainerRepository.findByUsername("old.username")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByNameIgnoreCase("Yoga")).thenReturn(Optional.of(yoga));

        var newTrainee = trainerService.update("old.username", request);

        assertThat(newTrainee.getUser().getUsername()).isEqualTo("new.username");
        assertThat(newTrainee.getUser().getFirstName()).isEqualTo("NewFirst");
        assertThat(newTrainee.getUser().getLastName()).isEqualTo("NewLast");
        assertThat(newTrainee.getSpecialization()).isEqualTo(yoga);
    }

    @Test
    void update_successWhenNotChangingUsername() {
        var trainer = Util.trainer(1, "trainer.username");
        var yoga = Util.trainingType("Yoga");
        var request = TrainerService.TrainerUpdateRequest.builder().username("trainer.username").firstName(
                "NewFirst").lastName("NewLast").specialization("Yoga").build();


        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByNameIgnoreCase("Yoga")).thenReturn(Optional.of(yoga));

        var newTrainer = assertDoesNotThrow(() -> trainerService.update("trainer.username", request));
        assertThat(newTrainer.getUser().getUsername()).isEqualTo("trainer.username");
    }

    @Test
    void update_throwsOnInvalidUsername() {
        var trainer = Util.trainer(1, "trainer.username");
        var request = TrainerService.TrainerUpdateRequest.builder().username("new/username").firstName(
                "NewFirst").lastName("NewLast").specialization("Yoga").build();

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByNameIgnoreCase("Yoga")).thenReturn(Optional.of(Util.trainingType("Yoga")));

        assertThrows(ConstraintViolationException.class, () -> trainerService.update("trainer.username", request));
    }

    @Test
    void update_throwsOnDuplicateUsername() {
        var trainer = Util.trainer(1, "old.username");
        var request = TrainerService.TrainerUpdateRequest.builder().username("existing.username").firstName(
                "NewFirst").lastName("NewLast").specialization("Yoga").build();

        when(trainerRepository.findByUsername("old.username")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByNameIgnoreCase("Yoga")).thenReturn(Optional.of(Util.trainingType("Yoga")));

        when(credentialsService.isUsernameTaken(eq("existing.username"), any())).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> trainerService.update("old.username", request));
    }

    @Test
    void findByUsername_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findByUsername("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findNotAssignedToTrainee_returnsRepositoryResult() {
        var trainer1 = Util.trainer(1, "trainer.one");
        var trainer2 = Util.trainer(2, "trainer.two");

        when(trainingAssignmentRepository.findTrainersNotAssignedToTrainee("trainee.username")).thenReturn(List.of(
                trainer1, trainer2));

        var trainers = trainerService.findNotAssignedToTrainee("trainee.username");

        assertThat(trainers).containsExactly(trainer1, trainer2);
        verify(trainingAssignmentRepository).findTrainersNotAssignedToTrainee("trainee.username");
    }
}
