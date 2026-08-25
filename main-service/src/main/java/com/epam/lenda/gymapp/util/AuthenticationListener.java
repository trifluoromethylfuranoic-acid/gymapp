package com.epam.lenda.gymapp.util;

import com.epam.lenda.gymapp.repository.UserRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AuthenticationListener {
    private final UserRepository userRepository;
    private final int maxLoginAttempts;

    public AuthenticationListener(UserRepository userRepository,
                                  @Value("${application.security.userBlock.maxLoginAttempts}") int maxLoginAttempts) {
        this.userRepository = userRepository;
        this.maxLoginAttempts = maxLoginAttempts;
    }

    @EventListener
    @Transactional
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        log.trace("User {} provided wrong credentials", event.getAuthentication().getName());
        String username = event.getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.setFailedLoginAttempts(0);
                user.setLockedAt(Instant.now());
            }
        });
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        log.trace("User {} successfully logged in", event.getAuthentication().getName());
        String username = event.getAuthentication().getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
        });
    }
}
