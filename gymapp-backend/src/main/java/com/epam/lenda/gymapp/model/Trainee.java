package com.epam.lenda.gymapp.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainee {
    private Long id;

    private User user;

    private LocalDate dateOfBirth;

    private String address;

    private List<Trainee2Trainer> trainerAssignments;

    private List<Training> trainings;
}
