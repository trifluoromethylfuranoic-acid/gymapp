package com.epam.lenda.gymapp.report.messaging;

import com.epam.lenda.gymapp.messaging.TrainingReportMessaging;
import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.service.ReportingService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingReportMessageListener {
    private final ReportingService reportingService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @JmsListener(destination = TrainingReportMessaging.QUEUE)
    public void receive(String message) {
        final TrainingAction trainingAction;
        try {
            trainingAction = objectMapper.readValue(message, TrainingAction.class);
        } catch (JacksonException exception) {
            log.warn("Discarding training report message because its JSON is invalid", exception);
            return;
        }

        if (trainingAction == null) {
            log.warn("Discarding training report message because its payload is null");
            return;
        }

        final var violations = validator.validate(trainingAction);
        if (!violations.isEmpty()) {
            log.warn("Discarding invalid training report message: {}", violations);
            return;
        }

        reportingService.recordTraining(trainingAction);
    }
}
