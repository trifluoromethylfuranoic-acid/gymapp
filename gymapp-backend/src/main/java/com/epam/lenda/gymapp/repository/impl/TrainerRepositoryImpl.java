package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.dto.trainer.SearchTrainerRequest;
import com.epam.lenda.gymapp.model.Trainee2Trainer;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import com.epam.lenda.gymapp.util.PageUtil;
import com.epam.lenda.gymapp.util.SearchUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrainerRepositoryImpl implements TrainerRepository {
    private final MapBasedStorage<Trainer> trainersData;

    private final UserRepository userRepository;
    private final TrainingTypeRepository trainingTypeRepository;


    @Nonnull
    @Override
    public Optional<Trainer> findById(long id) {
        return Optional.ofNullable(trainersData.getData().get(id));
    }

    @Nonnull
    @Override
    public Optional<Trainer> findByUsername(@Nonnull String username) {
        return trainersData.getData().values().stream().filter(t -> Objects.equals(username,
                t.getUser().getUsername())).findAny();
    }

    @Nonnull
    @Override
    public Trainer save(@Nonnull Trainer entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            merge(entity);
        }
        return entity;
    }

    private void persist(@Nonnull Trainer entity) {
        checkEntity(entity);

        userRepository.save(entity.getUser());
        trainingTypeRepository.save(entity.getSpecialization());

        entity.setId(trainersData.nextId());
        trainersData.getData().put(entity.getId(), entity);
    }

    private void merge(@Nonnull Trainer entity) {
        checkEntity(entity);

        userRepository.save(entity.getUser());
        trainingTypeRepository.save(entity.getSpecialization());

        trainersData.getData().put(entity.getId(), entity);
    }

    @Override
    public void delete(@Nonnull Trainer entity) {
        checkEntity(entity);
        userRepository.delete(entity.getUser());
        trainersData.getData().remove(entity.getId());
    }

    @Override
    public void delete(long id) {
        findById(id).ifPresent(this::delete);
    }

    @Nonnull
    @Override
    public Page<Trainer> search(@Nullable SearchTrainerRequest request, @Nullable Pageable pageable) {
        var stream = trainersData.getData().values().stream();

        if (request == null) {
            return PageUtil.streamToPage(stream, Trainer.class, pageable);
        }

        if (Strings.isNotBlank(request.nameQuery())) {
            final var nameQueryLower = request.nameQuery().toLowerCase().trim();
            stream = stream.filter(t -> SearchUtil.userMatches(t.getUser(), nameQueryLower));
        }

        if (Strings.isNotBlank(request.specializationQuery())) {
            final var addressQueryLower = request.specializationQuery().toLowerCase().trim();
            stream = stream.filter(t -> t.getSpecialization().getName().toLowerCase().contains(addressQueryLower));
        }

        if (Strings.isNotBlank(request.assignedTo())) {
            final var assignedToLower = request.assignedTo().toLowerCase().trim();
            stream = stream.filter(trainer -> trainer.getTraineeAssignments().stream().map(
                    Trainee2Trainer::getTrainee).anyMatch(trainee -> SearchUtil.userMatches(trainee.getUser(),
                            assignedToLower))
            );
        }

        if (Strings.isNotBlank(request.notAssignedTo())) {
            final var notAssignedToLower = request.notAssignedTo().toLowerCase().trim();
            stream = stream.filter(trainer -> trainer.getTraineeAssignments().stream().map(
                    Trainee2Trainer::getTrainee).noneMatch(trainee -> SearchUtil.userMatches(trainee.getUser(),
                            notAssignedToLower))
            );
        }

        return PageUtil.streamToPage(stream, Trainer.class, pageable);
    }

    private void checkEntity(@Nonnull Trainer entity) {
        if (entity.getSpecialization() == null || entity.getTraineeAssignments() == null || entity.getUser() == null || entity.getTrainings() == null) {
            throw new IllegalStateException("Non nullable field is null in Trainer entity");
        }
    }
}
