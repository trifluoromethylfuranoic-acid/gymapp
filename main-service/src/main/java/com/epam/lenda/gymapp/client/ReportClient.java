package com.epam.lenda.gymapp.client;

import com.epam.lenda.gymapp.dto.event.TrainingActionEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "report-service", contextId = "reportClient", path = "/api/v1")
public interface ReportClient {
    @PostMapping("/reports")
    void recordTraining(@RequestBody TrainingActionEvent event);
}
