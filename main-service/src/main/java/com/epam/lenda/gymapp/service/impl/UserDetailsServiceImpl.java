package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.repository.UserRepository;
import jakarta.annotation.Nonnull;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final int blockDurationSeconds;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  UserMapper userMapper,
                                  @Value("${application.security.userBlock.durationSeconds}") int blockDurationSeconds) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.blockDurationSeconds = blockDurationSeconds;
    }

    @Override
    @Transactional
    public @Nonnull UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        final var user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                "Username not found"));
        if (user.getLockedAt() != null
                && user.getLockedAt().plusSeconds(blockDurationSeconds).isBefore(Instant.now())) {
            user.setFailedLoginAttempts(0);
            user.setLockedAt(null);
        }

        return userMapper.toUserDetails(user);
    }
}
