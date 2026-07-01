package com.epam.lenda.gymapp.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class Trainer extends User implements HasId {
    private Long id;

    private TrainingType specialization;

    public Trainer(String firstName, String lastName, String username, String password, Boolean isActive, Long id,
                   TrainingType specialization) {
        super(firstName, lastName, username, password, isActive);
        this.id = id;
        this.specialization = specialization;
    }
}
