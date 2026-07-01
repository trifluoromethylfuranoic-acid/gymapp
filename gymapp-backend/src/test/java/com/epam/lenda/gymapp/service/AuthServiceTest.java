package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.repository.TraineeRepository;
import com.epam.lenda.gymapp.repository.TrainerRepository;
import com.epam.lenda.gymapp.service.impl.AuthServiceImpl;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest {
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private SecureRandom secureRandom;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private final int defaultPasswordLength = 10;

    @BeforeEach
    void setup() {
        authService = new AuthServiceImpl(traineeRepository,
                trainerRepository,
                secureRandom,
                passwordEncoder);
        ReflectionTestUtils.setField(authService, "defaultPasswordLength", defaultPasswordLength);
    }

    @Test
    void generateUsername_concatenates() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        assertThat(authService.generateUsername("John", "Doe")).isEqualTo("John.Doe");
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void generateUsername_appendsCounterUntilUnique() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(new Trainee()));
        when(traineeRepository.findByUsername("John.Doe1")).thenReturn(Optional.of(new Trainee()));
        when(traineeRepository.findByUsername("John.Doe2")).thenReturn(Optional.empty());

        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("John.Doe1")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("John.Doe2")).thenReturn(Optional.empty());

        assertThat(authService.generateUsername("John", "Doe")).isEqualTo("John.Doe2");
    }

    @Test
    void generatePassword_hasCorrectLength() {
        stubNextBytes();

        assertThat(authService.generatePassword()).hasSize(defaultPasswordLength);
    }

    @Test
    void generateCredentials_encodesPassword() {
        when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        stubNextBytes();

        authService.generateCredentials("John", "Doe");

        verify(passwordEncoder).encode(any());
    }

    private void stubNextBytes() {
        doAnswer(invocation -> {
            var bytes = (byte[]) invocation.getArgument(0);
            Arrays.fill(bytes, (byte) 0x7C);
            return null;
        }).when(secureRandom).nextBytes(any());
    }
}
