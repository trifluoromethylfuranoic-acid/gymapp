package com.epam.lenda.gymapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Trainee extends AbstractEntity implements IsUser {
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @Column(nullable = true)
    private LocalDate dateOfBirth;

    @Column(nullable = true)
    private String address;

    public Trainee(String firstName, String lastName, String username, String password, Boolean isActive,
                   LocalDate dateOfBirth, String address) {
        super();
        this.user = User.builder().firstName(firstName).lastName(lastName).username(username).password(
                password).isActive(isActive).build();
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
}
