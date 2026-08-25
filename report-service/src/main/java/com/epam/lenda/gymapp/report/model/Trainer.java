package com.epam.lenda.gymapp.report.model;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

@ToString
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Trainer implements Persistable<String>, Cloneable {
    @Id
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Boolean isActive;

    @Transient
    private boolean isNew = true;

    @Builder
    public Trainer(String username, String firstName, String lastName, Boolean isActive) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    @Override
    public @Nullable String getId() {
        return username;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PrePersist
    public void markNotNew() {
        isNew = false;
    }

    @Override
    public Trainer clone() {
        try {
            return (Trainer) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
