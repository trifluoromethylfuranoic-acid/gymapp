package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.request.AuthenticationRequest;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.impl.AuthServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({AuthServiceImpl.class})
class AuthServiceTest {
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Test
    void authenticate_returnsUserWhenPasswordMatches() {
        var user = user("trainee.username", "encoded-password");

        when(userRepository.findByUsername("trainee.username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        var authenticated = authService.authenticate(new AuthenticationRequest("trainee.username",
                "raw-password"));

        assertThat(authenticated).containsSame(user);
    }

    @Test
    void authenticate_returnsEmptyWhenPasswordDoesNotMatch() {
        var user = user("trainee.username", "encoded-password");

        when(userRepository.findByUsername("trainee.username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        var authenticated = authService.authenticate(new AuthenticationRequest("trainee.username",
                "wrong-password"));

        assertThat(authenticated).isEmpty();
    }

    @Test
    void authenticate_returnsEmptyWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing.username")).thenReturn(Optional.empty());

        var authenticated = authService.authenticate(new AuthenticationRequest("missing.username",
                                                                               "raw-password"));

        assertThat(authenticated).isEmpty();
    }

    private static User user(String username, String password) {
        return User.builder().firstName("First").lastName("Last").username(username).password(password).isActive(
                true).build();
    }
}
