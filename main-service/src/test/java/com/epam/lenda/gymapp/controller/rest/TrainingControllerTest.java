package com.epam.lenda.gymapp.controller.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.TestConfig;
import com.epam.lenda.gymapp.TestSecurityConfig;
import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.common.security.Role;
import com.epam.lenda.gymapp.common.security.UserPrincipal;
import com.epam.lenda.gymapp.dto.response.TrainingResponse;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.TrainingMapper;
import com.epam.lenda.gymapp.service.TrainingService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrainingController.class)
@Import({TestSecurityConfig.class, TestConfig.class})
@ActiveProfiles("test")
class TrainingControllerTest {
    private static final String VALID_CREATE_REQUEST = """
            {
              "trainee": "john.doe",
              "trainer": "jane.smith",
              "name": "Morning workout",
              "type": "Fitness",
              "datetime": "2026-07-27T10:00:00+00:00",
              "durationMinutes": 60
            }
            """;
    private static final UserPrincipal USER_PRINCIPAL = UserPrincipal
            .builder()
            .username("john.doe")
            .role(Role.ROLE_TRAINEE)
            .id(UUID.randomUUID())
            .build();
    private static final TestingAuthenticationToken AUTH = new TestingAuthenticationToken(
            USER_PRINCIPAL, "n/a", Role.ROLE_TRAINEE.toString());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private TrainingMapper trainingMapper;

    @Test
    void getTrainings_returnsTrainingList() throws Exception {
        final var training = Util.training();
        final var response = TrainingResponse
                .builder()
                .name("Morning workout")
                .datetime(Util.dateTime("2026-07-27 10:00:00"))
                .type("Fitness")
                .durationMinutes(60)
                .trainee("john.doe")
                .trainer("jane.smith")
                .build();

        when(trainingService.search(any())).thenReturn(List.of(training));
        when(trainingMapper.toDto(training)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainings")
                                .param("traineeUsername", "john.doe")
                                .with(authentication(AUTH)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].name").value("Morning workout"))
               .andExpect(jsonPath("$[0].trainee").value("john.doe"));
    }

    @Test
    void getTrainings_returnsBadRequestForInvalidDate() throws Exception {
        mockMvc.perform(get("/api/v1/trainings")
                                .param("fromDateInclusive", "invalid-date"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void createTraining_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/trainings")
                                .with(authentication(AUTH))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_CREATE_REQUEST))
               .andExpect(status().isCreated());
    }

    @Test
    void createTraining_returnsBadRequestForMalformedRequest() throws Exception {
        mockMvc.perform(post("/api/v1/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void createTraining_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(trainingService).create(any());

        mockMvc.perform(post("/api/v1/trainings")
                                .with(authentication(AUTH))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_CREATE_REQUEST))
               .andExpect(status().isNotFound());
    }
}
