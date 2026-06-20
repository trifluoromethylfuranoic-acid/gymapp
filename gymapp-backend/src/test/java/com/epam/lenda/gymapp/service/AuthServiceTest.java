package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.auth.PasswordChangeRequest;
import com.epam.lenda.gymapp.dto.auth.SignupTraineeRequest;
import com.epam.lenda.gymapp.dto.auth.SignupTrainerRequest;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.exception.WrongPasswordException;
import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.Trainer;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.repository.TrainingTypeRepository;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.service.impl.AuthServiceImpl;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TrainerRepository trainerRepository;
    @MockitoBean
    private TraineeRepository traineeRepository;
    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;
    @MockitoBean
    private SecureRandom secureRandom;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthServiceImpl authService;

    @Value("${application.security.defaultPasswordLength}")
    private int defaultPasswordLength;

    @Test
    void signupTrainer_createsActiveTrainerWithGeneratedCredentials() {
        var specialization = TrainingType.builder().id(1L).name("Fitness").build();
        var request = SignupTrainerRequest.builder().firstName("John").lastName("Doe").specialization(
                "Fitness").build();

        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(trainingTypeRepository.findOrCreate("Fitness")).thenReturn(specialization);

        var response = authService.signupTrainer(request);

        assertThat(response.username()).isEqualTo("John.Doe");
        assertThat(response.password()).hasSize(defaultPasswordLength);
        var captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
        assertThat(captor.getValue().getSpecialization()).isSameAs(specialization);
    }

    @Test
    void signupTrainee_createsActiveTraineeWithGeneratedCredentials() {
        var request = SignupTraineeRequest.builder().firstName("Jane").lastName("Doe").dateOfBirth(LocalDate.of(1990, 1,
                1)).address("Address").build();

        when(userRepository.findByUsername("Jane.Doe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        var response = authService.signupTrainee(request);

        assertThat(response.username()).isEqualTo("Jane.Doe");
        assertThat(response.password()).hasSize(defaultPasswordLength);
        var captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getUser().getIsActive()).isTrue();
        assertThat(captor.getValue().getAddress()).isEqualTo("Address");
    }

    @Test
    void generateUsername_appendsCounterUntilUnique() {
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.findByUsername("John.Doe1")).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.findByUsername("John.Doe2")).thenReturn(Optional.empty());

        assertThat(authService.generateUsername("John", "Doe")).isEqualTo("John.Doe2");
    }

    @Test
    void authenticate_returnsAuthenticationWhenManagerSucceeds() {
        var authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        assertThat(authService.authenticate("user", "password")).contains(authentication);
    }

    @Test
    void authenticateReturns_emptyWhenManagerRejectsCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThat(authService.authenticate("user", "password")).isEmpty();
    }

    @Test
    void changePassword_encodesNewPasswordWhenOldPasswordIsValid() {
        var user = User.builder().username("user").password("old").build();
        var request = PasswordChangeRequest.builder().oldPassword("old-password").newPassword("new-password").build();

        when(authenticationManager.authenticate(any())).thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        authService.changePassword("user", request);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    void changePassword_throwsWhenOldPasswordIsInvalid() {
        var request = PasswordChangeRequest.builder().oldPassword("bad-password").newPassword("new-password").build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.changePassword("user", request)).isInstanceOf(
                WrongPasswordException.class);
    }

    @Test
    void changePassword_throwsWhenUserDoesNotExist() {
        var request = PasswordChangeRequest.builder().oldPassword("old-password").newPassword("new-password").build();

        when(authenticationManager.authenticate(any())).thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword("missing", request)).isInstanceOf(
                ResourceNotFoundException.class);
    }
}
