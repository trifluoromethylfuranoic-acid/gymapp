package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.service.impl.UserDetailsServiceImpl;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserDetailsServiceTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_returnsTraineePrincipalWhenTraineeExists() {
        when(traineeRepository.findByUsername("trainee.username"))
                .thenReturn(Optional.of(trainee("trainee.username")));

        var userDetails = (UserDetails) userDetailsService.loadUserByUsername("trainee.username");

        assertThat(userDetails.getUsername()).isEqualTo("trainee.username");
        assertThat(userDetails.getRole()).isEqualTo(Role.TRAINEE);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_returnsTrainerPrincipalWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("trainer.username")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("trainer.username"))
                .thenReturn(Optional.of(trainer("trainer.username")));

        var userDetails = (UserDetails) userDetailsService.loadUserByUsername("trainer.username");

        assertThat(userDetails.getUsername()).isEqualTo("trainer.username");
        assertThat(userDetails.getRole()).isEqualTo(Role.TRAINER);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_throwsWhenUserDoesNotExist() {
        when(traineeRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Trainee trainee(String username) {
        return Trainee.builder().id(1L).user(user(username)).trainerAssignments(new ArrayList<>()).trainings(
                new ArrayList<>()).build();
    }

    private static Trainer trainer(String username) {
        return Trainer.builder().id(2L).user(user(username)).specialization(TrainingType.builder().name(
                "Fitness").build()).traineeAssignments(new ArrayList<>()).trainings(new ArrayList<>()).build();
    }

    private static User user(String username) {
        return User.builder().id(10L).firstName("First").lastName("Last").username(username).password(
                "password").isActive(true).build();
    }
}
