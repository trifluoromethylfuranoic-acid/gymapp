package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.common.security.UserPrincipal;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Tag(name = "Trainings")
public class TrainingController {
    private final TrainingService trainingService;
    private final TrainingMapper trainingMapper;

    @Operation(summary = "Search trainings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @GetMapping("")
    @Nonnull
    @PreAuthorize("""
            #user != null && (hasRole('ADMIN')
              || #request?.traineeUsername == #user.username
              || #request?.trainerUsername == #user.username)
            """)
    public List<TrainingResponse> getTrainings(@ModelAttribute SearchTrainingRequest request,
                                               @AuthenticationPrincipal UserPrincipal user) {
        return trainingService.search(request).stream().map(trainingMapper::toDto).toList();
    }

    @Operation(summary = "Create new training")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success"),
            @ApiResponse(responseCode = "404",
                    description = "Trainer, trainee or training type not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("""
            #user != null && (hasRole('ADMIN')
              || #request?.trainee == #user.username
              || #request?.trainer == #user.username)
            """)
    public void createTraining(@RequestBody @NotNull @Valid CreateTrainingRequest request,
                               @AuthenticationPrincipal UserPrincipal user) {
        trainingService.create(request);
    }
}
