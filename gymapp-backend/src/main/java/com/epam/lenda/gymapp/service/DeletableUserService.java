package com.epam.lenda.gymapp.service;

import jakarta.annotation.Nonnull;

public interface DeletableUserService {
    void delete(@Nonnull String username);
}
