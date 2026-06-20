package com.epam.lenda.gymapp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

import com.epam.lenda.gymapp.config.SecurityConfig;
import com.epam.lenda.gymapp.controller.rest.ProfileController;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.filter.AuthFilter;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, AuthFilter.class})
@ActiveProfiles("test")
public class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    public void profile_forwardsToTrainerProfile() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(get("/api/v1/profile").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                forwardedUrl("/api/v1/trainers/john.smith"));
    }

    @Test
    public void profile_forwardsToTraineeProfile() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/profile").with(SecurityMockMvcRequestPostProcessors.user(mockUser))).andExpect(
                forwardedUrl("/api/v1/trainees/john.smith"));
    }

    @Test
    public void profile_forwardsToTrainerTrainees() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINER);
        mockMvc.perform(get("/api/v1/profile/trainees").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(forwardedUrl("/api/v1/trainers/john.smith/trainees"));
    }

    @Test
    public void profile_forwardsToTraineeTrainers() throws Exception {
        final var mockUser = user("john.smith", Role.TRAINEE);
        mockMvc.perform(get("/api/v1/profile/trainers").with(SecurityMockMvcRequestPostProcessors.user(
                mockUser))).andExpect(forwardedUrl("/api/v1/trainees/john.smith/trainers"));
    }

    private UserDetails user(String username, Role role) {
        return UserDetails.builder().id(1L).username(username).password(null).isActive(true).role(role).build();
    }
}
