package com.epam.lenda.gymapp.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    TRAINER, TRAINEE;

    @Override
    public String getAuthority() {
        return toString();
    }
}
