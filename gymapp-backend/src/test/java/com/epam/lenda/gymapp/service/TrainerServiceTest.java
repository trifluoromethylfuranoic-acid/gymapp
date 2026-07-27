package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.dto.request.CreateTrainerRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTrainerRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingAssignment;
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
        when(credentialsService.generateCredentials(any())).thenReturn(
                new CredentialsService.Credentials("Alice.Apple", "password", "encoded-password"));
        when(trainingTypeRepository.findByNameIgnoreCase("Yoga")).thenReturn(Optional.of(yoga));

        trainerService.create(
                CreateTrainerRequest.builder().firstName("Alice").lastName("Apple").specialization("Yoga").build());

        var captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUser().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
        assertThat(captor.getValue().getSpecialization()).isEqualTo(yoga);
    }

    @Test
    void getTraineeList_success() {
        var trainer = Util.trainer("trainer.username");
        var trainees = List.of(Util.trainee("trainee.1"), Util.trainee("trainee.2"));
        var assignments = trainees.stream().map(trainee -> new TrainingAssignment(trainee, trainer)).toList();

        when(trainerRepository.findByUsername(trainer.getUser().getUsername())).thenReturn(Optional.of(trainer));
        when(trainingAssignmentRepository.findByTrainerId(trainer.getId())).thenReturn(assignments);

        var traineesReturned = trainerService.getTraineeList(trainer.getUser().getUsername());

        assertThat(traineesReturned).containsExactlyInAnyOrderElementsOf(trainees);
    }

    @Test
    void update_success() {
        var trainer = Util.trainer("trainer.username");
        var request = UpdateTrainerRequest.builder().firstName("NewFirst").lastName("NewLast").isActive(true).build();

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var newTrainee = trainerService.update("trainer.username", request);

        assertThat(newTrainee.getUser().getUsername()).isEqualTo("trainer.username");
        assertThat(newTrainee.getUser().getFirstName()).isEqualTo("NewFirst");
        assertThat(newTrainee.getUser().getLastName()).isEqualTo("NewLast");
    }

    @Test
    void update_throwsOnInvalidFirstName() {
        var trainer = Util.trainer("trainer.username");
        var request = UpdateTrainerRequest.builder().firstName("invalid/name").lastName("NewLast").build();

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        assertThrows(ConstraintViolationException.class, () -> trainerService.update("trainer.username", request));
    }

    @Test
    void findByUsername_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findByUsername("missing")).isInstanceOf(
                ResourceNotFoundException.class);
    }
}
