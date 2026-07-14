package com.epam.lenda.gymapp.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

@ToString
@NoArgsConstructor
@Getter
@Setter
@Entity
@IdClass(TrainingAssignment.Id.class)
public class TrainingAssignment implements Persistable<TrainingAssignment.Id> {
    @jakarta.persistence.Id
    private UUID traineeId;

    @jakarta.persistence.Id
    private UUID trainerId;

    @Transient
    @ToString.Exclude
    private boolean isNew = true;

    @MapsId("traineeId")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "trainee_id", insertable = false, updatable = false)
    private Trainee trainee;

    @MapsId("trainerId")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_id", insertable = false, updatable = false)
    private Trainer trainer;

    public TrainingAssignment(Trainee trainee, Trainer trainer) {
        this.trainee = trainee;
        this.trainer = trainer;

        this.traineeId = trainee.getId();
        this.trainerId = trainer.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingAssignment that)) return false;
        return Objects.equals(traineeId, that.traineeId) && Objects.equals(trainerId, that.trainerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, trainerId);
    }

    @Override
    public @Nullable Id getId() {
        return new Id(traineeId, trainerId);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        private UUID traineeId;
        private UUID trainerId;
    }
}
