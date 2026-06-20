package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.trainee.PatchTraineeRequest;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.mapper.sort.TraineeSortMapper;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainee2Trainer;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.impl.TraineeServiceImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TraineeServiceTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TraineeSortMapper traineeSortMapper;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;

    private TraineeServiceImpl traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(
                traineeRepository, traineeSortMapper, Mappers.getMapper(UserMapper.class), trainerRepository,
                userRepository
        );
    }

    @Test
    void patchTraineeProfile_updatesProvidedFieldsAndSavesEntity() {
        var trainee = trainee("old.username");
        var request = new PatchTraineeRequest(
                "new.username", "NewFirst", "NewLast", LocalDate.of(1995, 5, 14), "New address", false
        );

        when(traineeRepository.findByUsername("old.username")).thenReturn(Optional.of(trainee));
        when(userRepository.findByUsername("new.username")).thenReturn(Optional.empty());

        var response = traineeService.patchTraineeProfile("old.username", request);

        assertThat(trainee.getUser().getUsername()).isEqualTo("new.username");
        assertThat(trainee.getUser().getFirstName()).isEqualTo("NewFirst");
        assertThat(trainee.getUser().getLastName()).isEqualTo("NewLast");
        assertThat(trainee.getUser().getIsActive()).isFalse();
        assertThat(trainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 14));
        assertThat(trainee.getAddress()).isEqualTo("New address");
        assertThat(response.username()).isEqualTo("new.username");
        assertThat(response.firstName()).isEqualTo("NewFirst");
        assertThat(response.lastName()).isEqualTo("NewLast");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 14));
        assertThat(response.address()).isEqualTo("New address");
        assertThat(response.isActive()).isFalse();
        assertThat(response.trainers()).isEmpty();
        verify(traineeRepository).save(trainee);
    }

    @Test
    void patchTraineeProfile_withNullRequestDoesNotSaveEntity() {
        var trainee = trainee("trainee.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        var response = traineeService.patchTraineeProfile("trainee.username", null);

        assertThat(response.username()).isEqualTo("trainee.username");
        assertThat(response.firstName()).isEqualTo("First");
        assertThat(response.lastName()).isEqualTo("Last");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.address()).isEqualTo("Address");
        assertThat(response.isActive()).isTrue();
        assertThat(response.trainers()).isEmpty();
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void patchTrainee_profileRejectsDuplicateUsername() {
        var trainee = trainee("old.username");
        var request = new PatchTraineeRequest("existing.username", null, null, null, null, null);

        when(traineeRepository.findByUsername("old.username")).thenReturn(Optional.of(trainee));
        when(userRepository.findByUsername("existing.username")).thenReturn(Optional.of(User.builder().id(
                2000L).build()));

        assertThatThrownBy(() -> traineeService.patchTraineeProfile("old.username", request)).isInstanceOf(
                DuplicateUsernameException.class);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void getTraineeProfile_throwsWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getTraineeProfile("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignTrainer_createsBidirectionalAssignmentOnce() {
        var trainee = trainee("trainee.username");
        var trainer = trainer("trainer.username");

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var firstResponse = traineeService.assignTrainer("trainee.username", "trainer.username");
        var secondResponse = traineeService.assignTrainer("trainee.username", "trainer.username");

        assertThat(trainee.getTrainerAssignments()).hasSize(1);
        assertThat(trainer.getTraineeAssignments()).hasSize(1);
        assertThat(trainee.getTrainerAssignments().get(0).getTrainee()).isSameAs(trainee);
        assertThat(trainee.getTrainerAssignments().get(0).getTrainer()).isSameAs(trainer);
        assertThat(firstResponse).hasSize(1);
        assertThat(firstResponse.get(0).username()).isEqualTo("trainer.username");
        assertThat(firstResponse.get(0).firstName()).isEqualTo("Trainer");
        assertThat(firstResponse.get(0).specialization()).isEqualTo("Fitness");
        assertThat(secondResponse).hasSize(1);
        assertThat(secondResponse.get(0).username()).isEqualTo("trainer.username");
    }

    @Test
    void unassignTrainer_removesAssignmentFromBothSides() {
        var trainee = trainee("trainee.username");
        var trainer = trainer("trainer.username");
        var assignment = Trainee2Trainer.builder().trainee(trainee).trainer(trainer).build();
        trainee.getTrainerAssignments().add(assignment);
        trainer.getTraineeAssignments().add(assignment);

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var response = traineeService.unassignTrainer("trainee.username", "trainer.username");

        assertThat(trainee.getTrainerAssignments()).isEmpty();
        assertThat(trainer.getTraineeAssignments()).isEmpty();
        assertThat(response).isEmpty();
    }

    @Test
    void deleteTrainee_clearsReferencesAndDeletesEntity() {
        var trainee = trainee("trainee.username");
        var trainer = trainer("trainer.username");
        var assignment = Trainee2Trainer.builder().trainee(trainee).trainer(trainer).build();
        var training = Training.builder().trainee(trainee).trainer(trainer).build();
        trainee.getTrainerAssignments().add(assignment);
        trainee.getTrainings().add(training);
        trainer.getTraineeAssignments().add(assignment);

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee("trainee.username");

        assertThat(trainer.getTraineeAssignments()).isEmpty();
        assertThat(training.getTrainee()).isNull();
        var captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).delete(captor.capture());
        assertThat(captor.getValue()).isSameAs(trainee);
    }

    @Test
    void deleteTrainee_ignoresMissingTrainee() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        traineeService.deleteTrainee("missing");

        verify(traineeRepository, never()).delete(any(Trainee.class));
    }

    private static Trainee trainee(String username) {
        return Trainee.builder().id(1L).user(User.builder().id(10L).username(username).firstName("First").lastName(
                "Last").isActive(true).build()).dateOfBirth(LocalDate.of(1990, 1, 1)).address(
                        "Address").trainerAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
    }

    private static Trainer trainer(String username) {
        return Trainer.builder().id(2L).user(User.builder().id(20L).username(username).firstName("Trainer").lastName(
                "Last").isActive(true).build()).specialization(TrainingType.builder().name(
                        "Fitness").build()).traineeAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
    }
}
