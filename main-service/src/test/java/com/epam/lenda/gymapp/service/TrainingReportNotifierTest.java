package com.epam.lenda.gymapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.lenda.gymapp.dto.event.TrainingActionEvent;
import com.epam.lenda.gymapp.messaging.TrainingReportMessaging;
import com.epam.lenda.gymapp.service.impl.TrainingReportNotifier;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TrainingReportNotifierTest {
    private static final TrainingActionEvent EVENT = TrainingActionEvent
            .builder()
            .action(TrainingActionEvent.Action.CREATE)
            .build();

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TrainingReportNotifier notifier;

    @Test
    void sendsTrainingEventAsJsonMessage() throws JMSException {
        when(objectMapper.writeValueAsString(EVENT)).thenReturn("payload");

        notifier.notify(EVENT);

        final var creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(TrainingReportMessaging.QUEUE), creatorCaptor.capture());

        final var session = mock(Session.class);
        final var message = mock(TextMessage.class);
        when(session.createTextMessage("payload")).thenReturn(message);

        assertThat(creatorCaptor.getValue().createMessage(session)).isSameAs(message);
    }
}
