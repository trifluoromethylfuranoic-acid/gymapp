package com.epam.lenda.gymapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public abstract class User {
    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private Boolean isActive;
}
