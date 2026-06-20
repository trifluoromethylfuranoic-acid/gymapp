package com.epam.lenda.gymapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.config.SecurityConfig;
import com.epam.lenda.gymapp.controller.rest.TraineeController;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.trainee.PatchTraineeRequest;
import com.epam.lenda.gymapp.filter.AuthFilter;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.TraineeService;
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

@WebMvcTest(TraineeController.class)
@Import({SecurityConfig.class, AuthFilter.class})
@ActiveProfiles("test")
public class TraineeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TraineeService traineeService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    public void search_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(get("/api/v1/trainees").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isOk());
        verify(traineeService).search(any(), any());
    }

    @Test
    public void search_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainees").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isForbidden());
        verify(traineeService, never()).search(any(), any());
    }

    @Test
    public void traineeProfile_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainees/john.smith").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isOk());
        verify(traineeService).getTraineeProfile(any());
    }

    @Test
    public void traineeProfile_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainees/ivan.petrov").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isForbidden());
        verify(traineeService, never()).getTraineeProfile(any());
    }

    @Test
    public void patchTraineeProfile_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        final var patchRequest = PatchTraineeRequest.builder().firstName("jack").build();
        mockMvc.perform(patch("/api/v1/trainees/john.smith").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
                        patchRequest))).andExpect(status().isOk());
        verify(traineeService).patchTraineeProfile(any(), any());
    }

    @Test
    public void patchTraineeProfile_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        final var patchRequest = PatchTraineeRequest.builder().firstName("jack").build();
        mockMvc.perform(patch("/api/v1/trainees/ivan.petrov").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
                        patchRequest))).andExpect(status().isForbidden());
        verify(traineeService, never()).patchTraineeProfile(any(), any());
    }

    @Test
    public void deleteTrainee_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(delete("/api/v1/trainees/john.smith").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isNoContent());
        verify(traineeService).deleteTrainee(any());
    }

    @Test
    public void deleteTrainee_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(delete("/api/v1/trainees/ivan.petrov").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isForbidden());
        verify(traineeService, never()).deleteTrainee(any());
    }

    @Test
    public void assignedTrainers_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainees/john.smith/trainers").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isOk());
        verify(traineeService).getAssignedTrainers(any());
    }

    @Test
    public void assignedTrainers_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainees/ivan.petrov/trainers").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isForbidden());
        verify(traineeService, never()).getAssignedTrainers(any());
    }

    @Test
    public void assignTrainer_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(post("/api/v1/trainees/ivan.petrov/trainers").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content("john.smith")).andExpect(status().isOk());
        verify(traineeService).assignTrainer(any(), any());
    }

    @Test
    public void assignTrainer_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(post("/api/v1/trainees/ivan.petrov/trainers").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content("jack.sparrow")).andExpect(
                        status().isForbidden());
        verify(traineeService, never()).assignTrainer(any(), any());
    }

    @Test
    public void unassignTrainer_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(delete("/api/v1/trainees/ivan.petrov/trainers/john.smith").with(
                SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(status().isOk());
        verify(traineeService).unassignTrainer(any(), any());
    }

    @Test
    public void unassignTrainer_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(delete("/api/v1/trainees/ivan.petrov/trainers/jack.sparrow").with(
                SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(status().isForbidden());
        verify(traineeService, never()).unassignTrainer(any(), any());
    }

    private UserDetails user(String username, Role role) {
        return UserDetails.builder().id(1L).username(username).password(null).isActive(true).role(role).build();
    }
}
