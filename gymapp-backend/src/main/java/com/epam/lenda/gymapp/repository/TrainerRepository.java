package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Trainer;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends BaseUserRepository<Trainer> {
    @Query("""
            SELECT trainer FROM Trainer trainer WHERE trainer.user.username IN :trainerUsernames
            """)
    @Nonnull
    List<Trainer> findByUsernames(@Nonnull List<String> trainerUsernames);
}
