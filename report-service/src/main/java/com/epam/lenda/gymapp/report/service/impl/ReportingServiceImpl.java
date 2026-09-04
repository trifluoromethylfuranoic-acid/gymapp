package com.epam.lenda.gymapp.report.service.impl;

import com.epam.lenda.gymapp.report.dto.Action;
import com.epam.lenda.gymapp.report.dto.TrainerRequest;
import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.report.mapper.TrainerMapper;
import com.epam.lenda.gymapp.report.model.MonthRecord;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.YearRecord;
import com.epam.lenda.gymapp.report.repository.TrainerRepository;
import com.epam.lenda.gymapp.report.service.ReportingService;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {
    private final TrainerRepository trainerRepository;
    private final TrainerMapper trainerMapper;
    private final Clock clock;

    @Override
    public void recordTraining(@NotNull TrainingAction trainingAction) {
        final var trainer = trainerRepository
                .findById(trainingAction.trainer().username())
                .map(trainerEntity -> updateTrainerInfo(trainerEntity, trainingAction.trainer()))
                .orElseGet(() -> trainerMapper.toEntity(trainingAction.trainer()));

        if (trainer.getYearRecords() == null) {
            trainer.setYearRecords(new ArrayList<>());
        }

        final var year = trainingAction.datetime().getYear();
        final var yearRecord = trainer.getYearRecords().stream()
                .filter(existingRecord -> Integer.valueOf(year).equals(existingRecord.getYear()))
                .findFirst()
                .orElseGet(() -> {
                    final var newYearRecord = new YearRecord(year, new ArrayList<>());
                    trainer.getYearRecords().add(newYearRecord);
                    return newYearRecord;
                });

        if (yearRecord.getMonthRecords() == null) {
            yearRecord.setMonthRecords(new ArrayList<>());
        }

        final var month = trainingAction.datetime().getMonth();
        final var monthRecord = yearRecord.getMonthRecords().stream()
                .filter(existingRecord -> month.equals(existingRecord.getMonth()))
                .findFirst()
                .orElseGet(() -> {
                    final var newMonthRecord = new MonthRecord(month, 0L);
                    yearRecord.getMonthRecords().add(newMonthRecord);
                    return newMonthRecord;
                });

        if (trainingAction.action() == Action.CREATE) {
            final var newDurationMinutes = monthRecord.getTrainingDurationMinutes() + trainingAction.durationMinutes();
            monthRecord.setTrainingDurationMinutes(newDurationMinutes);
        } else if (trainingAction.datetime().toInstant().isAfter(clock.instant())) {
            final var newDurationMinutes = Math.max(0L,
                                                    monthRecord.getTrainingDurationMinutes()
                                                            - trainingAction.durationMinutes());
            monthRecord.setTrainingDurationMinutes(newDurationMinutes);
        }
        trainerRepository.save(trainer);
    }

    @Override
    public @NonNull Trainer getTrainer(@NonNull String username) {
        return trainerRepository.findById(username)
                                .orElseThrow(() -> new ResourceNotFoundException("trainer", username));
    }

    @Override
    public @NonNull List<@NonNull YearRecord> getRecordsForTrainer(@NonNull String username) {
        return trainerRepository.findById(username)
                .map(Trainer::getYearRecords)
                .map(records -> records == null ? List.<YearRecord>of() : records)
                .orElseGet(List::of);
    }

    @Override
    public @NonNull List<@NonNull YearRecord> getRecordsForTrainerByYear(@NonNull String username, int year) {
        return getRecordsForTrainer(username).stream()
                .filter(record -> Integer.valueOf(year).equals(record.getYear()))
                .toList();
    }

    private @NonNull Trainer updateTrainerInfo(@NonNull Trainer trainer, @NonNull TrainerRequest request) {
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setIsActive(request.isActive());
        return trainer;
    }
}
