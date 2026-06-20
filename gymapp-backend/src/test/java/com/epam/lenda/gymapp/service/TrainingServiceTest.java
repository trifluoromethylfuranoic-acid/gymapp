package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.training.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.training.PatchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.TrainingMapper;
import com.epam.lenda.gymapp.mapper.sort.TrainingSortMapper;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.impl.TrainingServiceImpl;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TrainingServiceTest {
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingSortMapper trainingSortMapper;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl(
                trainingRepository, trainingSortMapper, Mappers.getMapper(TrainingMapper.class), traineeRepository,
                trainerRepository, trainingTypeRepository
        );
    }

    @Test
    void search_mapsRepositoryPageToResponsePage() {
        var request = SearchTrainingRequest.builder().nameQuery("fitness").build();
        var pageable = Pageable.ofSize(10);
        var training = training(1L, trainee("trainee.username"), trainer("trainer.username"), fitness(), "Fitness");

        when(trainingSortMapper.mapPageable(pageable)).thenReturn(pageable);
        when(trainingRepository.searchForTrainee(request, pageable)).thenReturn(new PageImpl<>(List.of(training),
                pageable, 1));

        var response = trainingService.search(request, pageable);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.elements()).hasSize(1);
        assertThat(response.elements().get(0).trainee()).isEqualTo("trainee.username");
        assertThat(response.elements().get(0).trainer()).isEqualTo("trainer.username");
        assertThat(response.elements().get(0).trainingType()).isEqualTo("Fitness");
    }

    @Test
    void createTraining_resolvesReferencesSavesAndLinksBothSides() {
        var trainee = trainee("trainee.username");
        var trainer = trainer("trainer.username");
        var type = fitness();
        var datetime = ZonedDateTime.of(2026, 7, 1, 10, 0, 0, 0, ZoneId.of("Europe/Kiev"));
        var request = new CreateTrainingRequest(
                "trainee.username", "trainer.username", "Morning Fitness", "Fitness", datetime, Duration.ofHours(1)
        );

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findOrCreate("Fitness")).thenReturn(type);

        var response = trainingService.createTraining(request);

        var captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getTrainee()).isSameAs(trainee);
        assertThat(saved.getTrainer()).isSameAs(trainer);
        assertThat(saved.getType()).isSameAs(type);
        assertThat(trainee.getTrainings()).containsExactly(saved);
        assertThat(trainer.getTrainings()).containsExactly(saved);
        assertThat(response.trainee()).isEqualTo("trainee.username");
        assertThat(response.trainer()).isEqualTo("trainer.username");
        assertThat(response.name()).isEqualTo("Morning Fitness");
        assertThat(response.datetime()).isEqualTo(datetime);
        assertThat(response.duration()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void createTraining_throwsWhenTraineeDoesNotExist() {
        var request = new CreateTrainingRequest(
                "missing", "trainer.username", "Morning Fitness", "Fitness", ZonedDateTime.now(), Duration.ofHours(1)
        );

        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.createTraining(request)).isInstanceOf(ResourceNotFoundException.class);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getTraining_throwsWhenTrainingDoesNotExist() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTraining(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patchTraining_updatesProvidedFieldsAndMovesReferences() {
        var oldTrainee = trainee("old.trainee");
        var oldTrainer = trainer("old.trainer");
        var newTrainee = trainee("new.trainee");
        var newTrainer = trainer("new.trainer");
        var yoga = TrainingType.builder().id(8L).name("Yoga").build();
        var training = training(10L, oldTrainee, oldTrainer, fitness(), "Old Name");
        oldTrainee.getTrainings().add(training);
        oldTrainer.getTrainings().add(training);
        var newDatetime = ZonedDateTime.of(2026, 8, 1, 12, 0, 0, 0, ZoneId.of("Europe/Kiev"));
        var request = new PatchTrainingRequest(
                "new.trainee", "new.trainer", "Yoga Basics", "Yoga", newDatetime, Duration.ofMinutes(50)
        );

        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));
        when(traineeRepository.findByUsername("new.trainee")).thenReturn(Optional.of(newTrainee));
        when(trainerRepository.findByUsername("new.trainer")).thenReturn(Optional.of(newTrainer));
        when(trainingTypeRepository.findOrCreate("Yoga")).thenReturn(yoga);

        var response = trainingService.patchTraining(10L, request);

        assertThat(oldTrainee.getTrainings()).isEmpty();
        assertThat(oldTrainer.getTrainings()).isEmpty();
        assertThat(newTrainee.getTrainings()).containsExactly(training);
        assertThat(newTrainer.getTrainings()).containsExactly(training);
        assertThat(training.getTrainee()).isSameAs(newTrainee);
        assertThat(training.getTrainer()).isSameAs(newTrainer);
        assertThat(training.getName()).isEqualTo("Yoga Basics");
        assertThat(training.getType()).isSameAs(yoga);
        assertThat(training.getDatetime()).isEqualTo(newDatetime);
        assertThat(training.getDuration()).isEqualTo(Duration.ofMinutes(50));
        assertThat(response.trainee()).isEqualTo("new.trainee");
        assertThat(response.trainer()).isEqualTo("new.trainer");
        assertThat(response.trainingType()).isEqualTo("Yoga");
        verify(trainingRepository).save(training);
    }

    @Test
    void patchTraining_withNullRequestDoesNotSaveEntity() {
        var training = training(1L, trainee("trainee.username"), trainer("trainer.username"), fitness(), "Fitness");

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        var response = trainingService.patchTraining(1L, null);

        assertThat(response.name()).isEqualTo("Fitness");
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void deleteTraining_removesReferencesAndDeletesEntity() {
        var trainee = trainee("trainee.username");
        var trainer = trainer("trainer.username");
        var training = training(1L, trainee, trainer, fitness(), "Fitness");
        trainee.getTrainings().add(training);
        trainer.getTrainings().add(training);

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        trainingService.deleteTraining(1L);

        assertThat(trainee.getTrainings()).isEmpty();
        assertThat(trainer.getTrainings()).isEmpty();
        verify(trainingRepository).delete(training);
    }

    @Test
    void deleteTraining_ignoresMissingTraining() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        trainingService.deleteTraining(99L);

        verify(trainingRepository, never()).delete(any(Training.class));
    }

    private static Training training(Long id, Trainee trainee, Trainer trainer, TrainingType type, String name) {
        return Training.builder().id(id).trainee(trainee).trainer(trainer).name(name).type(type).datetime(
                ZonedDateTime.of(2026, 7, 1, 10, 0, 0, 0, ZoneId.of("Europe/Kiev"))).duration(Duration.ofHours(
                        1)).build();
    }

    private static Trainee trainee(String username) {
        return Trainee.builder().id(username.hashCode() & 0xffffL).user(User.builder().id(
                username.hashCode() & 0xffffL).username(username).firstName("Trainee").lastName("Last").isActive(
                        true).build()).dateOfBirth(LocalDate.of(1990, 1, 1)).address("Address").trainerAssignments(
                                new ArrayList<>()).trainings(new ArrayList<>()).build();
    }

    private static Trainer trainer(String username) {
        return Trainer.builder().id(username.hashCode() & 0xffffL).user(User.builder().id(
                username.hashCode() & 0xffffL).username(username).firstName("Trainer").lastName("Last").isActive(
                        true).build()).specialization(fitness()).traineeAssignments(new ArrayList<>()).trainings(
                                new ArrayList<>()).build();
    }

    private static TrainingType fitness() {
        return TrainingType.builder().id(7L).name("Fitness").build();
    }
}
