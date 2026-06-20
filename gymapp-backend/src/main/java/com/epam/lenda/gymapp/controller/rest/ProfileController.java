package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.dto.auth.UserDetails;
import com.epam.lenda.gymapp.model.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
public class ProfileController {
    @RequestMapping("/profile/{*path}")
    public String profile(@PathVariable String path, @AuthenticationPrincipal UserDetails user) {
        if (user.getRole() == Role.TRAINEE) {
            return "forward:/api/v1/trainees/" + user.getUsername() + path;
        } else {
            return "forward:/api/v1/trainers/" + user.getUsername() + path;
        }
    }
}