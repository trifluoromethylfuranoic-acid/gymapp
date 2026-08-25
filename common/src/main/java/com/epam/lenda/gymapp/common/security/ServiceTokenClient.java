package com.epam.lenda.gymapp.common.security;

import com.netflix.discovery.EurekaClient;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Fetches this service's own client_credentials token and caches it,
 * refreshing shortly before expiry rather than on every outbound call.
 */
@Component
public class ServiceTokenClient {

    private final RestTemplate restTemplate;
    private final EurekaClient eurekaClient;
    private final String authServerServiceName;
    private final String authServerTokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final AtomicReference<CachedToken> cache = new AtomicReference<>();

    public ServiceTokenClient(
            RestTemplate restTemplate, EurekaClient eurekaClient,
            @Value("${application.security.authServer.name:main-service}") String authServerServiceName,
            @Value("${application.security.authServer.tokenEndpoint:/oauth2/token}") String authServerTokenEndpoint,
            @Value("${spring.application.name}") String clientId,
            @Value("${application.security.clients.current.secret}") String clientSecret,
            @Value("${application.security.interserviceToken.scope:internal.call}") String scope) {
        this.restTemplate = restTemplate;
        this.eurekaClient = eurekaClient;
        this.authServerServiceName = authServerServiceName;
        this.authServerTokenEndpoint = authServerTokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    public String getToken() {
        final var cached = cache.get();
        if (cached != null && cached.isStillValid()) {
            return cached.token;
        }
        return refresh();
    }

    private synchronized String refresh() {
        final var cached = cache.get();
        if (cached != null && cached.isStillValid()) {
            return cached.token;
        }

        final var basicAuth = Base64.getEncoder()
                                 .encodeToString((clientId + ":" + clientSecret).getBytes());

        final var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        final var authServerInstance = eurekaClient.getApplication(authServerServiceName).getInstances().get(0);
        final var authServerHost = authServerInstance.getHostName();
        final var authServerPort = authServerInstance.getPort();
        final var url = "http://" + authServerHost + ":" + authServerPort + authServerTokenEndpoint;

        final var requestEntity = RequestEntity
                .post(url)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form);

        final var  response = restTemplate.postForObject(url, requestEntity, TokenResponse.class);

        if (response == null) {
            throw new IllegalStateException("Failed to obtain service token for client " + clientId);
        }

        // refresh at 80% of the token's lifetime, not right at expiry
        final var expiresAt = Instant.now().plusSeconds((long) (response.expires_in * 0.8));
        final var  next = new CachedToken(response.access_token, expiresAt);
        cache.set(next);
        return next.token;
    }

    private record CachedToken(String token, Instant refreshAt) {
        boolean isStillValid() {
            return Instant.now().isBefore(refreshAt);
        }
    }

    record TokenResponse(String access_token, long expires_in) {
    }
}
