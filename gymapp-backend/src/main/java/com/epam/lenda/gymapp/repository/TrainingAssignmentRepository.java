package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingAssignment;
import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, TrainingAssignment.Id> {
    @Query("""
            SELECT trainer FROM Trainer trainer
                WHERE NOT EXISTS (
                    SELECT 1 FROM TrainingAssignment ta
                        WHERE ta.trainee.user.username = :traineeUsername
                            AND ta.trainer.id = trainer.id
                ) AND trainer.user.isActive
            """)
    @Nonnull
    List<Trainer> findActiveTrainersNotAssignedToTrainee(@Nonnull String traineeUsername);

    @Query("""
            SELECT ta FROM TrainingAssignment ta
                WHERE ta.traineeId = :traineeId
            """)
    @Nonnull
    List<TrainingAssignment> findByTraineeId(@Nonnull UUID traineeId);

    @Query("""
            SELECT ta FROM TrainingAssignment ta
                WHERE ta.trainerId = :trainerId
            """)
    @Nonnull
    List<TrainingAssignment> findByTrainerId(@Nonnull UUID trainerId);

    @Query("""
            DELETE FROM TrainingAssignment ta
                WHERE ta.traineeId = :traineeId
            """)
    @Modifying
    void deleteByTraineeId(@Nonnull UUID traineeId);

    @Query("""
            DELETE FROM TrainingAssignment ta
                WHERE ta.traineeId = :traineeId
                    AND ta.trainerId IN :trainerIds
            """)
    @Modifying
    void deleteByTraineeIdAndTrainerIds(@Nonnull UUID traineeId, @Nonnull Collection<UUID> trainerIds);
}
