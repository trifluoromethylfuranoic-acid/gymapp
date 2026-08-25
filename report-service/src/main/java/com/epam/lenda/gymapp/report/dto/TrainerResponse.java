package com.epam.lenda.gymapp.report.dto;

import java.time.Month;
import java.util.Map;
import lombok.Builder;

@Builder
public record TrainerResponse(
        String username,
        String firstName,
        String lastName,
        boolean isActive,
        Map<Integer, Map<Month, Long>> hours
) {
}
