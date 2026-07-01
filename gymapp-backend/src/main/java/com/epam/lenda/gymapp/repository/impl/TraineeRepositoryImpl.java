package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Repository;

@Repository
public class TraineeRepositoryImpl extends BaseUserRepositoryImpl<Trainee> implements TraineeRepository {
    public TraineeRepositoryImpl(MapBasedStorage<Trainee> traineesData) {
        super(traineesData);
    }

    @Override
    protected void checkEntity(@Nonnull Trainee entity) {
        super.checkEntity(entity);
        if (entity.getAddress() == null || entity.getDateOfBirth() == null) {
            throw new IllegalStateException("Non nullable field is null in Trainer entity");
        }
    }
}
