package com.epam.lenda.gymapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trainee2Trainer {
    private Long id;

    private Trainee trainee;

    private Trainer trainer;
}
