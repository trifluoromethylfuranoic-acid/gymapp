package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.WrongCredentialsException;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.AuthService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
