package com.epam.lenda.gymapp.controller.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.Util;
import com.epam.lenda.gymapp.config.SecurityConfig;
import com.epam.lenda.gymapp.dto.response.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.response.TraineeResponse;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.IllegalStateTransitionException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.service.TrainerService;
import com.epam.lenda.gymapp.util.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrainerController.class)
@Import(SecurityConfig.class)
class TrainerControllerTest {
    private static final String VALID_CREATE_REQUEST = """
            {
              "firstName": "Jane",
              "lastName": "Smith",
              "specialization": "Fitness"
            }
            """;
    private static final String VALID_UPDATE_REQUEST = """
            {
              "firstName": "Jane",
              "lastName": "Smith",
              "isActive": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void createTrainer_returnsCreatedSignupResponse() throws Exception {
        final var trainer = Util.trainer("jane.smith");
        when(trainerService.create(any())).thenReturn(Pair.of(trainer, "password"));

        mockMvc.perform(post("/api/v1/trainers").contentType(MediaType.APPLICATION_JSON).content(
                VALID_CREATE_REQUEST)).andExpect(status().isCreated()).andExpect(jsonPath("$.username").value(
                        "jane.smith")).andExpect(jsonPath("$.password").value("password"));
    }

    @Test
    void createTrainer_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/trainers").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(
                status().isBadRequest());
    }

    @Test
    void createTrainer_returnsConflictForDuplicateUsername() throws Exception {
        when(trainerService.create(any())).thenThrow(new DuplicateUsernameException());

        mockMvc.perform(post("/api/v1/trainers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_CREATE_REQUEST))
                .andExpect(status().isConflict());
    }

    @Test
    void getTrainer_returnsProfile() throws Exception {
        final var trainer = Util.trainer("jane.smith");
        final var trainee = Util.trainee("john.doe");
        final var response = FullTrainerResponse.builder().username("jane.smith").firstName("Jane").lastName(
                "Smith").specialization("Fitness").isActive(true).trainees(List.of(traineeResponse(
                        "john.doe"))).build();
        when(trainerService.findByUsername("jane.smith")).thenReturn(trainer);
        when(trainerService.getTraineeList("jane.smith")).thenReturn(List.of(trainee));
        when(userMapper.toDto(trainer, List.of(trainee))).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/jane.smith")).andExpect(status().isOk()).andExpect(jsonPath(
                "$.username").value("jane.smith")).andExpect(jsonPath("$.trainees", hasSize(1))).andExpect(jsonPath(
                        "$.trainees[0].username").value("john.doe"));
    }

    @Test
    void getTrainer_returnsNotFound() throws Exception {
        when(trainerService.findByUsername("missing")).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(get("/api/v1/trainers/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainer_returnsUpdatedProfile() throws Exception {
        final var trainer = Util.trainer("jane.smith");
        final var response = FullTrainerResponse.builder().username("jane.smith").firstName("Jane").lastName(
                "Smith").specialization("Fitness").isActive(true).trainees(List.of()).build();
        when(trainerService.update(eq("jane.smith"), any())).thenReturn(trainer);
        when(trainerService.getTraineeList("jane.smith")).thenReturn(List.of());
        when(userMapper.toDto(trainer, List.of())).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainers/jane.smith").contentType(MediaType.APPLICATION_JSON).content(
                VALID_UPDATE_REQUEST)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value(
                        "jane.smith")).andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void updateTrainer_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(put("/api/v1/trainers/jane.smith").contentType(MediaType.APPLICATION_JSON).content(
                "{}")).andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainer_returnsNotFound() throws Exception {
        when(trainerService.update(eq("missing"), any())).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(put("/api/v1/trainers/missing")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_REQUEST))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActiveStatus_returnsOk() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/jane.smith").contentType(MediaType.APPLICATION_JSON).content("""
                {"isActive": false}
                """)).andExpect(status().isOk());
    }

    @Test
    void updateActiveStatus_returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/jane.smith").contentType(MediaType.APPLICATION_JSON).content(
                "{}")).andExpect(status().isBadRequest());
    }

    @Test
    void updateActiveStatus_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException()).when(trainerService).updateActiveStatus("missing", false);

        mockMvc.perform(patch("/api/v1/trainers/missing").contentType(MediaType.APPLICATION_JSON).content("""
                {"isActive": false}
                """)).andExpect(status().isNotFound());
    }

    @Test
    void updateActiveStatus_returnsConflict() throws Exception {
        doThrow(new IllegalStateTransitionException()).when(trainerService).updateActiveStatus("jane.smith", false);

        mockMvc.perform(patch("/api/v1/trainers/jane.smith").contentType(MediaType.APPLICATION_JSON).content("""
                {"isActive": false}
                """)).andExpect(status().isConflict());
    }

    private static TraineeResponse traineeResponse(String username) {
        return TraineeResponse.builder().username(username).firstName("John").lastName("Doe").address(
                "Main Street").isActive(true).build();
    }
}
