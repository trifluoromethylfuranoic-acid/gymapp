package com.epam.lenda.gymapp.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

@MappedSuperclass
@AllArgsConstructor
@ToString
@SuperBuilder
public abstract class AbstractEntity implements Persistable<UUID> {
    @Id
    private @Nonnull UUID id;

    @Transient
    @ToString.Exclude
    private boolean isNew = true;

    protected AbstractEntity() {
        this.id = UUID.randomUUID();
    }

    @Override
    public @Nonnull UUID getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntity other)) return false;
        return id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
