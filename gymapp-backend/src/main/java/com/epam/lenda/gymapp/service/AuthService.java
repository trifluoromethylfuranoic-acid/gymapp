package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.User;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

public interface AuthService {
    Optional<User> authenticate(AuthenticationRequest authenticationRequest);

    User requireAuthentication(AuthenticationRequest authenticationRequest);

    @Getter
    @AllArgsConstructor
    @Builder
    @Jacksonized
    class AuthenticationRequest {
        private String username;

        private String password;
    }
}
