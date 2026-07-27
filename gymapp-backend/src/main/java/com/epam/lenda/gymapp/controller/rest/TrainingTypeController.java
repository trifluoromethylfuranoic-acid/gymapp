package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.model.TrainingType;
import com.epam.lenda.gymapp.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/training-types")
@RequiredArgsConstructor
@Tag(name = "Training types")
public class TrainingTypeController {
    private final TrainingTypeService trainingTypeService;

    @Operation(summary = "Get available training types")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success"),
    })
    @GetMapping("")
    @Nonnull
    public List<String> getTrainingTypes() {
        return trainingTypeService.findAll().stream().map(TrainingType::getName).toList();
    }
}
