package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.TrainingAssignment;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingAssignmentRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.service.impl.TraineeServiceImpl;
import jakarta.validation.ConstraintViolationException;
import java.sql.Date;
import java.time.LocalDate;
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

    @Autowired
    private TraineeService traineeService;

    @Test
    void create_createsActiveTrainee() {
        var dateOfBirth = LocalDate.of(1990, 1, 1);
        var address = "Address";

        when(credentialsService.generateCredentials(any())).thenReturn(new CredentialsService.Credentials("Alice.Apple",
                "password",
                "encoded-password"));

        traineeService.create(TraineeService.TraineeCreateRequest.builder().firstName("Alice").lastName(
                "Apple").dateOfBirth(Date.valueOf(dateOfBirth)).address(address).build());

        var captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUser().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
        assertThat(captor.getValue().getDateOfBirth()).isEqualTo(Date.valueOf(dateOfBirth));
        assertThat(captor.getValue().getAddress()).isEqualTo(address);
    }

    @Test
    void update_successWhenChangingUsername() {
        var trainee = Util.trainee(1, "old.username");
        var request = TraineeService.TraineeUpdateRequest.builder().username("new.username").firstName(
                "NewFirst").lastName("NewLast").dateOfBirth(Date.valueOf(LocalDate.of(1995, 5, 14))).address(
                        "New address").build();

        when(traineeRepository.findByUsername("old.username")).thenReturn(Optional.of(trainee));

        var newTrainee = traineeService.update("old.username", request);

        assertThat(newTrainee.getUser().getUsername()).isEqualTo("new.username");
        assertThat(newTrainee.getUser().getFirstName()).isEqualTo("NewFirst");
        assertThat(newTrainee.getUser().getLastName()).isEqualTo("NewLast");
        assertThat(newTrainee.getDateOfBirth()).isEqualTo(Date.valueOf(LocalDate.of(1995, 5, 14)));
        assertThat(newTrainee.getAddress()).isEqualTo("New address");
    }

    @Test
    void update_successWhenNotChangingUsername() {
        var trainee = Util.trainee(1, "trainee.username");
        var request = TraineeService.TraineeUpdateRequest.builder().username("trainee.username").firstName(
                "NewFirst").lastName("NewLast").dateOfBirth(Date.valueOf(LocalDate.of(1995, 5, 14))).address(
                        "New address").build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        var newTrainee = assertDoesNotThrow(() -> traineeService.update("trainee.username", request));
        assertThat(newTrainee.getUser().getUsername()).isEqualTo("trainee.username");
    }

    @Test
    void update_throwsOnInvalidUsername() {
        var trainee = Util.trainee(1, "trainee.username");
        var request = TraineeService.TraineeUpdateRequest.builder().username("new/username").firstName(
                "NewFirst").lastName("NewLast").dateOfBirth(Date.valueOf(LocalDate.of(1995, 5, 14))).address(
                        "New address").build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        assertThrows(ConstraintViolationException.class, () -> traineeService.update("trainee.username", request));
    }

    @Test
    void update_throwsOnDuplicateUsername() {
        var trainee = Util.trainee(1, "old.username");
        var request = TraineeService.TraineeUpdateRequest.builder().username("existing.username").firstName(
                "NewFirst").lastName("NewLast").dateOfBirth(Date.valueOf(LocalDate.of(1995, 5, 14))).address(
                        "New address").build();

        when(traineeRepository.findByUsername("old.username")).thenReturn(Optional.of(trainee));

        when(credentialsService.isUsernameTaken(eq("existing.username"), any())).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> traineeService.update("old.username", request));
    }

    @Test
    void updatePassword_authenticatesAndChangesPassword() {
        var trainee = Util.trainee(1, "trainee.username");
        var request = new BaseUserService.PasswordChangeRequest("old-password", "new-password");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(credentialsService.encodePassword("new-password")).thenReturn("encoded-new-password");

        var updated = traineeService.updatePassword("trainee.username", request);

        var authenticationCaptor = ArgumentCaptor.forClass(AuthService.AuthenticationRequest.class);
        verify(authService).requireAuthentication(authenticationCaptor.capture());
        assertThat(authenticationCaptor.getValue().getUsername()).isEqualTo("trainee.username");
        assertThat(authenticationCaptor.getValue().getPassword()).isEqualTo("old-password");
        assertThat(updated).isSameAs(trainee);
        assertThat(trainee.getUser().getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    void updatePassword_doesNotChangePasswordWhenAuthenticationFails() {
        var request = new BaseUserService.PasswordChangeRequest("wrong-password", "new-password");

        doThrow(new RuntimeException("bad credentials")).when(authService).requireAuthentication(any(
                AuthService.AuthenticationRequest.class));

        assertThatThrownBy(() -> traineeService.updatePassword("trainee.username", request)).isInstanceOf(
                RuntimeException.class);
        verify(traineeRepository, never()).findByUsername("trainee.username");
        verify(credentialsService, never()).encodePassword(any());
    }

    @Test
    void toggleActivation_flipsActiveFlag() {
        var trainee = Util.trainee(1, "trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        var updated = traineeService.toggleActivation("trainee.username");

        assertThat(updated).isSameAs(trainee);
        assertThat(trainee.getUser().getIsActive()).isFalse();
    }

    @Test
    void toggleActivation_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.toggleActivation("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTrainerList_addsAndRemovesAssignments() {
        var trainee = Util.trainee(1, "trainee.username");
        var keptTrainer = Util.trainer(2, "kept.trainer");
        var addedTrainer = Util.trainer(3, "added.trainer");
        var removedTrainer = Util.trainer(4, "removed.trainer");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(List.of("kept.trainer", "added.trainer"))).thenReturn(List.of(
                keptTrainer, addedTrainer));
        when(trainingAssignmentRepository.findByTraineeId(trainee.getId())).thenReturn(List.of(new TrainingAssignment(
                trainee, keptTrainer),
                new TrainingAssignment(trainee, removedTrainer)));

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
        var trainee = Util.trainee(1, "trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(List.of("existing.trainer", "missing.trainer"))).thenReturn(List.of(
                Util.trainer(2, "existing.trainer")));

        assertThatThrownBy(() -> traineeService.updateTrainerList("trainee.username",
                List.of("existing.trainer", "missing.trainer"))).isInstanceOf(ResourceNotFoundException.class);
        verify(trainingAssignmentRepository, never()).findByTraineeId(any());
    }

    @Test
    void findByUsername_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findByUsername("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_clearsReferencesAndDeletesEntity() {
        var trainee = Util.trainee(1, "trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        traineeService.delete("trainee.username");

        verify(traineeRepository).delete(trainee);
        verify(trainingRepository).deleteByTraineeId(trainee.getId());
    }

    @Test
    void delete_ignoresMissingTrainee() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> traineeService.delete("missing"));
    }
}
