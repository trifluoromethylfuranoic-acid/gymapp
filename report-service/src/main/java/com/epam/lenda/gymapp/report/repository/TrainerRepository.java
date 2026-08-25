package com.epam.lenda.gymapp.report.repository;

import com.epam.lenda.gymapp.report.model.Trainer;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends ListCrudRepository<Trainer, String> {

}
