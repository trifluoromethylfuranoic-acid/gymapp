package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.trainee.TraineeResponse;
import com.epam.lenda.gymapp.dto.trainer.FullTrainerResponse;
import com.epam.lenda.gymapp.dto.trainer.PatchTrainerRequest;
import com.epam.lenda.gymapp.dto.trainer.SearchTrainerRequest;
import com.epam.lenda.gymapp.dto.trainer.TrainerResponse;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.TrainerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    @GetMapping({"", "/"})
    public Page<TrainerResponse> trainers(@ModelAttribute SearchTrainerRequest request,
                                          @PageableDefault Pageable pageable) {
        return trainerService.search(request, pageable);
    }

    @GetMapping({"/{username}", "/{username}/"})
    public Object trainerProfile(@PathVariable String username, @AuthenticationPrincipal UserDetails user) {
        if (user.getRole() == Role.TRAINER) {
            return trainerService.getFullTrainerProfile(username);
        } else {
            return trainerService.getTrainerProfile(username);
        }
    }

    @PatchMapping({"/{username}", "/{username}/"})
    @PreAuthorize("#user.username == #username")
    public FullTrainerResponse patchTrainerProfile(@PathVariable String username,
                                                   @RequestBody @Valid PatchTrainerRequest request,
                                                   @AuthenticationPrincipal UserDetails user) {
        return trainerService.patchTrainerProfile(username, request);
    }

    @DeleteMapping({"/{username}", "/{username}/"})
    @PreAuthorize("#user.username == #username")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrainer(@PathVariable String username, @AuthenticationPrincipal UserDetails user) {
        trainerService.deleteTrainer(username);
    }

    @GetMapping({"/{username}/trainees", "/{username}/trainees/"})
    @PreAuthorize("hasAuthority('TRAINER')")
    public List<TraineeResponse> assignedTrainees(@PathVariable String username) {
        return trainerService.getAssignedTrainees(username);
    }

    @PostMapping({"/{trainerUsername}/trainees", "/{trainerUsername}/trainees/"})
    @PreAuthorize("#user.username == #traineeUsername || #user.username == #trainerUsername")
    public List<TraineeResponse> assignTrainee(@PathVariable String trainerUsername,
                                               @RequestBody String traineeUsername,
                                               @AuthenticationPrincipal UserDetails user) {
        return trainerService.assignTrainee(trainerUsername, traineeUsername);
    }

    @DeleteMapping({"/{trainerUsername}/trainees/{traineeUsername}", "/{trainerUsername}/trainees/{traineeUsername}/"})
    @PreAuthorize("#user.username == #traineeUsername || #user.username == #trainerUsername")
    public List<TraineeResponse> unassignTrainee(@PathVariable String trainerUsername,
                                                 @PathVariable String traineeUsername,
                                                 @AuthenticationPrincipal UserDetails user) {
        return trainerService.unassignTrainee(trainerUsername, traineeUsername);
    }
}
