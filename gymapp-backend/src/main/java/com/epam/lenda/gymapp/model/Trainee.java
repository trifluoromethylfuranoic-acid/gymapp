package com.epam.lenda.gymapp.model;

import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class Trainee extends User implements HasId {
    private Long id;

    private LocalDate dateOfBirth;

    private String address;

    public Trainee(String firstName, String lastName, String username, String password, Boolean isActive, Long id,
                   LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
}
