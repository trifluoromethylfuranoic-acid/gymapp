package com.epam.lenda.gymapp.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer {
    private Long id;

    private User user;

    private TrainingType specialization;

    private List<Trainee2Trainer> traineeAssignments;

    private List<Training> trainings;
}