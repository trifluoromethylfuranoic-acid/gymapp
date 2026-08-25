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
public class Admin extends AbstractEntity implements IsUser {
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    public Admin(String firstName, String lastName, String username, String password, Boolean isActive) {
        super();
        this.user = User.builder().firstName(firstName).lastName(lastName).username(username).password(
                password).isActive(isActive).build();
    }
}
