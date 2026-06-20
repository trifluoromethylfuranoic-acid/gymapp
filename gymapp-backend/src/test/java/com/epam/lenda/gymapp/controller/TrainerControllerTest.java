package com.epam.lenda.gymapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.config.SecurityConfig;
import com.epam.lenda.gymapp.controller.rest.TrainerController;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.trainer.PatchTrainerRequest;
import com.epam.lenda.gymapp.filter.AuthFilter;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.TrainerService;
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

@WebMvcTest(TrainerController.class)
@Import({SecurityConfig.class, AuthFilter.class})
@ActiveProfiles("test")
public class TrainerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerService trainerService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    public void search_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(get("/api/v1/trainers").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                status().isOk());
        verify(trainerService).search(any(), any());
    }

    @Test
    public void trainerProfile_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainers/ivan.petrov").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isOk());
        verify(trainerService).getTrainerProfile(any());
    }

    @Test
    public void patchTrainerProfile_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        final var patchRequest = PatchTrainerRequest.builder().firstName("jack").build();
        mockMvc.perform(patch("/api/v1/trainers/john.smith").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
                        patchRequest))).andExpect(status().isOk());
        verify(trainerService).patchTrainerProfile(any(), any());
    }

    @Test
    public void patchTrainerProfile_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        final var patchRequest = PatchTrainerRequest.builder().firstName("jack").build();
        mockMvc.perform(patch("/api/v1/trainers/ivan.petrov").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
                        patchRequest))).andExpect(status().isForbidden());
        verify(trainerService, never()).patchTrainerProfile(any(), any());
    }

    @Test
    public void deleteTrainer_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(delete("/api/v1/trainers/john.smith").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isNoContent());
        verify(trainerService).deleteTrainer(any());
    }

    @Test
    public void deleteTrainer_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(delete("/api/v1/trainers/ivan.petrov").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isForbidden());
        verify(trainerService, never()).deleteTrainer(any());
    }

    @Test
    public void assignedTrainees_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(get("/api/v1/trainers/ivan.petrov/trainees").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isOk());
        verify(trainerService).getAssignedTrainees(any());
    }

    @Test
    public void assignedTrainees_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/trainers/ivan.petrov/trainees").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(status().isForbidden());
        verify(trainerService, never()).getAssignedTrainees(any());
    }

    @Test
    public void assignTrainee_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(post("/api/v1/trainers/ivan.petrov/trainees").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content("john.smith")).andExpect(status().isOk());
        verify(trainerService).assignTrainee(any(), any());
    }

    @Test
    public void assignTrainee_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(post("/api/v1/trainers/ivan.petrov/trainees").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser)).contentType(MediaType.APPLICATION_JSON).content("jack.sparrow")).andExpect(
                        status().isForbidden());
        verify(trainerService, never()).assignTrainee(any(), any());
    }

    @Test
    public void unassignTrainee_success() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(delete("/api/v1/trainers/ivan.petrov/trainees/john.smith").with(
                SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(status().isOk());
        verify(trainerService).unassignTrainee(any(), any());
    }

    @Test
    public void unassignTrainee_accessDenied() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(delete("/api/v1/trainers/ivan.petrov/trainees/jack.sparrow").with(
                SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(status().isForbidden());
        verify(trainerService, never()).unassignTrainee(any(), any());
    }

    private UserDetails user(String username, Role role) {
        return UserDetails.builder().id(1L).username(username).password(null).isActive(true).role(role).build();
    }
}
