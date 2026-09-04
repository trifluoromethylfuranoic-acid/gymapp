package com.epam.lenda.gymapp.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.report.dto.Action;
import com.epam.lenda.gymapp.report.dto.TrainerRequest;
import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.mapper.impl.RecordMapperImpl;
import com.epam.lenda.gymapp.report.mapper.impl.TrainerMapperImpl;
import com.epam.lenda.gymapp.report.model.MonthRecord;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.YearRecord;
import com.epam.lenda.gymapp.report.repository.TrainerRepository;
import com.epam.lenda.gymapp.report.service.impl.ReportingServiceImpl;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@Import({ReportingServiceImpl.class, TrainerMapperImpl.class, RecordMapperImpl.class})
class ReportingServiceTest {
    private static final String USERNAME = "trainer1";
    private static final LocalDateTime NOW = LocalDateTime.of(2000, 6, 20, 12, 0);
    private static final TrainerRequest TRAINER_REQUEST = TrainerRequest.builder()
            .username(USERNAME)
            .firstName("Updated")
            .lastName("Trainer")
            .isActive(true)
            .build();

    @MockitoBean
    private TrainerRepository trainerRepository;

    @Autowired
    private ReportingService reportingService;

    @Configuration
    static class Config {
        @Bean
        Clock clock() {
            return Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        }
    }

    @Test
    void recordTraining_createsTrainerAndEmbeddedRecord() {
        when(trainerRepository.findById(USERNAME)).thenReturn(Optional.empty());

        reportingService.recordTraining(action(Action.CREATE, NOW.plusMinutes(100), 10));

        final var savedTrainer = verifySavedTrainer();
        assertEquals(USERNAME, savedTrainer.getUsername());
        assertEquals(1, savedTrainer.getYearRecords().size());
        assertEquals(1, savedTrainer.getYearRecords().get(0).getMonthRecords().size());
        assertEquals(10L, savedTrainer.getYearRecords().get(0).getMonthRecords().get(0).getTrainingDurationMinutes());
    }

    @Test
    void recordTraining_addsDurationToExistingMonthlyRecordAndUpdatesTrainer() {
        when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainerWithRecord(500L, 2000, Month.JUNE)));

        reportingService.recordTraining(action(Action.CREATE, NOW.plusMinutes(100), 10));

        final var savedTrainer = verifySavedTrainer();
        assertEquals(510L, savedTrainer.getYearRecords().get(0).getMonthRecords().get(0).getTrainingDurationMinutes());
        assertEquals(TRAINER_REQUEST.firstName(), savedTrainer.getFirstName());
        assertEquals(TRAINER_REQUEST.lastName(), savedTrainer.getLastName());
    }

    @Test
    void recordTraining_deleteDoesNotDropDurationBelowZero() {
        when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainerWithRecord(5L, 2000, Month.JUNE)));

        reportingService.recordTraining(action(Action.DELETE, NOW.plusMinutes(100), 10));

        final var savedTrainer = verifySavedTrainer();
        assertEquals(0L, savedTrainer.getYearRecords().get(0).getMonthRecords().get(0).getTrainingDurationMinutes());
    }

    @Test
    void recordTraining_ignoresDeleteForPastTraining() {
        when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainerWithRecord(500L, 2000, Month.JUNE)));

        reportingService.recordTraining(action(Action.DELETE, NOW.minusMinutes(100), 10));

        final var savedTrainer = verifySavedTrainer();
        assertEquals(500L, savedTrainer.getYearRecords().get(0).getMonthRecords().get(0).getTrainingDurationMinutes());
    }

    @Test
    void getRecordsForTrainerByYear_filtersEmbeddedRecords() {
        final var trainer = trainerWithRecord(500L, 2000, Month.JUNE);
        trainer.getYearRecords().add(new YearRecord(1999,
                                                    new ArrayList<>(List.of(new MonthRecord(Month.JUNE, 100L)))));
        when(trainerRepository.findById(USERNAME)).thenReturn(Optional.of(trainer));

        final var records = reportingService.getRecordsForTrainerByYear(USERNAME, 2000);

        assertEquals(1, records.size());
        assertEquals(2000, records.get(0).getYear());
    }

    private static TrainingAction action(Action action, LocalDateTime datetime, int durationMinutes) {
        return TrainingAction.builder()
                .trainer(TRAINER_REQUEST)
                .datetime(datetime.atZone(ZoneOffset.UTC))
                .durationMinutes(durationMinutes)
                .action(action)
                .build();
    }

    private static Trainer trainerWithRecord(long durationMinutes, int year, Month month) {
        final var trainer = Trainer.builder()
                .username(USERNAME)
                .firstName("Original")
                .lastName("Trainer")
                .isActive(false)
                .build();
        trainer.setYearRecords(new ArrayList<>(List.of(
                new YearRecord(year,
                               new ArrayList<>(List.of(new MonthRecord(month, durationMinutes)))))));
        return trainer;
    }

    private Trainer verifySavedTrainer() {
        final var captor = org.mockito.ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());
        return captor.getValue();
    }
}
