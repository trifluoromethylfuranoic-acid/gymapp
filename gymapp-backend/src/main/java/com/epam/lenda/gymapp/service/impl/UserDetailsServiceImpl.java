package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    @Nonnull
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        final var traineeOpt = traineeRepository.findByUsername(username);
        final User userEntity;
        final Role role;
        if (traineeOpt.isPresent()) {
            userEntity = traineeOpt.get().getUser();
            role = Role.TRAINEE;
        } else {
            var trainer = trainerRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
            userEntity = trainer.getUser();
            role = Role.TRAINER;
        }
        return UserDetails.builder().id(userEntity.getId()).username(userEntity.getUsername()).password(
                userEntity.getPassword()).isActive(userEntity.getIsActive()).role(role).build();
    }
}
