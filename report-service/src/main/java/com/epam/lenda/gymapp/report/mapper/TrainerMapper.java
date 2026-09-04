package com.epam.lenda.gymapp.report.mapper;

import com.epam.lenda.gymapp.report.dto.TrainerRequest;
import com.epam.lenda.gymapp.report.dto.TrainerResponse;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.YearRecord;
import java.util.List;
import org.jspecify.annotations.NonNull;

public interface TrainerMapper {
    Trainer toEntity(TrainerRequest request);

    @NonNull TrainerResponse toDto(@NonNull Trainer trainer, @NonNull List<@NonNull YearRecord> records);
}
