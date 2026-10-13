package com.anjosflow;

import static org.assertj.core.api.Assertions.*;

import com.anjosflow.common.time.AppTime;
import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MvpIT {
  @Autowired private TestRestTemplate http;
  @Autowired private ObjectMapper json;
  private final List<String> created = new ArrayList<>();

  @AfterEach
  void cleanupOnlyOwnRecords() {
    for (String path : created.reversed()) {
      var response = http.exchange(path, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
      assertThat(response.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);
    }
  }

  private JsonNode post(String route, Map<String, Object> data) {
    var response = http.postForEntity("/api/" + route, data, JsonNode.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    created.add("/api/" + route + "/" + response.getBody().get("id").asLong());
    return response.getBody();
  }

  private long subject() {
    return post("subjects", Map.of("name", "IT-MVP " + UUID.randomUUID())).get("id").asLong();
  }

  private JsonNode task(long subjectId) {
    return post(
        "tasks",
        Map.of(
            "title",
            "Estudar Java",
            "subjectId",
            subjectId,
            "status",
            "PENDING",
            "priority",
            "HIGH",
            "estimatedMinutes",
            30,
            "dueDate",
            LocalDate.now(AppTime.ZONE).toString()));
  }

  @Test
  void tasksHaveFiltersStatusTransitionsAndRestrictSubjectDeletion() {
    long sub = subject();
    var task = task(sub);
    long id = task.get("id").asLong();
    var period = LocalDate.now(AppTime.ZONE).with(DayOfWeek.MONDAY);
    var goal =
        post(
            "goals",
            Map.of(
                "type",
                "COMPLETED_TASKS",
                "targetValue",
                2,
                "periodStart",
                period.toString(),
                "periodEnd",
                period.plusDays(6).toString(),
                "subjectId",
                sub));
    var completed =
        http.exchange(
            "/api/tasks/" + id + "/status",
            HttpMethod.PATCH,
            new HttpEntity<>(Map.of("status", "COMPLETED")),
            JsonNode.class);
    assertThat(completed.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(completed.getBody().get("completedAt").isTextual()).isTrue();
    var progress = http.getForObject("/api/goals/progress", JsonNode.class);
    for (var item : progress)
      if (item.get("goal").get("id").equals(goal.get("id")))
        assertThat(item.get("currentValue").asLong()).isEqualTo(1);
    var filtered =
        http.getForObject(
            "/api/tasks?subjectId="
                + sub
                + "&status=COMPLETED&priority=HIGH&dueBefore="
                + LocalDate.now(AppTime.ZONE),
            JsonNode.class);
    assertThat(filtered.size()).isEqualTo(1);
    var reopened =
        http.exchange(
            "/api/tasks/" + id + "/status",
            HttpMethod.PATCH,
            new HttpEntity<>(Map.of("status", "PENDING")),
            JsonNode.class);
    assertThat(reopened.getBody().get("completedAt").isNull()).isTrue();
    var reopenedProgress = http.getForObject("/api/goals/progress", JsonNode.class);
    for (var item : reopenedProgress)
      if (item.get("goal").get("id").equals(goal.get("id")))
        assertThat(item.get("currentValue").asLong()).isEqualTo(0);
    assertThat(
            http.exchange("/api/subjects/" + sub, HttpMethod.DELETE, HttpEntity.EMPTY, String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void eventsValidateTimesAndExamsAreProjectedWithoutDuplicateRows() {
    long sub = subject();
    var start = Instant.now().plusSeconds(86400).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    var e =
        post(
            "events",
            Map.of(
                "title",
                "Estudo",
                "subjectId",
                sub,
                "startAt",
                start.toString(),
                "endAt",
                start.plusSeconds(1800).toString(),
                "type",
                "STUDY"));
    var bad =
        http.postForEntity(
            "/api/events",
            Map.of(
                "title",
                "Inválido",
                "startAt",
                start.toString(),
                "endAt",
                start.toString(),
                "type",
                "STUDY"),
            String.class);
    assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    var exam =
        post(
            "exams",
            Map.of(
                "title",
                "Prova",
                "subjectId",
                sub,
                "scheduledAt",
                start.plusSeconds(7200).toString(),
                "status",
                "UPCOMING"));
    var calendar =
        http.getForObject(
            "/api/calendar?from=" + start + "&to=" + start.plusSeconds(86400), JsonNode.class);
    assertThat(calendar.findValuesAsText("id"))
        .contains("event-" + e.get("id").asLong(), "exam-" + exam.get("id").asLong());
    var badScore =
        http.exchange(
            "/api/exams/" + exam.get("id").asLong(),
            HttpMethod.PUT,
            new HttpEntity<>(
                Map.of(
                    "title",
                    "Prova",
                    "scheduledAt",
                    start.toString(),
                    "status",
                    "COMPLETED",
                    "score",
                    11,
                    "maxScore",
                    10)),
            String.class);
    assertThat(badScore.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void sessionsAreIdempotentAndGoalsAndDashboardUseRealAggregates() {
    long sub = subject();
    var task = task(sub);
    var end = Instant.now().minusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    var data =
        Map.<String, Object>of(
            "subjectId",
            sub,
            "taskId",
            task.get("id").asLong(),
            "startedAt",
            end.minusSeconds(1800).toString(),
            "endedAt",
            end.toString(),
            "clientKey",
            UUID.randomUUID().toString());
    var session = post("sessions", data);
    var replay = http.postForEntity("/api/sessions", data, JsonNode.class);
    assertThat(replay.getBody().get("id")).isEqualTo(session.get("id"));
    var mismatch = new HashMap<>(data);
    mismatch.put("notes", "Outros dados");
    assertThat(http.postForEntity("/api/sessions", mismatch, String.class).getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
    var from = end.atZone(AppTime.ZONE).toLocalDate();
    var goal =
        post(
            "goals",
            Map.of(
                "type",
                "STUDY_MINUTES",
                "targetValue",
                60,
                "periodStart",
                from.toString(),
                "periodEnd",
                from.plusDays(6).toString(),
                "subjectId",
                sub));
    var progress = http.getForObject("/api/goals/progress", JsonNode.class);
    var matching = new ArrayList<JsonNode>();
    progress.forEach(
        p -> {
          if (p.get("goal").get("id").equals(goal.get("id"))) matching.add(p);
        });
    assertThat(matching.getFirst().get("currentValue").asLong()).isEqualTo(30);
    assertThat(matching.getFirst().get("percentage").asInt()).isEqualTo(50);
    var dashboard = http.getForObject("/api/dashboard", JsonNode.class);
    assertThat(dashboard.get("timeZone").asText()).isEqualTo("America/Sao_Paulo");
    assertThat(dashboard.get("dailyStudy").size()).isEqualTo(7);
    assertThat(dashboard.get("distribution").findValuesAsText("name"))
        .contains(http.getForObject("/api/subjects/" + sub, JsonNode.class).get("name").asText());
  }

  @Test
  void reviewsAdvanceOncePerDayAndCanBeEdited() {
    long sub = subject();
    var review = post("reviews", Map.of("title", "Revisar Java", "subjectId", sub));
    var path = "/api/reviews/" + review.get("id").asLong();
    var first = http.postForObject(path + "/complete", Map.of(), JsonNode.class);
    var again = http.postForObject(path + "/complete", Map.of(), JsonNode.class);
    assertThat(first.get("reviewCount").asInt()).isEqualTo(1);
    assertThat(first.get("intervalDays").asInt()).isEqualTo(3);
    assertThat(again.get("reviewCount").asInt()).isEqualTo(1);
    var updated =
        http.exchange(
            path,
            HttpMethod.PUT,
            new HttpEntity<>(Map.of("title", "Revisar coleções", "subjectId", sub)),
            JsonNode.class);
    assertThat(updated.getBody().get("title").asText()).isEqualTo("Revisar coleções");
  }

  @Test
  void invalidRelationshipsAndMissingIdsAreReported() {
    assertThat(
            http.postForEntity(
                    "/api/tasks",
                    Map.of(
                        "title",
                        "Teste",
                        "subjectId",
                        Long.MAX_VALUE,
                        "status",
                        "PENDING",
                        "priority",
                        "LOW"),
                    String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
    for (String route : List.of("tasks", "events", "sessions", "exams", "goals", "reviews")) {
      assertThat(
              http.getForEntity("/api/" + route + "/" + Long.MAX_VALUE, String.class)
                  .getStatusCode())
          .isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(http.postForEntity("/api/" + route, Map.of(), String.class).getStatusCode())
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }
  }

  @Test
  void concurrentSessionRetriesProduceOneRecord() throws Exception {
    long sub = subject();
    var end = Instant.now().minusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    var data =
        Map.<String, Object>of(
            "subjectId",
            sub,
            "startedAt",
            end.minusSeconds(1500).toString(),
            "endedAt",
            end.toString(),
            "clientKey",
            UUID.randomUUID().toString());
    var first =
        java.util.concurrent.CompletableFuture.supplyAsync(
            () -> http.postForEntity("/api/sessions", data, JsonNode.class));
    var second =
        java.util.concurrent.CompletableFuture.supplyAsync(
            () -> http.postForEntity("/api/sessions", data, JsonNode.class));
    var a = first.get(15, java.util.concurrent.TimeUnit.SECONDS);
    var b = second.get(15, java.util.concurrent.TimeUnit.SECONDS);
    if (a.getBody() != null && a.getBody().has("id"))
      created.add("/api/sessions/" + a.getBody().get("id").asLong());
    assertThat(a.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(b.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(a.getBody().get("id")).isEqualTo(b.getBody().get("id"));
    assertThat(http.getForObject("/api/sessions?subjectId=" + sub, JsonNode.class).size())
        .isEqualTo(1);
  }
}
