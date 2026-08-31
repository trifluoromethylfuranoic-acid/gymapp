package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.event.TrainingActionEvent;
import com.epam.lenda.gymapp.messaging.TrainingReportMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class TrainingReportNotifier {
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public void notify(TrainingActionEvent event) {
        jmsTemplate.send(TrainingReportMessaging.QUEUE,
                         session -> session.createTextMessage(objectMapper.writeValueAsString(event)));
    }
}
