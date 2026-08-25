package com.epam.lenda.gymapp.report.service.impl;

import com.epam.lenda.gymapp.report.dto.Action;
import com.epam.lenda.gymapp.report.dto.TrainerRequest;
import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.report.mapper.TrainerMapper;
import com.epam.lenda.gymapp.report.model.Trainer;
import com.epam.lenda.gymapp.report.model.TrainingRecord;
import com.epam.lenda.gymapp.report.repository.TrainerRepository;
import com.epam.lenda.gymapp.report.repository.TrainingRecordRepository;
import com.epam.lenda.gymapp.report.service.ReportingService;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {
    private final TrainerRepository trainerRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final TrainerMapper trainerMapper;
    private final Clock clock;

    @Transactional
    @Override
    public void recordTraining(@NotNull TrainingAction trainingAction) {
        updateOrCreateTrainer(trainingAction.trainer());

        final var recordId = TrainingRecord.Id
                .builder()
                .trainer(trainingAction.trainer().username())
                .year(trainingAction.datetime().getYear())
                .month(trainingAction.datetime().getMonth())
                .build();

        final var record = trainingRecordRepository.findById(recordId).orElseGet(
                () -> new TrainingRecord(recordId, 0L));

        if (trainingAction.action() == Action.CREATE) {
            final var newDurationMinutes = record.getTrainingDurationMinutes() + trainingAction.durationMinutes();
            record.setTrainingDurationMinutes(newDurationMinutes);
        } else if (trainingAction.datetime().toInstant().isAfter(clock.instant())) {
            final var newDurationMinutes = Math.max(0L,
                                                    record.getTrainingDurationMinutes() - trainingAction.durationMinutes());
            record.setTrainingDurationMinutes(newDurationMinutes);
        }
        trainingRecordRepository.save(record);

    }

    @Transactional
    @Override
    public @NonNull Trainer getTrainer(@NonNull String username) {
        return trainerRepository.findById(username)
                                .orElseThrow(() -> new ResourceNotFoundException("trainer", username));
    }

    @Transactional
    @Override
    public @NonNull List<@NonNull TrainingRecord> getRecordsForTrainer(@NonNull String username) {
        return trainingRecordRepository.findByTrainerUsername(username);
    }

    @Transactional
    @Override
    public @NonNull List<@NonNull TrainingRecord> getRecordsForTrainerByYear(@NonNull String username, int year) {
        return trainingRecordRepository.findByTrainerUsernameAndYear(username, year);
    }

    private void updateOrCreateTrainer(@NonNull TrainerRequest trainerRequest) {
        final var trainer = trainerRepository
                .findById(trainerRequest.username())
                .map(trainerEntity -> updateTrainerInfo(trainerEntity, trainerRequest))
                .orElseGet(() -> trainerMapper.toEntity(trainerRequest));

        trainerRepository.save(trainer);
    }

    private @NonNull Trainer updateTrainerInfo(@NonNull Trainer trainer, @NonNull TrainerRequest request) {
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setIsActive(request.isActive());
        return trainer;
    }
}
