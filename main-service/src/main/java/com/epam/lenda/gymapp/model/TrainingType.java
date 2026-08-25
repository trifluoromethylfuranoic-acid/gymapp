package com.epam.lenda.gymapp.model;

import jakarta.persistence.*;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class TrainingType extends AbstractEntity {
    @Column(nullable = false)
    private String name;
}
