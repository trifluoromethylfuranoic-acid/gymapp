package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.dto.trainee.SearchTraineeRequest;
import com.epam.lenda.gymapp.model.Trainee;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface TraineeRepository extends Repository<Trainee, Long> {
    @Nonnull
    Optional<Trainee> findById(long id);

    @Nonnull
    Optional<Trainee> findByUsername(@Nonnull String username);

    @Nonnull
    Trainee save(@Nonnull Trainee entity);

    void delete(@Nonnull Trainee entity);

    void delete(long id);

    @Nonnull
    Page<Trainee> search(@Nullable SearchTraineeRequest request, @Nullable Pageable pageable);
}
