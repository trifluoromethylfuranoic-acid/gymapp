package com.epam.lenda.gymapp.integration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class TrainingReportingSteps {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ScenarioState state;

    @BeforeAll
    public static void startServices() {
        SuiteEnvironment.start();
    }

    @AfterAll
    public static void stopServices() {
        SuiteEnvironment.stop();
    }

    @Before
    public void resetScenario() {
        SuiteEnvironment.clearReports();
        state.clear();
    }

    @Given("a registered trainer and trainee")
    public void registerTrainerAndTrainee() throws Exception {
        final var trainer = postMain("/api/v1/trainers", null, """
                {
                  "firstName": "Cucumber",
                  "lastName": "Trainer",
                  "specialization": "Strength Training"
                }
                """);
        assertEquals(201, trainer.statusCode(), trainer.body());
        final var trainerBody = json(trainer);
        state.trainerUsername = trainerBody.required("username").asString();
        state.trainerAccessToken = trainerBody.required("accessToken").asString();

        final var trainee = postMain("/api/v1/trainees", null, """
                {
                  "firstName": "Cucumber",
                  "lastName": "Trainee",
                  "dateOfBirth": "1990-01-01",
                  "address": "Integration Test Street"
                }
                """);
        assertEquals(201, trainee.statusCode(), trainee.body());
        final var traineeBody = json(trainee);
        state.traineeUsername = traineeBody.required("username").asString();
        state.traineeAccessToken = traineeBody.required("accessToken").asString();
    }

    @When("the trainer creates a 120-minute training two days in the future")
    public void createFutureTraining() throws Exception {
        state.trainingDate = ZonedDateTime.now(ZoneOffset.UTC).plusDays(2).withNano(0);
        final var response = postMain("/api/v1/trainings", state.trainerAccessToken, """
                {
                  "trainee": "%s",
                  "trainer": "%s",
                  "name": "Cucumber reporting training",
                  "type": "Strength Training",
                  "datetime": "%s",
                  "durationMinutes": 120
                }
                """.formatted(state.traineeUsername, state.trainerUsername, state.trainingDate));

        assertEquals(201, response.statusCode(), response.body());
    }

    @When("the trainee is deleted")
    public void deleteTrainee() throws Exception {
        final var response = request("DELETE", SuiteEnvironment.mainServiceUrl() + "/api/v1/trainees/"
                + state.traineeUsername, state.traineeAccessToken, null);
        assertEquals(204, response.statusCode(), response.body());
    }

    @Then("the report eventually contains 2 hours for that training month")
    public void reportEventuallyContainsCreatedDuration() {
        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(200)).untilAsserted(() -> {
            final var report = getReport();
            assertEquals(200, report.statusCode(), report.body());
            final var duration = reportMonth(json(report));
            assertTrue(duration.isNumber(), report.body());
            assertEquals(2L, duration.asLong(), report.body());
        });
    }

    @Then("the report eventually omits that training month")
    public void reportEventuallyOmitsDeletedDuration() {
        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofMillis(200)).untilAsserted(() -> {
            final var report = getReport();
            assertEquals(200, report.statusCode(), report.body());
            assertTrue(reportMonth(json(report)).isMissingNode(), report.body());
        });
    }

    private HttpResponse<String> getReport() throws IOException, InterruptedException {
        return request("GET", SuiteEnvironment.reportServiceUrl() + "/api/v1/reports/" + state.trainerUsername
                + "?year=" + state.trainingDate.getYear(), state.trainerAccessToken, null);
    }

    private HttpResponse<String> postMain(String path, String accessToken, String body)
            throws IOException, InterruptedException {
        return request("POST", SuiteEnvironment.mainServiceUrl() + path, accessToken, body);
    }

    private HttpResponse<String> request(String method, String url, String accessToken, String body)
            throws IOException, InterruptedException {
        final var builder = HttpRequest.newBuilder(URI.create(url));
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        final var publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(
                body);
        return SuiteEnvironment.httpClient().send(builder.method(method, publisher).build(),
                                                  HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode reportMonth(JsonNode report) {
        return report.path("hours")
                     .path(Integer.toString(state.trainingDate.getYear()))
                     .path(monthKey());
    }

    private String monthKey() {
        final var uppercaseMonth = state.trainingDate.getMonth().name();
        return uppercaseMonth.charAt(0) + uppercaseMonth.substring(1).toLowerCase();
    }

    private JsonNode json(HttpResponse<String> response) throws tools.jackson.core.JacksonException {
        return OBJECT_MAPPER.readTree(response.body());
    }
}
