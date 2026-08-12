package com.epam.lenda.gymapp.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
public class User extends AbstractEntity implements IsUser {
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private Integer failedLoginAttempts;

    @Column(nullable = true)
    private Instant lockedAt;

    @Override
    public @Nonnull User getUser() {
        return this;
    }
}
