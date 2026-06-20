package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.trainer.PatchTrainerRequest;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.mapper.sort.TrainerSortMapper;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainee2Trainer;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.impl.TrainerServiceImpl;
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
class TrainerServiceTest {
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainerSortMapper trainerSortMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TraineeService traineeService;

    private TrainerServiceImpl trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl(
                trainerRepository, trainerSortMapper, Mappers.getMapper(UserMapper.class), userRepository,
                trainingTypeRepository, traineeService
        );
    }

    @Test
    void patchTrainerProfile_updatesProvidedFieldsAndSavesEntity() {
        var trainer = trainer("old.username");
        var specialization = TrainingType.builder().id(99L).name("Yoga").build();
        var request = new PatchTrainerRequest("new.username", "NewFirst", "NewLast", "Yoga", false);

        when(trainerRepository.findByUsername("old.username")).thenReturn(Optional.of(trainer));
        when(userRepository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(trainingTypeRepository.findOrCreate("Yoga")).thenReturn(specialization);

        var response = trainerService.patchTrainerProfile("old.username", request);

        assertThat(trainer.getUser().getUsername()).isEqualTo("new.username");
        assertThat(trainer.getUser().getFirstName()).isEqualTo("NewFirst");
        assertThat(trainer.getUser().getLastName()).isEqualTo("NewLast");
        assertThat(trainer.getUser().getIsActive()).isFalse();
        assertThat(trainer.getSpecialization()).isSameAs(specialization);
        assertThat(response.username()).isEqualTo("new.username");
        assertThat(response.firstName()).isEqualTo("NewFirst");
        assertThat(response.lastName()).isEqualTo("NewLast");
        assertThat(response.specialization()).isEqualTo("Yoga");
        assertThat(response.isActive()).isFalse();
        assertThat(response.trainees()).isEmpty();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void patchTrainerProfile_withNullRequestDoesNotSaveEntity() {
        var trainer = trainer("trainer.username");

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var response = trainerService.patchTrainerProfile("trainer.username", null);

        assertThat(response.username()).isEqualTo("trainer.username");
        assertThat(response.firstName()).isEqualTo("Trainer");
        assertThat(response.lastName()).isEqualTo("Last");
        assertThat(response.specialization()).isEqualTo("Fitness");
        assertThat(response.isActive()).isTrue();
        assertThat(response.trainees()).isEmpty();
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void patchTrainerProfile_rejectsDuplicateUsername() {
        var trainer = trainer("trainer.username");
        var request = new PatchTrainerRequest("existing.username", null, null, null, null);

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));
        when(userRepository.findByUsername("existing.username")).thenReturn(Optional.of(User.builder().id(
                2000L).build()));

        assertThatThrownBy(() -> trainerService.patchTrainerProfile("trainer.username", request)).isInstanceOf(
                DuplicateUsernameException.class);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getTrainerProfile_throwsWhenFullTrainerDoesNotExist() {
        when(trainerRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getFullTrainerProfile("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTrainer_clearsReferencesAndDeletesEntity() {
        var trainer = trainer("trainer.username");
        var trainee = trainee("trainee.username");
        var assignment = Trainee2Trainer.builder().trainee(trainee).trainer(trainer).build();
        var training = Training.builder().trainee(trainee).trainer(trainer).build();
        trainer.getTraineeAssignments().add(assignment);
        trainer.getTrainings().add(training);
        trainee.getTrainerAssignments().add(assignment);

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        trainerService.deleteTrainer("trainer.username");

        assertThat(trainee.getTrainerAssignments()).isEmpty();
        assertThat(training.getTrainer()).isNull();
        var captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).delete(captor.capture());
        assertThat(captor.getValue()).isSameAs(trainer);
    }

    @Test
    void assignTrainee_delegatesToTraineeServiceAndReturnsAssignedTrainees() {
        var trainer = trainer("trainer.username");
        var trainee = trainee("trainee.username");
        var assignment = Trainee2Trainer.builder().trainee(trainee).trainer(trainer).build();
        trainer.getTraineeAssignments().add(assignment);
        trainee.getTrainerAssignments().add(assignment);

        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var response = trainerService.assignTrainee("trainer.username", "trainee.username");

        verify(traineeService).assignTrainer("trainee.username", "trainer.username");
        assertThat(response).hasSize(1);
        assertThat(response.get(0).username()).isEqualTo("trainee.username");
        assertThat(response.get(0).firstName()).isEqualTo("First");
        assertThat(response.get(0).dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void unassignTrainee_delegatesToTraineeService() {
        var trainer = trainer("trainer.username");
        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));

        var response = trainerService.unassignTrainee("trainer.username", "trainee.username");

        verify(traineeService).unassignTrainer("trainee.username", "trainer.username");
        assertThat(response).isEmpty();
    }

    private static Trainer trainer(String username) {
        return Trainer.builder().id(2L).user(User.builder().id(20L).username(username).firstName("Trainer").lastName(
                "Last").isActive(true).build()).specialization(TrainingType.builder().name(
                        "Fitness").build()).traineeAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
    }

    private static Trainee trainee(String username) {
        return Trainee.builder().id(1L).user(User.builder().id(10L).username(username).firstName("First").lastName(
                "Last").isActive(true).build()).dateOfBirth(LocalDate.of(1990, 1, 1)).address(
                        "Address").trainerAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
    }
}
