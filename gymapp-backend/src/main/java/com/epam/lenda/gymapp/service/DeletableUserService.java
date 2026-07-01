package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;

public interface DeletableUserService<T extends User> extends BaseUserService<T> {
    void delete(@Nonnull String username);
}
