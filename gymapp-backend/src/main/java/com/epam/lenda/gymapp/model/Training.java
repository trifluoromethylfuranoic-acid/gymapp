package com.epam.lenda.gymapp.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Training {
    private Long id;

    private Trainee trainee;

    private Trainer trainer;

    private String name;

    private TrainingType type;

    private ZonedDateTime datetime;

    private Duration duration;
}
