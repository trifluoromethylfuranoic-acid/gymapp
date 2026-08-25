package com.epam.lenda.gymapp.report.mapper.impl;

import com.epam.lenda.gymapp.report.dto.TrainerRequest;
import com.epam.lenda.gymapp.report.dto.TrainerResponse;
import com.epam.lenda.gymapp.report.mapper.RecordMapper;
import com.epam.lenda.gymapp.report.mapper.TrainerMapper;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.TrainingRecord;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerMapperImpl implements TrainerMapper {
    private final RecordMapper recordMapper;

    @Override
    public @NonNull Trainer toEntity(@NonNull TrainerRequest request) {
        return Trainer
                .builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .isActive(request.isActive())
                .build();
    }

    @Override
    public @NonNull TrainerResponse toDto(@NonNull Trainer trainer, @NonNull List<@NonNull TrainingRecord> records) {
        final var hoursMap = recordMapper.toMap(records);
        return TrainerResponse
                .builder()
                .username(trainer.getUsername())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .isActive(trainer.getIsActive())
                .hours(hoursMap)
                .build();
    }

}
