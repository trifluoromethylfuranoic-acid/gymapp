package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.Page;
import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.dto.training.CreateTrainingRequest;
import com.epam.lenda.gymapp.dto.training.PatchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.SearchTrainingRequest;
import com.epam.lenda.gymapp.dto.training.TrainingResponse;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping({"", "/"})
    public Page<TrainingResponse> trainings(@ModelAttribute SearchTrainingRequest request,
                                            @PageableDefault Pageable pageable,
                                            @AuthenticationPrincipal UserDetails user) {
        if (user.getRole() == Role.TRAINER) {
            return trainingService.search(request, pageable);
        } else {
            return trainingService.searchForTrainee(request, pageable, user.getUsername());
        }
    }

    @PostMapping({"", "/"})
    @PreAuthorize("#user.username == #request.trainee || #user.username == #request.trainer")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingResponse createTraining(@RequestBody @Valid CreateTrainingRequest request,
                                           @AuthenticationPrincipal UserDetails user) {
        return trainingService.createTraining(request);
    }

    @GetMapping({"/{id}", "/{id}/"})
    @PostAuthorize("hasAuthority('TRAINER') || returnObject.trainee == #user.username")
    public TrainingResponse training(@PathVariable long id, @AuthenticationPrincipal UserDetails user) {
        return trainingService.getTraining(id);
    }

    @PatchMapping({"/{id}", "/{id}/"})
    public TrainingResponse patchTraining(@PathVariable long id, @RequestBody @Valid PatchTrainingRequest request,
                                          @AuthenticationPrincipal UserDetails user) {
        return trainingService.patchTrainingVerifyOwner(id, request, user.getUsername());
    }

    @DeleteMapping({"/{id}", "/{id}/"})
    public void deleteTraining(@PathVariable long id, @AuthenticationPrincipal UserDetails user) {
        trainingService.deleteTrainingVerifyOwner(id, user.getUsername());
    }
}
