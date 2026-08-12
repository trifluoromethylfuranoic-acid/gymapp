package com.epam.lenda.gymapp.controller.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.lenda.gymapp.TestSecurityConfig;
import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.service.TrainingTypeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrainingTypeController.class)
@Import({TestSecurityConfig.class})
@ActiveProfiles("test")
class TrainingTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingTypeService trainingTypeService;

    @Test
    void getTrainingTypes_returnsAvailableTypes() throws Exception {
        final var fitness = new TrainingType("Fitness");
        final var yoga = new TrainingType("Yoga");
        when(trainingTypeService.findAll()).thenReturn(List.of(fitness, yoga));

        mockMvc.perform(get("/api/v1/training-types"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0]").value("Fitness"))
               .andExpect(jsonPath("$[1]").value("Yoga"));
    }
}
