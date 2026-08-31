package com.epam.lenda.gymapp.report.controller.rest;

import com.epam.lenda.gymapp.common.security.UserPrincipal;
import com.epam.lenda.gymapp.report.dto.TrainerResponse;
import com.epam.lenda.gymapp.report.mapper.TrainerMapper;
import com.epam.lenda.gymapp.report.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReportingController {
    private final ReportingService reportingService;
    private final TrainerMapper trainerMapper;

    @GetMapping("/reports/{username}")
    @PreAuthorize("hasRole('ADMIN') || (#user != null && #user.username == #username)")
    public TrainerResponse getReport(@AuthenticationPrincipal UserPrincipal user,
                                     @PathVariable String username,
                                     @RequestParam(required = false) Integer year) {
        final var trainer = reportingService.getTrainer(username);
        final var records = year == null ?
                reportingService.getRecordsForTrainer(username) :
                reportingService.getRecordsForTrainerByYear(username, year);

        return trainerMapper.toDto(trainer, records);
    }

}
