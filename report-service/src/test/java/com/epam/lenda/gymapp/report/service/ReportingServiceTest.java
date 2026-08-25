package com.epam.lenda.gymapp.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.report.dto.Action;
import com.epam.lenda.gymapp.report.dto.TrainerRequest;
import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.mapper.impl.RecordMapperImpl;
import com.epam.lenda.gymapp.report.mapper.impl.TrainerMapperImpl;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.TrainingRecord;
import com.epam.lenda.gymapp.report.repository.TrainerRepository;
import com.epam.lenda.gymapp.report.repository.TrainingRecordRepository;
import com.epam.lenda.gymapp.report.service.impl.ReportingServiceImpl;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@Import({ReportingServiceImpl.class, TrainerMapperImpl.class, RecordMapperImpl.class})
public class ReportingServiceTest {
    private static final LocalDateTime FIXED_DATETIME = LocalDateTime.of(2000, 6, 20, 12, 0);

    @MockitoBean
    private TrainerRepository trainerRepository;

    @MockitoBean
    private TrainingRecordRepository trainingRecordRepository;

    @Autowired
    private ReportingService reportingService;

    @Configuration
    static class Config {
        @Bean
        public Clock clock() {
            return Clock.fixed(FIXED_DATETIME.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        }
    }

    @ParameterizedTest
    @MethodSource("argumentsForReportTraining")
    void reportTraining_createSuccess(TrainerRequest trainerRequest, Optional<Trainer> trainer,
                                      Optional<TrainingRecord> record, TrainingAction action,
                                      long finalDurationMinutes) {
        when(trainerRepository.findById(trainerRequest.username())).thenReturn(trainer);
        when(trainingRecordRepository.findById(any())).thenReturn(record);

        reportingService.recordTraining(action);

        final var captor = ArgumentCaptor.forClass(TrainingRecord.class);
        verify(trainerRepository).save(any());
        verify(trainingRecordRepository).save(captor.capture());
        final var savedRecord = captor.getValue();

        assertEquals(finalDurationMinutes, savedRecord.getTrainingDurationMinutes());
    }

    Stream<Arguments> argumentsForReportTraining() {
        final var trainerRequest =
                TrainerRequest.builder().username("trainer1").firstName("a").lastName("b").isActive(true).build();
        final var trainer = Trainer
                .builder()
                .username(trainerRequest.username())
                .firstName(trainerRequest.firstName())
                .lastName(trainerRequest.lastName())
                .isActive(trainerRequest.isActive())
                .build();
        final var actions = new TrainingAction[]{
                TrainingAction
                        .builder()
                        .trainer(trainerRequest)
                        .datetime(FIXED_DATETIME.plusMinutes(100).atZone(ZoneOffset.UTC))
                        .durationMinutes(10)
                        .action(Action.CREATE)
                        .build(),
                TrainingAction
                        .builder()
                        .trainer(trainerRequest)
                        .datetime(FIXED_DATETIME.plusMinutes(100).atZone(ZoneOffset.UTC))
                        .durationMinutes(10)
                        .action(Action.DELETE)
                        .build(),
                TrainingAction
                        .builder()
                        .trainer(trainerRequest)
                        .datetime(FIXED_DATETIME.minusMinutes(100).atZone(ZoneOffset.UTC))
                        .durationMinutes(10)
                        .action(Action.DELETE)
                        .build()
        };
        final var record = TrainingRecord
                .builder()
                .id(TrainingRecord.Id
                            .builder()
                            .trainer(trainerRequest.username())
                            .year(FIXED_DATETIME.getYear())
                            .month(FIXED_DATETIME.getMonth())
                            .build())
                .trainingDurationMinutes(500L)
                .build();

        return Stream.of(
                Arguments.of(trainerRequest, Optional.empty(), Optional.empty(), actions[0], 10),
                Arguments.of(trainerRequest, Optional.of(trainer.clone()), Optional.empty(), actions[0], 10),
                Arguments.of(trainerRequest, Optional.of(trainer.clone()), Optional.of(record.clone()), actions[0], 510),
                Arguments.of(trainerRequest, Optional.empty(), Optional.empty(), actions[1], 0),
                Arguments.of(trainerRequest, Optional.empty(), Optional.of(record.clone()), actions[1], 490),
                Arguments.of(trainerRequest, Optional.empty(), Optional.of(record.clone()), actions[2], 500)
                );
    }
}
