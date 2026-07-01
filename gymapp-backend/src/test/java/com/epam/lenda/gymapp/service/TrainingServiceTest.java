package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.service.impl.TrainingServiceImpl;
import jakarta.validation.ConstraintViolationException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({TestConfig.class, TrainingServiceImpl.class})
class TrainingServiceTest {
    @MockitoBean
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingService trainingService;

    @Test
    void create_success() {
        var trainee = Util.trainee(1, "trainee.username");
        var trainer = Util.trainer(2, "trainer.username");
        var datetime = ZonedDateTime.of(2026, 7, 1, 10, 0, 0, 0, ZoneId.of("Europe/Kiev"));
        var duration = Duration.ofHours(1);

        var created = trainingService.create(trainee,
                trainer,
                "New training",
                TrainingType.CARDIO,
                datetime,
                duration);

        var captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getTrainee()).isSameAs(trainee);
        assertThat(saved.getTrainer()).isSameAs(trainer);
        assertThat(saved.getType()).isEqualTo(TrainingType.CARDIO);
        assertThat(created.getTrainee().getUsername()).isEqualTo("trainee.username");
        assertThat(created.getTrainer().getUsername()).isEqualTo("trainer.username");
        assertThat(created.getName()).isEqualTo("New training");
        assertThat(created.getDatetime()).isEqualTo(datetime);
        assertThat(created.getDuration()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void create_throwsOnBlankName() {
        var trainee = Util.trainee(1, "trainee");
        var trainer = Util.trainer(1, "trainer");

        assertThatThrownBy(() -> trainingService.create(trainee,
                trainer,
                "",
                TrainingType.YOGA,
                ZonedDateTime.now(),
                Duration.ofMinutes(1))).isInstanceOf(ConstraintViolationException.class);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void findById_throwsWhenTrainingDoesNotExist() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
