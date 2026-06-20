package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.model.Training;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface TrainingRepository extends Repository<Training, Long> {
    @Nonnull
    Optional<Training> findById(long id);

    @Nonnull
    Training save(@Nonnull Training entity);

    void delete(@Nonnull Training entity);

    void delete(long id);

    @Nonnull
    Page<Training> searchForTrainee(@Nullable SearchTrainingRequest request, @Nullable Pageable pageable);

    @Nonnull
    Page<Training> searchForTrainee(@Nullable SearchTrainingRequest request, @Nullable Pageable pageable,
                                    @Nullable String traineeUsername);
}
