package com.epam.lenda.gymapp.integration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.util.Map;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

final class SuiteEnvironment {
    private static final DockerImageName ACTIVEMQ_IMAGE = DockerImageName.parse("apache/activemq:6.3.0");
    private static final DockerImageName MONGODB_IMAGE = DockerImageName.parse("mongo:8.0");
    private static final String ACTIVEMQ_USER = "integration";
    private static final String ACTIVEMQ_PASSWORD = "integration-password";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static GenericContainer<?> activeMq;
    private static MongoDBContainer mongoDb;
    private static ConfigurableApplicationContext mainService;
    private static ConfigurableApplicationContext reportService;
    private static int mainServicePort;
    private static int reportServicePort;

    private SuiteEnvironment() {
    }

    static synchronized void start() {
        if (mainService != null) {
            return;
        }

        activeMq = new GenericContainer<>(ACTIVEMQ_IMAGE)
                .withExposedPorts(61616)
                .withEnv("ACTIVEMQ_CONNECTION_USER", ACTIVEMQ_USER)
                .withEnv("ACTIVEMQ_CONNECTION_PASSWORD", ACTIVEMQ_PASSWORD)
                .waitingFor(Wait.forListeningPort());
        mongoDb = new MongoDBContainer(MONGODB_IMAGE);
        activeMq.start();
        mongoDb.start();

        mainServicePort = availablePort();
        reportServicePort = availablePort();
        final var mainServiceUrl = "http://localhost:" + mainServicePort;
        final var activeMqUrl = "tcp://%s:%d".formatted(activeMq.getHost(), activeMq.getMappedPort(61616));

        mainService = new SpringApplicationBuilder(MainServiceIntegrationApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(commandLineProperties(mainProperties(mainServiceUrl, activeMqUrl)));
        reportService = new SpringApplicationBuilder(com.epam.lenda.gymapp.report.ReportServiceApplication.class,
                                                      IntegrationSupportConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .run(commandLineProperties(reportProperties(mainServiceUrl, activeMqUrl)));
    }

    static synchronized void stop() {
        close(reportService);
        close(mainService);
        if (mongoDb != null) {
            mongoDb.stop();
        }
        if (activeMq != null) {
            activeMq.stop();
        }
        reportService = null;
        mainService = null;
        mongoDb = null;
        activeMq = null;
    }

    static HttpClient httpClient() {
        return HTTP_CLIENT;
    }

    static String mainServiceUrl() {
        return "http://localhost:" + mainServicePort;
    }

    static String reportServiceUrl() {
        return "http://localhost:" + reportServicePort;
    }

    static void clearReports() {
        reportService.getBean(MongoTemplate.class).getDb().drop();
    }

    private static Map<String, Object> mainProperties(String mainServiceUrl, String activeMqUrl) {
        return Map.ofEntries(
                Map.entry("server.port", mainServicePort),
                Map.entry("spring.datasource.url", "jdbc:h2:mem:gymapp-integration;DB_CLOSE_DELAY=-1"),
                Map.entry("spring.datasource.username", "sa"),
                Map.entry("spring.datasource.password", ""),
                Map.entry("spring.jpa.hibernate.ddl-auto", "create-drop"),
                Map.entry("spring.jpa.defer-datasource-initialization", true),
                Map.entry("spring.sql.init.mode", "always"),
                Map.entry("spring.sql.init.data-locations", "classpath:data-init/data.sql"),
                Map.entry("spring.activemq.broker-url", activeMqUrl),
                Map.entry("spring.activemq.user", ACTIVEMQ_USER),
                Map.entry("spring.activemq.password", ACTIVEMQ_PASSWORD),
                Map.entry("spring.mongodb.uri", mongoDb.getReplicaSetUrl("gymapp-report-integration")),
                Map.entry("spring.cloud.discovery.enabled", false),
                Map.entry("eureka.client.enabled", false),
                Map.entry("application.security.accessToken.issuer", mainServiceUrl),
                Map.entry("application.security.accessToken.publicKey", "classpath:keys/test-public.pem"),
                Map.entry("application.security.accessToken.privateKey", "classpath:keys/test-private.pem"),
                Map.entry("application.security.accessToken.keyId", "integration-key"),
                Map.entry("application.security.clients.current.secret", "integration-main-secret"),
                Map.entry("application.security.clients.secrets.gatewayService", "integration-gateway-secret"),
                Map.entry("application.security.clients.secrets.mainService", "integration-main-secret"),
                Map.entry("application.security.clients.secrets.reportService", "integration-report-secret"));
    }

    private static Map<String, Object> reportProperties(String mainServiceUrl, String activeMqUrl) {
        return Map.ofEntries(
                Map.entry("server.port", reportServicePort),
                Map.entry("spring.mongodb.uri", mongoDb.getReplicaSetUrl("gymapp-report-integration")),
                Map.entry("spring.activemq.broker-url", activeMqUrl),
                Map.entry("spring.activemq.user", ACTIVEMQ_USER),
                Map.entry("spring.activemq.password", ACTIVEMQ_PASSWORD),
                Map.entry("spring.jms.listener.auto-startup", true),
                Map.entry("spring.cloud.discovery.enabled", false),
                Map.entry("eureka.client.enabled", false),
                Map.entry("spring.security.oauth2.resourceserver.jwt.issuer-uri", mainServiceUrl),
                Map.entry("application.security.clients.current.secret", "integration-report-secret"));
    }

    private static int availablePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to allocate a local port for an integration service", exception);
        }
    }

    private static String[] commandLineProperties(Map<String, Object> properties) {
        return properties.entrySet().stream()
                         .map(entry -> "--%s=%s".formatted(entry.getKey(), entry.getValue()))
                         .toArray(String[]::new);
    }

    private static void close(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
