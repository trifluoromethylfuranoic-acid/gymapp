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
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.service.impl.TrainerServiceImpl;
import jakarta.validation.ConstraintViolationException;
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
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private AuthService authService;

    @Autowired
    private TrainerService trainerService;

    @Test
    void create_createsActiveTrainer() {
        when(authService.generateCredentials(any(), any())).thenReturn(new AuthService.Credentials("Alice.Apple",
                                                                                                   "password",
                                                                                                   "encoded-password"));

        trainerService.create("Alice", "Apple", TrainingType.YOGA);

        var captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getIsActive()).isTrue();
        assertThat(captor.getValue().getSpecialization()).isEqualTo(TrainingType.YOGA);
    }

    @Test
    void update_successWhenChangingUsername() {
        var trainer = Util.trainer(1, "old.username");
        var request = TrainerService.UpdateRequest.builder().username("new.username").password("new.password").isActive(
                false).firstName("NewFirst").lastName("NewLast").specialization(TrainingType.YOGA).build();

        when(trainerRepository.findByUsername("old.username")).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        var newTrainee = trainerService.update("old.username", request);

        assertThat(newTrainee.getUsername()).isEqualTo("new.username");
        assertThat(newTrainee.getPassword()).isEqualTo("encoded-password");
        assertThat(newTrainee.getFirstName()).isEqualTo("NewFirst");
        assertThat(newTrainee.getLastName()).isEqualTo("NewLast");
        assertThat(newTrainee.getSpecialization()).isEqualTo(TrainingType.YOGA);
        assertThat(newTrainee.getIsActive()).isFalse();
    }

    @Test
    void update_successWhenNotChangingUsername() {
        var trainer = Util.trainer(1, "trainer.username");
        var request = TrainerService.UpdateRequest.builder().username("trainer.username").password(
                "new.password").isActive(
                        false).firstName("NewFirst").lastName("NewLast").specialization(TrainingType.YOGA).build();


        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var newTrainer = assertDoesNotThrow(() -> trainerService.update("trainer.username", request));
        assertThat(newTrainer.getUsername()).isEqualTo("trainer.username");
    }

    @Test
    void update_throwsOnInvalidUsername() {
        var trainer = Util.trainer(1, "trainer.username");
        var request = TrainerService.UpdateRequest.builder().username("new/username").password("new.password").isActive(
                false).firstName("NewFirst").lastName("NewLast").specialization(TrainingType.YOGA).build();

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        assertThrows(ConstraintViolationException.class, () -> trainerService.update("trainer.username", request));
    }

    @Test
    void update_throwsOnDuplicateUsername() {
        var trainer = Util.trainer(1, "old.username");
        var trainer2 = Util.trainer(2, "existing.username");
        var request = TrainerService.UpdateRequest.builder().username("existing.username").password(
                "new.password").isActive(
                        false).firstName("NewFirst").lastName("NewLast").specialization(TrainingType.YOGA).build();

        when(trainerRepository.findByUsername("old.username")).thenReturn(Optional.of(trainer));
        when(trainerRepository.findByUsername("existing.username")).thenReturn(Optional.of(trainer2));

        assertThrows(DuplicateUsernameException.class, () -> trainerService.update("old.username", request));
    }

    @Test
    void findByUsername_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findByUsername("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
