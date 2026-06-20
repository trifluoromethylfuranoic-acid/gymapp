package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.TrainingType;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface TrainingTypeRepository extends Repository<Trainee, Long> {
    @Nonnull
    Optional<TrainingType> findById(long id);

    @Nonnull
    Optional<TrainingType> findByName(@Nonnull String name);

    @Nonnull
    TrainingType findOrCreate(@Nonnull String name);

    @Nonnull
    TrainingType save(@Nonnull TrainingType entity);

    void delete(@Nonnull TrainingType entity);

    void delete(long id);
}
