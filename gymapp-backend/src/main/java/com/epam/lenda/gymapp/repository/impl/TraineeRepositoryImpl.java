package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.dto.trainee.SearchTraineeRequest;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainee2Trainer;
import com.epam.lenda.gymapp.repository.TraineeRepository;
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
public class TraineeRepositoryImpl implements TraineeRepository {
    private final MapBasedStorage<Trainee> traineesData;

    private final UserRepository userRepository;

    @Override
    @Nonnull
    public Optional<Trainee> findById(long id) {
        return Optional.ofNullable(traineesData.getData().get(id));
    }

    @Override
    @Nonnull
    public Optional<Trainee> findByUsername(@Nonnull String username) {
        return traineesData.getData().values().stream().filter(t -> Objects.equals(username,
                t.getUser().getUsername())).findAny();
    }

    @Override
    @Nonnull
    public Trainee save(@Nonnull Trainee entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            merge(entity);
        }
        return entity;
    }

    private void persist(@Nonnull Trainee entity) {
        checkEntity(entity);
        userRepository.save(entity.getUser());
        entity.setId(traineesData.nextId());
        traineesData.getData().put(entity.getId(), entity);
    }

    private void merge(@Nonnull Trainee entity) {
        checkEntity(entity);
        userRepository.save(entity.getUser());
        traineesData.getData().put(entity.getId(), entity);
    }

    @Override
    public void delete(@Nonnull Trainee entity) {
        checkEntity(entity);
        userRepository.delete(entity.getUser());
        traineesData.getData().remove(entity.getId());
    }

    @Override
    public void delete(long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    @Nonnull
    public Page<Trainee> search(@Nullable SearchTraineeRequest request, @Nullable Pageable pageable) {
        var stream = traineesData.getData().values().stream();

        if (request == null) {
            return PageUtil.streamToPage(stream, Trainee.class, pageable);
        }

        if (Strings.isNotBlank(request.nameQuery())) {
            final var nameQueryLower = request.nameQuery().toLowerCase().trim();
            stream = stream.filter(t -> SearchUtil.userMatches(t.getUser(), nameQueryLower));
        }

        if (Strings.isNotBlank(request.addressQuery())) {
            final var addressQueryLower = request.addressQuery().toLowerCase().trim();
            stream = stream.filter(t -> t.getAddress().toLowerCase().contains(addressQueryLower));
        }

        if (request.dateOfBirthMin() != null) {
            stream = stream.filter(t -> !request.dateOfBirthMin().isAfter(t.getDateOfBirth()));
        }

        if (request.dateOfBirthMax() != null) {
            stream = stream.filter(t -> !request.dateOfBirthMax().isBefore(t.getDateOfBirth()));
        }

        if (Strings.isNotBlank(request.assignedTo())) {
            final var assignedToLower = request.assignedTo().toLowerCase().trim();
            stream = stream.filter(trainee -> trainee.getTrainerAssignments().stream().map(
                    Trainee2Trainer::getTrainer).anyMatch(trainer -> SearchUtil.userMatches(trainer.getUser(),
                            assignedToLower))
            );
        }

        if (Strings.isNotBlank(request.notAssignedTo())) {
            final var notAssignedToLower = request.notAssignedTo().toLowerCase().trim();
            stream = stream.filter(trainee -> trainee.getTrainerAssignments().stream().map(
                    Trainee2Trainer::getTrainer).noneMatch(trainer -> SearchUtil.userMatches(trainer.getUser(),
                            notAssignedToLower))
            );
        }

        return PageUtil.streamToPage(stream, Trainee.class, pageable);
    }

    private void checkEntity(@Nonnull Trainee entity) {
        if (entity.getTrainerAssignments() == null || entity.getUser() == null || entity.getTrainings() == null) {
            throw new IllegalStateException("Non nullable field is null in Trainee entity");
        }
    }
}
