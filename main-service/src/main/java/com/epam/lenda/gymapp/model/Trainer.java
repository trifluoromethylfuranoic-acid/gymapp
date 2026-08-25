package com.epam.lenda.gymapp.model;

import jakarta.persistence.*;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Trainer extends AbstractEntity implements IsUser {
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private TrainingType specialization;

    public Trainer(String firstName, String lastName, String username, String password, Boolean isActive,
                   TrainingType specialization) {
        super();
        this.user = User.builder().firstName(firstName).lastName(lastName).username(username).password(
                password).isActive(isActive).build();
        this.specialization = specialization;
    }
}
