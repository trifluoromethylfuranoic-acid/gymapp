package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.service.impl.TraineeServiceImpl;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
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
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private AuthService authService;

    @Autowired
    private TraineeService traineeService;

    @Test
    void create_createsActiveTrainee() {
        var dateOfBirth = LocalDate.of(1990, 1, 1);
        var address = "Address";

        when(authService.generateCredentials(any(), any())).thenReturn(new AuthService.Credentials("Alice.Apple",
                "password",
                "encoded-password"));

        traineeService.create("Alice", "Apple", dateOfBirth, address);

        var captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getIsActive()).isTrue();
        assertThat(captor.getValue().getDateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(captor.getValue().getAddress()).isEqualTo(address);
    }

    @Test
    void update_successWhenChangingUsername() {
        var trainee = Util.trainee(1, "old.username");
        var request = TraineeService.UpdateRequest.builder().username("new.username").password("new.password").isActive(
                false).firstName("NewFirst").lastName("NewLast").dateOfBirth(LocalDate.of(1995, 5, 14)).address(
                        "New address").build();

        when(traineeRepository.findByUsername("old.username")).thenReturn(Optional.of(trainee));
        when(traineeRepository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        var newTrainee = traineeService.update("old.username", request);

        assertThat(newTrainee.getUsername()).isEqualTo("new.username");
        assertThat(newTrainee.getPassword()).isEqualTo("encoded-password");
        assertThat(newTrainee.getFirstName()).isEqualTo("NewFirst");
        assertThat(newTrainee.getLastName()).isEqualTo("NewLast");
        assertThat(newTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 14));
        assertThat(newTrainee.getAddress()).isEqualTo("New address");
        assertThat(newTrainee.getIsActive()).isFalse();
    }

    @Test
    void update_successWhenNotChangingUsername() {
        var trainee = Util.trainee(1, "trainee.username");
        var request = TraineeService.UpdateRequest.builder().username("trainee.username").password(
                "new.password").isActive(
                        false).firstName("NewFirst").lastName("NewLast").dateOfBirth(LocalDate.of(1995, 5, 14)).address(
                                "New address").build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        var newTrainee = assertDoesNotThrow(() -> traineeService.update("trainee.username", request));
        assertThat(newTrainee.getUsername()).isEqualTo("trainee.username");
    }

    @Test
    void update_throwsOnInvalidUsername() {
        var trainee = Util.trainee(1, "trainee.username");
        var request = TraineeService.UpdateRequest.builder().username("new/username").password("new.password").isActive(
                false).firstName("NewFirst").lastName("NewLast").dateOfBirth(LocalDate.of(1995, 5, 14)).address(
                        "New address").build();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        assertThrows(ConstraintViolationException.class, () -> traineeService.update("trainee.username", request));
    }

    @Test
    void update_throwsOnDuplicateUsername() {
        var trainee = Util.trainee(1, "old.username");
        var trainee2 = Util.trainee(2, "existing.username");
        var request = TraineeService.UpdateRequest.builder().username("existing.username").password(
                "new.password").isActive(
                        false).firstName("NewFirst").lastName("NewLast").dateOfBirth(LocalDate.of(1995, 5, 14)).address(
                                "New address").build();

        when(traineeRepository.findByUsername("old.username")).thenReturn(Optional.of(trainee));
        when(traineeRepository.findByUsername("existing.username")).thenReturn(Optional.of(trainee2));

        assertThrows(DuplicateUsernameException.class, () -> traineeService.update("old.username", request));
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
        verify(trainingRepository).removeTraineeRefs(trainee.getId());
    }

    @Test
    void delete_ignoresMissingTrainee() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> traineeService.delete("missing"));
    }
}
