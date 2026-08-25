package com.epam.lenda.gymapp.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

class ServiceTokenClientTest {
    private static final String TOKEN_ENDPOINT = "/oauth2/token";

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final EurekaClient eurekaClient = mock(EurekaClient.class);
    private ServiceTokenClient client;

    @BeforeEach
    void setUp() {
        final var instance = InstanceInfo.Builder.newBuilder().setInstanceId("i-1").setAppName("MAIN-SERVICE")
                .setHostName("172.19.0.2").setPort(8082).setStatus(InstanceInfo.InstanceStatus.UP).build();
        final var application = new Application();
        application.addInstance(instance);
        when(eurekaClient.getApplication("main-service")).thenReturn(application);

        client = new ServiceTokenClient(restTemplate, eurekaClient, "main-service", TOKEN_ENDPOINT, "report-service",
                "secret", "internal.call");
    }

    @Test
    void refresh_buildsUrlWithScheme_andCachesTheToken() {
        when(restTemplate.postForObject(contains(TOKEN_ENDPOINT), any(), eq(ServiceTokenClient.TokenResponse.class)))
                .thenReturn(new ServiceTokenClient.TokenResponse("token-1", 100));

        assertEquals("token-1", client.getToken());
        assertEquals("token-1", client.getToken());

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, times(1)).postForObject(urlCaptor.capture(), any(HttpEntity.class),
                eq(ServiceTokenClient.TokenResponse.class));
        assertEquals("http://172.19.0.2:8082" + TOKEN_ENDPOINT, urlCaptor.getValue());
    }

    @Test
    void refresh_refreshesOnceCacheExpires() {
        when(restTemplate.postForObject(startsWith("http://"), any(), eq(ServiceTokenClient.TokenResponse.class)))
                .thenReturn(new ServiceTokenClient.TokenResponse("token-1", 0));

        client.getToken();

        when(restTemplate.postForObject(startsWith("http://"), any(), eq(ServiceTokenClient.TokenResponse.class)))
                .thenReturn(new ServiceTokenClient.TokenResponse("token-2", 100));
        assertEquals("token-2", client.getToken());
    }
}
