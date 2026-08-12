package com.epam.lenda.gymapp.controller.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.TestSecurityConfig;
import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.dto.response.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.response.TrainerResponse;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.IllegalStateTransitionException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.model.RefreshToken;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.RefreshTokenService;
import com.epam.lenda.gymapp.service.TraineeService;
import com.epam.lenda.gymapp.util.Pair;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TraineeController.class)
@Import({TestSecurityConfig.class})
@ActiveProfiles("test")
class TraineeControllerTest {
    private static final String VALID_CREATE_REQUEST = """
            {
              "firstName": "John",
              "lastName": "Doe",
              "dateOfBirth": "1990-01-01",
              "address": "Main Street"
            }
            """;
    private static final String VALID_UPDATE_REQUEST = """
            {
              "firstName": "John",
              "lastName": "Doe",
              "isActive": true,
              "dateOfBirth": "1990-01-01",
              "address": "Main Street"
            }
            """;
    private static final GymUserDetails USER_DETAILS = GymUserDetails
            .builder()
            .username("john.doe")
            .role(Role.ROLE_TRAINEE)
            .isActive(true)
            .id(UUID.randomUUID())
            .build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TraineeService traineeService;
    @MockitoBean
    private UserMapper userMapper;
    @MockitoBean
    private AccessTokenService accessTokenService;
    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Test
    void createTrainee_returnsCreatedSignupResponse() throws Exception {
        final var trainee = Util.trainee("john.doe");
        when(traineeService.create(any())).thenReturn(Pair.of(trainee, "password"));
        when(userMapper.toUserDetails(any())).thenReturn(
                GymUserDetails
                        .builder()
                        .id(trainee.getUser().getId())
                        .username(trainee.getUser().getUsername())
                        .password(trainee.getUser().getPassword())
                        .isActive(trainee.getUser().getIsActive())
                        .isLocked(trainee.getUser().getLockedAt() != null)
                        .role(trainee.getUser().getRole()).build());

        when(accessTokenService.encode(any())).thenReturn("access token");
        when(refreshTokenService.create(any())).thenReturn(Pair.of(new RefreshToken(), "refresh token"));

        mockMvc.perform(post("/api/v1/trainees")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_CREATE_REQUEST))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.username").value("john.doe"))
               .andExpect(jsonPath("$.password").value("password"));
    }

    @Test
    void createTrainee_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/trainees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void createTrainee_returnsConflictForDuplicateUsername() throws Exception {
        when(traineeService.create(any())).thenThrow(new DuplicateUsernameException());

        mockMvc.perform(post("/api/v1/trainees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_CREATE_REQUEST))
               .andExpect(status().isConflict());
    }

    @Test
    void getTrainee_returnsProfile() throws Exception {
        final var trainee = Util.trainee("john.doe");
        final var trainer = Util.trainer("jane.smith");
        final var response = FullTraineeResponse
                .builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(Util.date("1990-01-01"))
                .address("Main Street")
                .isActive(true)
                .trainers(List.of(trainerResponse("jane.smith")))
                .build();

        when(traineeService.findByUsername("john.doe")).thenReturn(trainee);
        when(traineeService.getTrainerList("john.doe")).thenReturn(List.of(trainer));
        when(userMapper.toDto(trainee, List.of(trainer))).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("john.doe"))
               .andExpect(jsonPath("$.trainers", hasSize(1)))
               .andExpect(jsonPath("$.trainers[0].username").value("jane.smith"));
    }

    @Test
    void getTrainee_returnsNotFound() throws Exception {
        when(traineeService.findByUsername("missing")).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get("/api/v1/trainees/missing"))
               .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainee_returnsUpdatedProfile() throws Exception {
        final var trainee = Util.trainee("john.doe");
        final var response = FullTraineeResponse
                .builder().username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .address("Main Street")
                .isActive(true)
                .trainers(List.of())
                .build();

        when(traineeService.update(eq("john.doe"), any())).thenReturn(trainee);
        when(traineeService.getTrainerList("john.doe")).thenReturn(List.of());
        when(userMapper.toDto(trainee, List.of())).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees/john.doe")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_REQUEST))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("john.doe"))
               .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void updateTrainee_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(put("/api/v1/trainees/john.doe")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainee_returnsNotFound() throws Exception {
        when(traineeService.update(eq("missing"), any())).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(put("/api/v1/trainees/missing")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_REQUEST))
               .andExpect(status().isForbidden());
    }

    @Test
    void updateActiveStatus_returnsOk() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/john.doe")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"isActive\": false}"))
               .andExpect(status().isOk());
    }

    @Test
    void updateActiveStatus_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/john.doe")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void updateActiveStatus_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(traineeService).updateActiveStatus("missing", false);

        mockMvc.perform(patch("/api/v1/trainees/missing")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON).content("{\"isActive\": false}"))
               .andExpect(status().isForbidden());
    }

    @Test
    void updateActiveStatus_returnsConflict() throws Exception {
        doThrow(new IllegalStateTransitionException()).when(traineeService).updateActiveStatus("john.doe", false);

        mockMvc.perform(patch("/api/v1/trainees/john.doe")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"isActive\": false}"))
               .andExpect(status().isConflict());
    }

    @Test
    void deleteTrainee_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees/john.doe")
                                .with(user(USER_DETAILS)))
               .andExpect(status().isNoContent());
    }

    @Test
    void deleteTrainee_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(traineeService).delete("missing");

        mockMvc.perform(delete("/api/v1/trainees/missing")
                                .with(user(USER_DETAILS)))
               .andExpect(status().isForbidden());
    }

    @Test
    void getAssignedTrainers_returnsTrainerList() throws Exception {
        final var trainer = Util.trainer("jane.smith");
        final var response = trainerResponse("jane.smith");
        when(traineeService.getTrainerList("john.doe")).thenReturn(List.of(trainer));
        when(userMapper.toDto(trainer)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe/trainers"))
               .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].username").value("jane.smith"));
    }

    @Test
    void getAssignedTrainers_returnsUnassignedTrainerList() throws Exception {
        final var trainer = Util.trainer("jane.smith");
        final var response = trainerResponse("jane.smith");
        when(traineeService.findActiveTrainersNotAssignedToTrainee("john.doe")).thenReturn(List.of(trainer));
        when(userMapper.toDto(trainer)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/john.doe/trainers")
                                .param("unassigned", "true"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].username").value("jane.smith"));
    }

    @Test
    void getAssignedTrainers_returnsNotFound() throws Exception {
        when(traineeService.getTrainerList("missing")).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get("/api/v1/trainees/missing/trainers"))
               .andExpect(status().isNotFound());
    }

    @Test
    void updateAssignedTrainers_returnsTrainerList() throws Exception {
        final var trainer = Util.trainer("jane.smith");
        final var response = trainerResponse("jane.smith");
        when(traineeService.updateTrainerList("john.doe", List.of("jane.smith"))).thenReturn(List.of(trainer));
        when(userMapper.toDto(trainer)).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees/john.doe/trainers")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"trainers\": [\"jane.smith\"]} "))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].username").value("jane.smith"));
    }

    @Test
    void updateAssignedTrainers_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(put("/api/v1/trainees/john.doe/trainers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
               .andExpect(status().isBadRequest());
    }

    @Test
    void updateAssignedTrainers_returnsNotFound() throws Exception {
        when(traineeService.updateTrainerList("missing", List.of("jane.smith")))
                .thenThrow(new ResourceNotFoundException());

        mockMvc.perform(put("/api/v1/trainees/missing/trainers")
                                .with(user(USER_DETAILS))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"trainers\": [\"jane.smith\"]}"))
               .andExpect(status().isForbidden());
    }

    private static TrainerResponse trainerResponse(String username) {
        return TrainerResponse
                .builder()
                .username(username)
                .firstName("Jane")
                .lastName("Smith")
                .specialization(
                "Fitness")
                .isActive(true)
                .build();
    }


}
