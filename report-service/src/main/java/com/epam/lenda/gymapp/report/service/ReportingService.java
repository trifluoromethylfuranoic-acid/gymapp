package com.epam.lenda.gymapp.report.service;

import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.YearRecord;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.jspecify.annotations.NonNull;

public interface ReportingService {
    void recordTraining(@NotNull TrainingAction trainingAction);

    @NonNull Trainer getTrainer(@NonNull String username);

    @NonNull List<@NonNull YearRecord> getRecordsForTrainer(@NonNull String username);

    @NonNull List<@NonNull YearRecord> getRecordsForTrainerByYear(@NonNull String username, int year);
}
