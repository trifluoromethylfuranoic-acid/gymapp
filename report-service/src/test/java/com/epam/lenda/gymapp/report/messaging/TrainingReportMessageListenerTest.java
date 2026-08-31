package com.epam.lenda.gymapp.report.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.report.dto.TrainingAction;
import com.epam.lenda.gymapp.report.service.ReportingService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TrainingReportMessageListenerTest {
    @Mock
    private ReportingService reportingService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private TrainingReportMessageListener listener;

    @Test
    void deserializesMessageAndRecordsTraining() {
        final var action = TrainingAction.builder().build();
        when(objectMapper.readValue("payload", TrainingAction.class)).thenReturn(action);
        when(validator.validate(action)).thenReturn(Set.of());

        listener.receive("payload");

        verify(reportingService).recordTraining(action);
    }

    @Test
    void doesNotRecordWhenJsonIsInvalid() {
        when(objectMapper.readValue("payload", TrainingAction.class))
                .thenThrow(mock(JacksonException.class));

        listener.receive("payload");

        verify(reportingService, never()).recordTraining(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doesNotRecordWhenValidationFails() {
        final var action = TrainingAction.builder().build();
        final ConstraintViolation<TrainingAction> violation = mock(ConstraintViolation.class);
        when(objectMapper.readValue("payload", TrainingAction.class)).thenReturn(action);
        when(validator.validate(action)).thenReturn(Set.of(violation));

        listener.receive("payload");

        verify(reportingService, never()).recordTraining(org.mockito.ArgumentMatchers.any());
    }
}
