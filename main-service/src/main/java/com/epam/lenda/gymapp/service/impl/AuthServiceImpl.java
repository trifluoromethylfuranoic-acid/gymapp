package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.dto.request.AuthenticationRequest;
import com.epam.lenda.gymapp.dto.request.ChangePasswordRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.AuthService;
import com.epam.lenda.gymapp.service.RefreshTokenService;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    @Override
    public Optional<GymUserDetails> authenticate(AuthenticationRequest authenticationRequest) {

        final var token = new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        try {
            final var principal = authenticationManager.authenticate(token).getPrincipal();
            if (principal instanceof GymUserDetails userDetails) {
                return Optional.of(userDetails);
            }
            return Optional.empty();
        } catch (AuthenticationException e) {
            return Optional.empty();
        }
    }

    @Override
    public GymUserDetails requireAuthentication(AuthenticationRequest authenticationRequest) {
        final var token = new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        return (GymUserDetails) authenticationManager.authenticate(token).getPrincipal();
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

        refreshTokenService.revokeAllForUser(username);

        return user;
    }
}
