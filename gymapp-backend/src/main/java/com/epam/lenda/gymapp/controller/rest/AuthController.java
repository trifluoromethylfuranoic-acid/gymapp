package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.auth.*;
import com.epam.lenda.gymapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthService authService;

    @PostMapping({"/signup/trainer", "/signup/trainer/"})
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signupTrainer(@Valid @RequestBody SignupTrainerRequest request) {
        return authService.signupTrainer(request);
    }

    @PostMapping({"/signup/trainee", "/signup/trainee/"})
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signupTrainee(@Valid @RequestBody SignupTraineeRequest request) {
        return authService.signupTrainee(request);
    }

    @PostMapping({"/login", "/login/"})
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PatchMapping({"/password", "/password/"})
    public void changePassword(@Valid @RequestBody PasswordChangeRequest request,
                               @AuthenticationPrincipal UserDetails user) {
        authService.changePassword(user.getUsername(), request);
    }
}
