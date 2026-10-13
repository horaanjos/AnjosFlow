package com.anjosflow.subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubjectIT {
    @Autowired private TestRestTemplate http;
    @Autowired private SubjectRepository repository;
    @Autowired private JdbcTemplate jdbc;
    private final List<Long> createdIds = new ArrayList<>();
    private final String prefix = "IT-" + UUID.randomUUID();

    @AfterEach
    void removeOnlyRecordsCreatedByThisTest() {
        repository.deleteAllById(createdIds);
    }

    @Test
    void persistsCrudAndStableOrderingInPostgreSql() {
        SubjectResponse second = create(prefix + " B", null);
        SubjectResponse first = create("  " + prefix + " A  ", "#abc123");
        SubjectResponse duplicate = create(prefix + " A", null);
        assertThat(first.name()).isEqualTo(prefix + " A");
        assertThat(first.color()).isEqualTo("#abc123");
        assertThat(second.color()).isEqualTo("#A3B18A");
        assertThat(first.createdAt()).isNotNull();
        assertThat(first.updatedAt()).isEqualTo(first.createdAt());
        assertThat(jdbc.queryForObject("SELECT name FROM subjects WHERE id = ?", String.class, first.id()))
                .isEqualTo(first.name());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE version = '2' AND success = true", Integer.class))
                .isEqualTo(1);

        var listing = http.getForEntity("/api/subjects", SubjectResponse[].class);
        assertThat(listing.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listing.getBody()).isNotNull();
        assertThat(Arrays.stream(listing.getBody()).filter(s -> createdIds.contains(s.id())).map(SubjectResponse::id).toList())
                .containsExactly(first.id(), duplicate.id(), second.id());

        var found = http.getForEntity("/api/subjects/" + first.id(), SubjectResponse.class);
        assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(found.getBody()).isEqualTo(first);

        var updated = http.exchange("/api/subjects/" + first.id(), HttpMethod.PUT,
                new HttpEntity<>(new SubjectRequest(prefix + " Atualizada", "Nova descrição", null)), SubjectResponse.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().id()).isEqualTo(first.id());
        assertThat(updated.getBody().createdAt()).isEqualTo(first.createdAt());
        assertThat(updated.getBody().updatedAt()).isAfter(first.updatedAt());
        assertThat(updated.getBody().color()).isEqualTo("#A3B18A");
        var persisted = repository.findById(first.id()).orElseThrow();
        assertThat(persisted.getDescription()).isEqualTo("Nova descrição");
        assertThat(persisted.getUpdatedAt()).isEqualTo(updated.getBody().updatedAt());

        var deleted = http.exchange("/api/subjects/" + first.id(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(deleted.getBody()).isNull();
        assertThat(repository.existsById(first.id())).isFalse();
        assertThat(http.getForEntity("/api/subjects/" + first.id(), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void invalidFieldsReturn400WithoutCreatingSubject() {
        var response = http.postForEntity("/api/subjects", new SubjectRequest(" ", null, "green"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("errors", "name", "color").doesNotContain("stackTrace", "SQLException");
    }

    @Test
    void nonexistentIdReturns404ForEveryOperation() {
        String path = "/api/subjects/9223372036854775807";
        assertThat(http.getForEntity(path, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(path, HttpMethod.PUT, new HttpEntity<>(new SubjectRequest("Java", null, null)), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(path, HttpMethod.DELETE, HttpEntity.EMPTY, String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private SubjectResponse create(String name, String color) {
        var response = http.postForEntity("/api/subjects", new SubjectRequest(name, null, color), SubjectResponse.class);
        if (response.getBody() != null && response.getBody().id() != null) {
            createdIds.add(response.getBody().id());
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().getLocation()).isEqualTo(java.net.URI.create("/api/subjects/" + response.getBody().id()));
        return response.getBody();
    }
}