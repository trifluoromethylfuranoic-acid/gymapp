package com.epam.lenda.gymapp.report.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Month;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class TrainingRecord implements Cloneable {
    @EmbeddedId
    private Id id;

    @Column(nullable = false)
    private Long trainingDurationMinutes;

    @Override
    public TrainingRecord clone() {
        try {
            return (TrainingRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Embeddable
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class Id implements Serializable {
        private String trainer;

        @Column(name = "`year`")
        private Integer year;

        @Enumerated(EnumType.STRING)
        @Column(name = "`month`")
        private Month month;
    }
}
