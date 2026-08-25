package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.TestConfig;
import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.dto.request.CreateTraineeRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTraineeRequest;
import com.epam.lenda.gymapp.exception.IllegalStateTransitionException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingAssignment;
import com.epam.lenda.gymapp.repository.*;
import com.epam.lenda.gymapp.service.impl.TraineeServiceImpl;
import jakarta.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
@Import({TestConfig.class, TraineeServiceImpl.class})
class TraineeServiceTest {
    @MockitoBean
    private TraineeRepository traineeRepository;
    @MockitoBean
    private TrainerRepository trainerRepository;
    @MockitoBean
    private TrainingRepository trainingRepository;
    @MockitoBean
    private TrainingAssignmentRepository trainingAssignmentRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private CredentialsService credentialsService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockitoBean
    private com.epam.lenda.gymapp.service.impl.TrainingReportNotifier trainingReportNotifier;
    @MockitoBean
    private Clock clock;

    @Autowired
    private TraineeService traineeService;

    @BeforeEach
    void stubClock() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void create_createsActiveTrainee() {
        var dateOfBirth = LocalDate.of(1990, 1, 1);
        var address = "Address";

        when(credentialsService.generateCredentials(any())).thenReturn(
                new CredentialsService.Credentials("Alice.Apple", "password", "encoded-password"));

        traineeService.create(CreateTraineeRequest.builder().firstName("Alice").lastName("Apple").dateOfBirth(dateOfBirth).address(address).build());

        var captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUser().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
        assertThat(captor.getValue().getDateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(captor.getValue().getAddress()).isEqualTo(address);
    }

    @Test
    void getTrainerList_success() {
        var trainee = Util.trainee("trainee.username");
        var trainers = List.of(Util.trainer("trainer.1"), Util.trainer("trainer.2"));
        var assignments = trainers.stream().map(trainer -> new TrainingAssignment(trainee, trainer)).toList();

        when(traineeRepository.findByUsername(trainee.getUser().getUsername())).thenReturn(Optional.of(trainee));
        when(trainingAssignmentRepository.findByTraineeId(trainee.getId())).thenReturn(assignments);

        var trainersReturned = traineeService.getTrainerList(trainee.getUser().getUsername());

        assertThat(trainersReturned).containsExactlyInAnyOrderElementsOf(trainers);
    }

    @Test
    void update_success() {
        var trainee = Util.trainee("trainee.username");
        var request = UpdateTraineeRequest.builder().firstName("NewFirst").lastName("NewLast").dateOfBirth(
                LocalDate.of(1995, 5, 14)).address("New address").isActive(true).build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        var newTrainee = traineeService.update("trainee.username", request);

        assertThat(newTrainee.getUser().getUsername()).isEqualTo("trainee.username");
        assertThat(newTrainee.getUser().getFirstName()).isEqualTo("NewFirst");
        assertThat(newTrainee.getUser().getLastName()).isEqualTo("NewLast");
        assertThat(newTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 14));
        assertThat(newTrainee.getAddress()).isEqualTo("New address");
    }

    @Test
    void update_throwsOnInvalidFirstName() {
        var trainee = Util.trainee("trainee.username");
        var request = UpdateTraineeRequest.builder().firstName("invalid/name").lastName("NewLast").dateOfBirth(
                LocalDate.of(1995, 5, 14)).address("New address").build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        assertThrows(ConstraintViolationException.class, () -> traineeService.update("trainee.username", request));
    }

