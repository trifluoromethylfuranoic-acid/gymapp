package com.epam.lenda.gymapp.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
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

    @Override
    public @Nonnull User getUser() {
        return this;
    }
}
