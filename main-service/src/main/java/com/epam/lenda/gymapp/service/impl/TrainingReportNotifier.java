package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.client.ReportClient;
import com.epam.lenda.gymapp.dto.event.TrainingActionEvent;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingReportNotifier {
    public static final String REPORT_SERVICE_CIRCUIT_BREAKER = "report-service";

    private final ReportClient reportClient;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Async
    @Retryable(includes = Exception.class,
            excludes = CallNotPermittedException.class,
            maxRetriesString = "${application.report.maxRetries:4}",
            delayString = "${application.report.backoffMillis:500}",
            multiplierString = "${application.report.multiplier:2}")
    public void notify(TrainingActionEvent event) {
        circuitBreakerFactory
                .create(REPORT_SERVICE_CIRCUIT_BREAKER)
                .run(() -> {
                    reportClient.recordTraining(event);
                    return null;
                });

    }
}
