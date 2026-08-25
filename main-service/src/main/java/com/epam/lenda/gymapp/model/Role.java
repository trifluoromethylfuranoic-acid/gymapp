package com.epam.lenda.gymapp.model;

import jakarta.annotation.Nonnull;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_TRAINEE, ROLE_TRAINER, ROLE_ADMIN;

    @Override
    public @Nonnull String getAuthority() {
        return toString();
    }
}
