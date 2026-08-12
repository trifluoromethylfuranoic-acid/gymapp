package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.TestConfig;
import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.dto.request.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.request.SearchTrainingRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.service.impl.TrainingServiceImpl;
import jakarta.validation.ConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({TestConfig.class, TrainingServiceImpl.class})
class TrainingServiceTest {
    @MockitoBean
    private TrainingRepository trainingRepository;
    @MockitoBean
    private TraineeRepository traineeRepository;
    @MockitoBean
    private TrainerRepository trainerRepository;
    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingService trainingService;

    @Test
    void create_success() {
        var trainee = Util.trainee("trainee.username");
        var trainer = Util.trainer("trainer.username");
        var trainingType = Util.trainingType("Cardio");
        var datetime = new Date();

        when(traineeRepository.findByUsername("trainee.username")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("trainer.username")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByNameIgnoreCase("Cardio")).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = trainingService.create(CreateTrainingRequest.builder().trainee(
                "trainee.username").trainer("trainer.username").name("New training").type("Cardio").datetime(
                        datetime).durationMinutes(60).build());

        var captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getTrainee()).isSameAs(trainee);
        assertThat(saved.getTrainer()).isSameAs(trainer);
        assertThat(saved.getType()).isEqualTo(trainingType);
        assertThat(created.getTrainee().getUser().getUsername()).isEqualTo("trainee.username");
        assertThat(created.getTrainer().getUser().getUsername()).isEqualTo("trainer.username");
        assertThat(created.getName()).isEqualTo("New training");
        assertThat(created.getDatetime()).isEqualTo(datetime);
        assertThat(created.getDurationMinutes()).isEqualTo(60);
    }

    @Test
    void create_throwsOnBlankName() {
        var trainee = Util.trainee("trainee");
        var trainer = Util.trainer("trainer");

        assertThatThrownBy(() -> trainingService.create(
                CreateTrainingRequest
                        .builder()
                        .trainee("trainee")
                        .trainer("trainer")
                        .name("")
                        .type("Yoga")
                        .datetime(new Date())
                        .durationMinutes(1)
                        .build()))
                .isInstanceOf(ConstraintViolationException.class);

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void findById_throwsWhenTrainingDoesNotExist() {
        var id = UUID.fromString("9f9f97cd-1e21-4a79-93de-c82396f91527");
        when(trainingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void search_delegatesToRepositoryWithSpecification() {
        var from = new Date(1000);
        var to = new Date(2000);
        var training = Training
                .builder()
                .trainee(Util.trainee("trainee.username"))
                .trainer(Util.trainer("trainer.username"))
                .name("Training")
                .type(Util.trainingType("Cardio"))
                .datetime(from)
                .durationMinutes(30)
                .build();

        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        var result = trainingService.search(SearchTrainingRequest
                                                    .builder()
                                                    .fromDateInclusive(from)
                                                    .toDateInclusive(to)
                                                    .traineeUsername("trainee.username")
                                                    .trainerUsername("trainer.username")
                                                    .build());

        assertThat(result).containsExactly(training);
        verify(trainingRepository).findAll(any(Specification.class));
    }
}
