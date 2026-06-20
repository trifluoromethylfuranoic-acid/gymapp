package com.epam.lenda.gymapp.util;

import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;

public class SearchUtil {
    public static boolean userMatches(@Nonnull User user, @Nonnull String query) {
        return user.getUsername().toLowerCase().contains(query) || user.getFirstName().toLowerCase().contains(
                query) || user.getLastName().toLowerCase().contains(query);
    }
}
