package com.epam.lenda.gymapp.dto.event;

import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record TrainingActionEvent(
        TrainerInfo trainer,
        ZonedDateTime datetime,
        long durationMinutes,
        Action action) {

    public enum Action {
        CREATE, DELETE
    }

    @Builder
    public record TrainerInfo(String username, String firstName, String lastName, boolean isActive) {
    }
}
