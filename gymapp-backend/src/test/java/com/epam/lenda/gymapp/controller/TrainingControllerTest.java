package com.epam.lenda.gymapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.config.SecurityConfig;
import com.epam.lenda.gymapp.controller.rest.TrainingController;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.training.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.training.TrainingResponse;
import com.epam.lenda.gymapp.filter.AuthFilter;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.TrainingService;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TrainingController.class)
@Import({SecurityConfig.class, AuthFilter.class})
@ActiveProfiles("test")
public class TrainingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingService trainingService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    public void search_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(get("/api/v1/trainings").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isOk());
        verify(trainingService).search(any(), any());
    }

    @Test
    public void search_forTraineeSuccess() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainings").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isOk());
        verify(trainingService).searchForTrainee(any(), any(), any());
    }

    @Test
    public void createTraining_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        final var createRequest = CreateTrainingRequest.builder().trainee("ivan.petrov").trainer("john.smith").name(
                "jjj").trainingType("aaa").datetime(ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneId.of("Z"))).duration(
                        Duration.ofMinutes(1)).build();

        mockMvc.perform(post("/api/v1/trainings").with(SecurityMockMvcRequestPostProcessors.user(mockUser)).contentType(
                MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest))).andExpect(
                        status().isCreated());
        verify(trainingService).createTraining(any());
    }

    @Test
    public void createTraining_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        final var createRequest = CreateTrainingRequest.builder().trainee("ivan.petrov").trainer("jack.sparrow").name(
                "jjj").trainingType("aaa").datetime(dateTime()).duration(Duration.ofMinutes(1)).build();

        mockMvc.perform(post("/api/v1/trainings").with(SecurityMockMvcRequestPostProcessors.user(mockUser)).contentType(
                MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createRequest))).andExpect(
                        status().isForbidden());
        verify(trainingService, never()).createTraining(any());
    }

    @Test
    public void getTraining_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        when(trainingService.getTraining(1)).thenReturn(TrainingResponse.builder().trainee("john.smith").trainer(
                "ivan.petrov").name("aa").trainingType("aaa").datetime(dateTime()).duration(Duration.ofMinutes(
                        1)).build());
        mockMvc.perform(get("/api/v1/trainings/1").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isOk());
        verify(trainingService).getTraining(1);
    }

    @Test
    public void getTraining_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        when(trainingService.getTraining(1)).thenReturn(TrainingResponse.builder().trainee("jack.sparrow").trainer(
                "ivan.petrov").name("aa").trainingType("aaa").datetime(dateTime()).duration(Duration.ofMinutes(
                        1)).build());
        mockMvc.perform(get("/api/v1/trainings/1").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isForbidden());
    }

    private UserDetails user(String username, Role role) {
        return UserDetails.builder().id(1L).username(username).password(null).isActive(true).role(role).build();
    }

    private ZonedDateTime dateTime() {
        return ZonedDateTime.of(
                2026, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")
        );
    }
}
