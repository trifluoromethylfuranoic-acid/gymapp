package com.epam.lenda.gymapp.service;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.lenda.gymapp.client.ReportClient;
import com.epam.lenda.gymapp.dto.event.TrainingActionEvent;
import com.epam.lenda.gymapp.service.impl.TrainingReportNotifier;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "application.report.maxRetries=1",
        "application.report.backoffMillis=10",
        "application.report.multiplier=1",
        "resilience4j.circuitbreaker.instances.report-service.slidingWindowSize=1",
        "resilience4j.circuitbreaker.instances.report-service.minimumNumberOfCalls=1",
        "resilience4j.circuitbreaker.instances.report-service.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.report-service.waitDurationInOpenState=60s"
})
@ActiveProfiles("test")
class TrainingReportNotifierCircuitBreakerTest {
    private static final TrainingActionEvent EVENT = TrainingActionEvent
            .builder()
            .action(TrainingActionEvent.Action.CREATE)
            .build();

    @Autowired
    private TrainingReportNotifier notifier;

    @MockitoBean
    private ReportClient reportClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker(TrainingReportNotifier.REPORT_SERVICE_CIRCUIT_BREAKER).reset();
    }

    @Test
    void opensAfterFailuresAndFailsFastWithoutTouchingTheClient() {
        doThrow(new RuntimeException("report-service down")).when(reportClient).recordTraining(any());

        notifier.notify(EVENT);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(
                () -> verify(reportClient, times(1)).recordTraining(any()));

        notifier.notify(EVENT);

        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3)).untilAsserted(
                () -> verify(reportClient, times(1)).recordTraining(any()));
    }
}
