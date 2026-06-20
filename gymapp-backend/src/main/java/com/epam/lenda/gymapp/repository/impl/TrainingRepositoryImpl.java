package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.repository.TrainingRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import com.epam.lenda.gymapp.util.PageUtil;
import com.epam.lenda.gymapp.util.SearchUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrainingRepositoryImpl implements TrainingRepository {
    private final MapBasedStorage<Training> trainingsData;
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    @Nonnull
    public Optional<Training> findById(long id) {
        return Optional.ofNullable(trainingsData.getData().get(id));
    }

    @Override
    @Nonnull
    public Training save(@Nonnull Training entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            merge(entity);
        }
        return entity;
    }

    private void persist(@Nonnull Training entity) {
        checkEntity(entity);
        trainingTypeRepository.save(entity.getType());
        entity.setId(trainingsData.nextId());
        trainingsData.getData().put(entity.getId(), entity);
    }

    private void merge(@Nonnull Training entity) {
        checkEntity(entity);
        trainingTypeRepository.save(entity.getType());
        trainingsData.getData().put(entity.getId(), entity);
    }

    @Override
    public void delete(@Nonnull Training entity) {
        checkEntity(entity);
        trainingsData.getData().remove(entity.getId());
    }

    @Override
    public void delete(long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public @NonNull Page<Training> searchForTrainee(@Nullable SearchTrainingRequest request,
                                                    @Nullable Pageable pageable) {
        return searchForTrainee(request, pageable, null);
    }

    @Nonnull
    @Override
    public Page<Training> searchForTrainee(@Nullable SearchTrainingRequest request, @Nullable Pageable pageable,
                                           @Nullable String traineeUsername) {
        var stream = trainingsData.getData().values().stream();

        if (traineeUsername != null) {
            stream = stream.filter(
                    training -> training.getTrainee() != null && training.getTrainee().getUser().getUsername().equals(
                            traineeUsername));
        }

        if (request == null) {
            return PageUtil.streamToPage(stream, Training.class, pageable);
        }

        if (traineeUsername == null && Strings.isNotBlank(request.traineeQuery())) {
            final var traineeQueryLower = request.traineeQuery().toLowerCase().trim();
            stream = stream.filter(t -> SearchUtil.userMatches(t.getTrainee().getUser(), traineeQueryLower));
        }

        if (Strings.isNotBlank(request.trainerQuery())) {
            final var trainerQueryLower = request.trainerQuery().toLowerCase().trim();
            stream = stream.filter(t -> SearchUtil.userMatches(t.getTrainer().getUser(), trainerQueryLower));
        }

        if (Strings.isNotBlank(request.nameQuery())) {
            final var nameQueryLower = request.nameQuery().toLowerCase().trim();
            stream = stream.filter(t -> t.getName().toLowerCase().contains(nameQueryLower));
        }

        if (Strings.isNotBlank(request.trainingTypeQuery())) {
            final var trainingTypeQueryLower = request.trainingTypeQuery().toLowerCase().trim();
            stream = stream.filter(t -> t.getType().getName().toLowerCase().contains(trainingTypeQueryLower));
        }

        if (request.datetimeMin() != null) {
            stream = stream.filter(t -> !request.datetimeMin().isAfter(t.getDatetime()));
        }

        if (request.dateTimeMax() != null) {
            stream = stream.filter(t -> !request.dateTimeMax().isBefore(t.getDatetime()));
        }

        if (request.durationMin() != null) {
            stream = stream.filter(t -> t.getDuration().compareTo(request.durationMin()) >= 0);
        }

        if (request.durationMax() != null) {
            stream = stream.filter(t -> t.getDuration().compareTo(request.durationMax()) <= 0);
        }

        return PageUtil.streamToPage(stream, Training.class, pageable);
    }

    private void checkEntity(@Nonnull Training entity) {
        if (entity.getName() == null || entity.getType() == null || entity.getDatetime() == null || entity.getDuration() == null) {
            throw new IllegalStateException("Non nullable field is null in Training entity");
        }
    }
}
