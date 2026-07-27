package com.epam.lenda.gymapp.controller.rest;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Profile("dev")
public class DummyController {
    @GetMapping("/crash")
    public void crash() {
        throw new RuntimeException("AAA i'm crashing");
    }
}
