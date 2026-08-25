package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.epam.lenda.gymapp.dto.request.CreateUserRequest;
import com.epam.lenda.gymapp.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CredentialsServiceTest {
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private SecureRandom secureRandom;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CredentialsService credentialsService;

    private final int defaultPasswordLength = 10;

    @BeforeAll
    void setup() {
        ReflectionTestUtils.setField(credentialsService, "defaultPasswordLength", defaultPasswordLength);
    }

    @Test
    void generateUsername_concatenates() {
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        assertThat(credentialsService.generateUsername(new CreateUserRequest("John", "Doe"))).isEqualTo(
                "John.Doe");
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void generateUsername_appendsCounterUntilUnique() {
        when(userRepository.existsByUsername("John.Doe")).thenReturn(true);
        when(userRepository.existsByUsername("John.Doe1")).thenReturn(true);
        when(userRepository.existsByUsername("John.Doe2")).thenReturn(false);

        assertThat(credentialsService.generateUsername(new CreateUserRequest("John", "Doe"))).isEqualTo(
                "John.Doe2");
    }

    @Test
    void generatePassword_hasCorrectLength() {
        stubNextBytes();

        assertThat(credentialsService.generatePassword()).hasSize(defaultPasswordLength);
    }

    @Test
    void generateCredentials_encodesPassword() {
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        stubNextBytes();

        credentialsService.generateCredentials(new CreateUserRequest("John", "Doe"));

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
