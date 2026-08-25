package com.epam.lenda.gymapp.report.service;

import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.TrainingRecord;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;

public interface ReportingService {
    @Transactional
    void recordTraining(@NotNull TrainingAction trainingAction);

    @Transactional
    @NonNull Trainer getTrainer(@NonNull String username);

    @Transactional
    @NonNull List<@NonNull TrainingRecord> getRecordsForTrainer(@NonNull String username);

    @Transactional
    @NonNull List<@NonNull TrainingRecord> getRecordsForTrainerByYear(@NonNull String username, int year);
}
