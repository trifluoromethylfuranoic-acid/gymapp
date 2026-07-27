package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.request.AuthenticationRequest;
import com.epam.lenda.gymapp.dto.request.ChangePasswordRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.exception.WrongCredentialsException;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.AuthService;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> authenticate(AuthenticationRequest authenticationRequest) {
        return userRepository.findByUsername(authenticationRequest.getUsername()).filter(
                user -> passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword()));
    }

    @Override
    public User requireAuthentication(AuthenticationRequest authenticationRequest) {
        return authenticate(authenticationRequest).orElseThrow(WrongCredentialsException::new);
    }

    @Override
    @Transactional
    public @NonNull User updatePassword(@NonNull String username, @Nonnull ChangePasswordRequest request) {
        final var authentication = new AuthenticationRequest(username, request.getOldPassword());
        requireAuthentication(authentication);

        final var user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("user", username));
        final var passwordHash = passwordEncoder.encode(request.getNewPassword());

        user.setPassword(passwordHash);

        return user;
    }
}
