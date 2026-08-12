package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.dto.request.CreateTraineeRequest;
import com.epam.lenda.gymapp.dto.request.UpdateActiveStatusRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTraineeRequest;
import com.epam.lenda.gymapp.dto.request.UpdateTrainerListRequest;
import com.epam.lenda.gymapp.dto.response.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.response.SignupResponse;
import com.epam.lenda.gymapp.dto.response.TrainerResponse;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.RefreshTokenService;
import com.epam.lenda.gymapp.service.TraineeService;
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
@RequestMapping("api/v1/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainees")
public class TraineeController {
    private final TraineeService traineeService;
    private final UserMapper userMapper;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Signup as a trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Nonnull
    public SignupResponse createTrainee(@RequestBody @Valid @NotNull CreateTraineeRequest request) {
        final var traineeAndPassword = traineeService.create(request);
        final var username = traineeAndPassword.first().getUser().getUsername();
        final var password = traineeAndPassword.second();
        final var userDetails = userMapper.toUserDetails(traineeAndPassword.first().getUser());
        final var accessToken = accessTokenService.encode(userDetails);
        final var refreshToken = refreshTokenService.create(username).second();

        return SignupResponse.builder().username(username).password(password).accessToken(accessToken).refreshToken(
                refreshToken).build();
    }

    @Operation(summary = "Find trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{username}")
    @Nonnull
    public FullTraineeResponse getTrainee(@PathVariable @Nonnull String username) {
        final var trainee = traineeService.findByUsername(username);
        final var trainers = traineeService.getTrainerList(username);
        return userMapper.toDto(trainee, trainers);
    }

    @Operation(summary = "Update trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content)
    })
    @PutMapping("/{username}")
    @Nonnull
    @PreAuthorize("#user != null && (hasRole('ADMIN') || #user.username == #username)")
    public FullTraineeResponse updateTrainee(@PathVariable @Nonnull String username,
                                             @RequestBody @NotNull @Valid UpdateTraineeRequest request,
                                             @AuthenticationPrincipal GymUserDetails user) {
        final var trainee = traineeService.update(username, request);
        final var trainers = traineeService.getTrainerList(username);
        return userMapper.toDto(trainee, trainers);
    }

    @Operation(summary = "Activate/deactivate trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
            @ApiResponse(responseCode = "409", description = "Trainee is already active/inacive", content = @Content),
    })
    @PatchMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("#user != null && (hasRole('ADMIN') || #user.username == #username)")
    public void updateActiveStatus(@PathVariable @Nonnull String username,
                                   @RequestBody @NotNull @Valid UpdateActiveStatusRequest request,
                                   @AuthenticationPrincipal GymUserDetails user) {
        traineeService.updateActiveStatus(username, request.isActive());
    }

    @Operation(summary = "Delete trainee profile")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    })
    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#user != null && (hasRole('ADMIN') || #user.username == #username)")
    public void deleteTrainee(@PathVariable @Nonnull String username,
                              @AuthenticationPrincipal GymUserDetails user) {
        traineeService.delete(username);
    }

    @Operation(summary = "Get trainee's trainer list", description = "Get list of trainers assigned to a trainee or, " + "with `unassigned` flag, get list of trainers that are not yet assigned to the trainee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    })
    @GetMapping("/{username}/trainers")
    @Nonnull
    public List<TrainerResponse> getAssignedTrainers(@PathVariable @Nonnull String username,
                                                     @RequestParam(defaultValue = "false") boolean unassigned) {
        if (unassigned) {
            return traineeService.findActiveTrainersNotAssignedToTrainee(username).stream().map(
                    userMapper::toDto).toList();
        }

        return traineeService.getTrainerList(username).stream().map(userMapper::toDto).toList();
    }

    @Operation(summary = "Set trainee's trainer list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404",
                    description = "Either the trainee or at least one trainer not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format", content = @Content),
    })
    @PutMapping("/{username}/trainers")
    @Nonnull
    @PreAuthorize("#user != null && (hasRole('ADMIN') || #user.username == #username)")
    public List<TrainerResponse> updateAssignedTrainers(@PathVariable @Nonnull String username,
                                                        @RequestBody @NotNull @Valid UpdateTrainerListRequest request,
                                                        @AuthenticationPrincipal GymUserDetails user) {
        return traineeService.updateTrainerList(username, request.trainers()).stream().map(userMapper::toDto).toList();
    }
}
