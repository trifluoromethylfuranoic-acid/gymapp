package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerRepositoryImpl extends BaseUserRepositoryImpl<Trainer> implements TrainerRepository {
    public TrainerRepositoryImpl(MapBasedStorage<Trainer> trainersData) {
        super(trainersData);
    }

    @Override
    protected void checkEntity(@Nonnull Trainer entity) {
        super.checkEntity(entity);
        if (entity.getSpecialization() == null) {
            throw new IllegalStateException("Non nullable field is null in Trainer entity");
        }
    }
}
