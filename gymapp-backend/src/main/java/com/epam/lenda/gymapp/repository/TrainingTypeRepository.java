package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.TrainingType;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingType, UUID> {
    @Nonnull
    Optional<TrainingType> findByNameIgnoreCase(@Nonnull String name);
}
