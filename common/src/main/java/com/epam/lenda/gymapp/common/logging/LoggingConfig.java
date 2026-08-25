package com.epam.lenda.gymapp.common.logging;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({LoggingFilter.class, TransactionIdFilter.class})
public class LoggingConfig {
    @Bean
    public RestTemplate restTemplate() {
        final var restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((req, body, exec) -> {
            final var transactionId = MDC.get(TransactionIdFilter.MDC_KEY);
            if(transactionId != null) {
                req.getHeaders().add(TransactionIdFilter.TRANSACTION_ID_HEADER, transactionId);
            }
            return exec.execute(req, body);
        });

        return restTemplate;
    }
}
