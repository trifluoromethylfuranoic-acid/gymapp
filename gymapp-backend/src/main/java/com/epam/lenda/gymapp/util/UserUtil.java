package com.epam.lenda.gymapp.util;

import com.epam.lenda.gymapp.model.HasId;
import com.epam.lenda.gymapp.repository.BaseUserRepository;
import jakarta.annotation.Nonnull;

public class UserUtil {
    @SafeVarargs
    public static boolean isUsernameTaken(@Nonnull String newUsername,
                                          long id,
                                          @Nonnull BaseUserRepository<? extends HasId> currentRepository,
                                          @Nonnull BaseUserRepository<? extends HasId>... otherRepositories) {
        final var userOpt = currentRepository.findByUsername(newUsername);
        if (userOpt.isPresent() && !userOpt.get().getId().equals(id)) {
            return true;
        }

        for (var repository : otherRepositories) {
            if (repository.findByUsername(newUsername).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
