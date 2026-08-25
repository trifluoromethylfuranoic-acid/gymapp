package com.epam.lenda.gymapp.report.repository;

import com.epam.lenda.gymapp.report.model.TrainingRecord;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingRecordRepository extends ListCrudRepository<TrainingRecord, TrainingRecord.Id> {
    @Query("""
           SELECT tr FROM TrainingRecord tr WHERE tr.id.trainer = :username
           """)
    @NonNull List<@NonNull TrainingRecord> findByTrainerUsername(@NonNull String username);

    @Query("""
           SELECT tr FROM TrainingRecord tr WHERE tr.id.trainer = :username AND tr.id.year = :year
           """)
    @NonNull List<@NonNull TrainingRecord> findByTrainerUsernameAndYear(@NonNull String username, int year);
}