    @Test
    void updateActiveStatus_success() {
        var trainee = Util.trainee("trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        var updated = traineeService.updateActiveStatus("trainee.username", false);

        assertThat(updated).isSameAs(trainee);
        assertThat(trainee.getUser().getIsActive()).isFalse();
    }

    @Test
    void updateActiveStatus_throwsWhenSameStatus() {
        var trainee = Util.trainee("trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateTransitionException.class,
                () -> traineeService.updateActiveStatus("trainee.username", true));
    }

    @Test
    void toggleActivation_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateActiveStatus("missing", false)).isInstanceOf(
                ResourceNotFoundException.class);
    }

    @Test
    void updateTrainerList_addsAndRemovesAssignments() {
        var trainee = Util.trainee("trainee.username");
        var keptTrainer = Util.trainer("kept.trainer");
        var addedTrainer = Util.trainer("added.trainer");
        var removedTrainer = Util.trainer("removed.trainer");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(List.of("kept.trainer", "added.trainer"))).thenReturn(
                List.of(keptTrainer, addedTrainer));
        when(trainingAssignmentRepository.findByTraineeId(trainee.getId())).thenReturn(
                List.of(new TrainingAssignment(trainee, keptTrainer), new TrainingAssignment(trainee, removedTrainer)));

        var trainers = traineeService.updateTrainerList("trainee.username", List.of("kept.trainer", "added.trainer"));

        assertThat(trainers).containsExactly(keptTrainer, addedTrainer);
        verify(trainingAssignmentRepository).deleteByTraineeIdAndTrainerIds(eq(trainee.getId()),
                argThat(trainerIds -> trainerIds.size() == 1 && trainerIds.contains(
                        removedTrainer.getId())));

        var captor = ArgumentCaptor.forClass(Iterable.class);
        verify(trainingAssignmentRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(
                assignment -> ((TrainingAssignment) assignment).getTrainer()).containsExactly(addedTrainer);
    }

    @Test
    void updateTrainerList_throwsWhenAnyTrainerDoesNotExist() {
        var trainee = Util.trainee("trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(List.of("existing.trainer", "missing.trainer"))).thenReturn(
                List.of(Util.trainer("existing.trainer")));

        assertThatThrownBy(() -> traineeService.updateTrainerList("trainee.username", List.of("existing.trainer",
                "missing.trainer"))).isInstanceOf(
                        ResourceNotFoundException.class);
        verify(trainingAssignmentRepository, never()).findByTraineeId(any());
    }

    @Test
    void findByUsername_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findByUsername("missing")).isInstanceOf(
                ResourceNotFoundException.class);
    }

    @Test
    void delete_deletesFutureTrainingsAndOrphansPastOnes() {
        var trainee = Util.trainee("trainee.username");
        var now = ZonedDateTime.now(ZoneOffset.UTC);
        var futureTraining = Training.builder().trainee(trainee).trainer(Util.trainer("future.trainer")).name(
                "Future").type(Util.trainingType("Fitness")).datetime(now.plusDays(5)).durationMinutes(
                        60).build();
        var pastTraining = Training.builder().trainee(trainee).trainer(Util.trainer("past.trainer")).name(
                "Past").type(Util.trainingType("Fitness")).datetime(now.minusDays(5)).durationMinutes(
                        45).build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainingRepository.findByTraineeId(trainee.getId())).thenReturn(List.of(futureTraining, pastTraining));

        traineeService.delete("trainee.username");

        verify(traineeRepository).delete(trainee);
        verify(trainingRepository).deleteAll(List.of(futureTraining));
        verify(trainingAssignmentRepository).deleteByTraineeId(trainee.getId());
        verify(refreshTokenRepository).deleteByUsername(trainee.getUser().getUsername());

        assertThat(pastTraining.getTrainee()).isNull();
        var orphanCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(trainingRepository).saveAll(orphanCaptor.capture());
        assertThat(orphanCaptor.getValue()).containsExactly(pastTraining);

        var eventCaptor = ArgumentCaptor.forClass(
                com.epam.lenda.gymapp.dto.event.TrainingActionEvent.class);
        verify(trainingReportNotifier, times(1)).notify(eventCaptor.capture());
        assertThat(eventCaptor.getValue().action()).isEqualTo(
                com.epam.lenda.gymapp.dto.event.TrainingActionEvent.Action.DELETE);
        assertThat(eventCaptor.getValue().trainer().username()).isEqualTo("future.trainer");
    }

    @Test
    void delete_throwsOnMissingTrainee() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> traineeService.delete("missing"));
    }

    @Test
    void findActiveTrainersNotAssignedToTrainee_returnsRepositoryResult() {
        var trainer1 = Util.trainer("trainer.one");
        var trainer2 = Util.trainer("trainer.two");
        var trainee = Util.trainee("trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainingAssignmentRepository.findActiveTrainersNotAssignedToTrainee("trainee.username")).thenReturn(
                List.of(trainer1, trainer2));

        var trainers = traineeService.findActiveTrainersNotAssignedToTrainee("trainee.username");

        assertThat(trainers).containsExactly(trainer1, trainer2);
        verify(trainingAssignmentRepository).findActiveTrainersNotAssignedToTrainee("trainee.username");
    }
}
