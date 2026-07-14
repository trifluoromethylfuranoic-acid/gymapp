package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Training;
import jakarta.annotation.Nonnull;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingRepository extends JpaRepository<Training, UUID>, JpaSpecificationExecutor<Training> {
    void deleteByTraineeId(@Nonnull UUID id);
}
