package com.epam.lenda.gymapp.report.repository;

import com.epam.lenda.gymapp.report.model.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends MongoRepository<Trainer, String> {

}
