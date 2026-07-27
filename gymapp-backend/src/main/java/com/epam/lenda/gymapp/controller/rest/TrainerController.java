package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.request.CreateTrainerRequest;
import com.epam.lenda.gymapp.dto.request.UpdateActiveStatusRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTrainerRequest;
import com.epam.lenda.gymapp.dto.response.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.response.SignupResponse;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers")
public class TrainerController {
    private final TrainerService trainerService;
    private final UserMapper userMapper;

    @Operation(summary = "Signup as a trainer")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Success"), @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Nonnull
    public SignupResponse createTrainer(@RequestBody @Valid @NotNull CreateTrainerRequest request) {
        final var traineeAndPassword = trainerService.create(request);
        return new SignupResponse(traineeAndPassword.first().getUser().getUsername(), traineeAndPassword.second());
    }

    @Operation(summary = "Find trainer profile by username")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{username}")
    @Nonnull
    public FullTrainerResponse getTrainer(@PathVariable @Nonnull String username) {
        final var trainer = trainerService.findByUsername(username);
        final var trainees = trainerService.getTraineeList(username);
        return userMapper.toDto(trainer, trainees);
    }

    @Operation(summary = "Update trainer profile")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "404", description = "Not found", content = @Content), @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content)
    })
    @PutMapping("/{username}")
    @Nonnull
    public FullTrainerResponse updateTrainer(@PathVariable @Nonnull String username,
                                             @RequestBody @NotNull @Valid UpdateTrainerRequest request) {
        final var trainer = trainerService.update(username, request);
        final var trainees = trainerService.getTraineeList(username);
        return userMapper.toDto(trainer, trainees);
    }

    @Operation(summary = "Activate/deactivate trainer profile")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "404", description = "Not found", content = @Content), @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content), @ApiResponse(responseCode = "409", description = "Trainer is already active/inacive", content = @Content),
    })
    @PatchMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    public void updateActiveStatus(@PathVariable @Nonnull String username,
                                   @RequestBody @NotNull @Valid UpdateActiveStatusRequest request) {
        trainerService.updateActiveStatus(username, request.isActive());
    }
}
