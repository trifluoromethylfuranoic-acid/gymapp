package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.dto.trainer.SearchTrainerRequest;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface TrainerRepository extends Repository<Trainee, Long> {

    @Nonnull
    Optional<Trainer> findById(long id);

    @Nonnull
    Optional<Trainer> findByUsername(@Nonnull String username);

    @Nonnull
    Trainer save(@Nonnull Trainer entity);

    void delete(@Nonnull Trainer entity);

    void delete(long id);

    @Nonnull
    Page<Trainer> search(@Nullable SearchTrainerRequest request, @Nullable Pageable pageable);
}
