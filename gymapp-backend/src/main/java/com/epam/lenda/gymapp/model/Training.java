package com.epam.lenda.gymapp.model;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
public class Training extends AbstractEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Trainee trainee;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private TrainingType type;

    @Column(nullable = false)
    private Date datetime;

    @Column(nullable = false)
    private Integer durationMinutes;
}
