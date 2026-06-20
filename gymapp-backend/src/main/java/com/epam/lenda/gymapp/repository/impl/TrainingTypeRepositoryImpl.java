package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {
    private final MapBasedStorage<TrainingType> trainingTypesData;

    @Nonnull
    @Override
    public Optional<TrainingType> findById(long id) {
        return Optional.ofNullable(trainingTypesData.getData().get(id));
    }

    @Nonnull
    @Override
    public Optional<TrainingType> findByName(@Nonnull String name) {
        return trainingTypesData.getData().values().stream().filter(t -> t.getName().equalsIgnoreCase(name)).findAny();
    }

    @Override
    public @Nonnull TrainingType findOrCreate(@Nonnull String name) {
        final var trainingTypeOpt = findByName(name);
        TrainingType trainingType;
        if (trainingTypeOpt.isPresent()) {
            trainingType = trainingTypeOpt.get();
        } else {
            trainingType = TrainingType.builder().name(name).build();
            save(trainingType);
        }
        return trainingType;
    }

    @Nonnull
    @Override
    public TrainingType save(@Nonnull TrainingType entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            merge(entity);
        }
        return entity;
    }

    private void persist(@Nonnull TrainingType entity) {
        checkEntity(entity);
        entity.setId(trainingTypesData.nextId());
        trainingTypesData.getData().put(entity.getId(), entity);
    }

    private void merge(@Nonnull TrainingType entity) {
        checkEntity(entity);
        trainingTypesData.getData().put(entity.getId(), entity);
    }

    @Override
    public void delete(@Nonnull TrainingType entity) {
        checkEntity(entity);
        trainingTypesData.getData().remove(entity.getId());
    }

    @Override
    public void delete(long id) {
        findById(id).ifPresent(this::delete);
    }

    private void checkEntity(@Nonnull TrainingType entity) {
        if (entity.getName() == null) {
            throw new IllegalStateException("Non nullable field is null in Trainer entity");
        }
    }
}
