package com.epam.lenda.gymapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private Boolean isActive;
}