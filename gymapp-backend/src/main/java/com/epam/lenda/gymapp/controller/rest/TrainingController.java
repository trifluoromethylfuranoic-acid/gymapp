package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.request.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.request.SearchTrainingRequest;
import com.epam.lenda.gymapp.dto.response.TrainingResponse;
import com.epam.lenda.gymapp.mapper.TrainingMapper;
import com.epam.lenda.gymapp.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Tag(name = "Trainings")
public class TrainingController {
    private final TrainingService trainingService;
    private final TrainingMapper trainingMapper;

    @Operation(summary = "Search trainings")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @GetMapping("")
    @Nonnull
    public List<TrainingResponse> getTrainings(@ModelAttribute SearchTrainingRequest request) {
        return trainingService.search(request).stream().map(trainingMapper::toDto).toList();
    }

    @Operation(summary = "Create new training")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Success"), @ApiResponse(responseCode = "404", description = "Trainer, trainee or training type not found", content = @Content), @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTraining(@RequestBody CreateTrainingRequest request) {
        trainingService.create(request);
    }
}
