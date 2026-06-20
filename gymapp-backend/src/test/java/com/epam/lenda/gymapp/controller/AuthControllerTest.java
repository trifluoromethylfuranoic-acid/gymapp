package com.epam.lenda.gymapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.config.SecurityConfig;
import com.epam.lenda.gymapp.controller.rest.AuthController;
import com.epam.lenda.gymapp.dto.auth.SignupResponse;
import com.epam.lenda.gymapp.dto.auth.SignupTraineeRequest;
import com.epam.lenda.gymapp.dto.auth.SignupTrainerRequest;
import com.epam.lenda.gymapp.filter.AuthFilter;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthFilter.class})
@ActiveProfiles("test")
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    public void signupTrainee_success() throws Exception {
        when(authService.signupTrainee(any())).thenReturn(SignupResponse
                .builder()
                .username("user.name")
                .password("aaaa")
                .accessToken("aaaa")
                .build());
        final var signupRequest = SignupTraineeRequest
                .builder()
                .firstName("hello")
                .lastName("world")
                .build();
        mockMvc.perform(post("/api/v1/signup/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());
        verify(authService).signupTrainee(signupRequest);
    }

    @Test
    public void signupTrainee_invalidFirstName() throws Exception {
        when(authService.signupTrainee(any())).thenReturn(SignupResponse
                .builder()
                .username("user.name")
                .password("aaaa")
                .accessToken("aaaa")
                .build());
        final var signupRequest = SignupTraineeRequest
                .builder()
                .firstName("hello123")
                .lastName("world")
                .build();
        mockMvc.perform(post("/api/v1/signup/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signupTrainer_success() throws Exception {
        when(authService.signupTrainer(any())).thenReturn(SignupResponse
                .builder()
                .username("user.name")
                .password("aaaa")
                .accessToken("aaaa")
                .build());
        final var signupRequest = SignupTrainerRequest
                .builder()
                .firstName("hello")
                .lastName("world")
                .specialization("Zumba")
                .build();
        mockMvc.perform(post("/api/v1/signup/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());
        verify(authService).signupTrainer(signupRequest);
    }

    @Test
    public void signupTrainer_invalidFirstName() throws Exception {
        when(authService.signupTrainer(any())).thenReturn(SignupResponse
                .builder()
                .username("user.name")
                .password("aaaa")
                .accessToken("aaaa")
                .build());
        final var signupRequest = SignupTrainerRequest
                .builder()
                .firstName("hello123")
                .lastName("world")
                .specialization("Zumba")
                .build();
        mockMvc.perform(post("/api/v1/signup/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }
}
