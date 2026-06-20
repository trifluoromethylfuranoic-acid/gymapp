package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.trainee.FullTraineeResponse;
import com.epam.lenda.gymapp.dto.trainee.PatchTraineeRequest;
import com.epam.lenda.gymapp.dto.trainee.SearchTraineeRequest;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import com.epam.lenda.gymapp.service.TraineeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Validated
public class TraineeController {
    private final TraineeService traineeService;

    @GetMapping({"", "/"})
    @PreAuthorize("hasAuthority('TRAINER')")
    public Page<TraineeResponse> trainees(@ModelAttribute SearchTraineeRequest request,
                                          @PageableDefault Pageable pageable,
                                          @AuthenticationPrincipal UserDetails user) {
        return traineeService.search(request, pageable);
    }

    @GetMapping({"/{username}", "/{username}/"})
    @PreAuthorize("hasAuthority('TRAINER') || #user.username == #username")
    public FullTraineeResponse traineeProfile(@PathVariable String username,
                                              @AuthenticationPrincipal UserDetails user) {
        return traineeService.getTraineeProfile(username);
    }

    @PatchMapping({"/{username}", "/{username}/"})
    @PreAuthorize("#user.username == #username")
    public FullTraineeResponse patchTraineeProfile(@PathVariable String username,
                                                   @RequestBody @Valid PatchTraineeRequest request,
                                                   @AuthenticationPrincipal UserDetails user) {
        return traineeService.patchTraineeProfile(username, request);
    }

    @DeleteMapping({"/{username}", "/{username}/"})
    @PreAuthorize("#user.username == #username")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrainee(@PathVariable String username, @AuthenticationPrincipal UserDetails user) {
        traineeService.deleteTrainee(username);
    }

    @GetMapping({"/{username}/trainers", "/{username}/trainers/"})
    @PreAuthorize("hasAuthority('TRAINER') || #user.username == #username")
    public List<TrainerResponse> assignedTrainers(@PathVariable String username,
                                                  @AuthenticationPrincipal UserDetails user) {
        return traineeService.getAssignedTrainers(username);
    }

    @PostMapping({"/{traineeUsername}/trainers", "/{traineeUsername}/trainers/"})
    @PreAuthorize("#user.username == #traineeUsername || #user.username == #trainerUsername")
    public List<TrainerResponse> assignTrainer(@PathVariable String traineeUsername,
                                               @RequestBody String trainerUsername,
                                               @AuthenticationPrincipal UserDetails user) {
        return traineeService.assignTrainer(traineeUsername, trainerUsername);
    }

    @DeleteMapping({"/{traineeUsername}/trainers/{trainerUsername}", "/{traineeUsername}/trainers/{trainerUsername}/"})
    @PreAuthorize("#user.username == #traineeUsername || #user.username == #trainerUsername")
    public List<TrainerResponse> unassignTrainer(@PathVariable String traineeUsername,
                                                 @PathVariable String trainerUsername,
                                                 @AuthenticationPrincipal UserDetails user) {
        return traineeService.unassignTrainer(traineeUsername, trainerUsername);
    }
}
