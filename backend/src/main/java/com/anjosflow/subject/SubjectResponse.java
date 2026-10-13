package com.anjosflow.subject;

import java.time.Instant;

public record SubjectResponse(Long id, String name, String description, String color,
                              Instant createdAt, Instant updatedAt) {
    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getDescription(),
                subject.getColor(), subject.getCreatedAt(), subject.getUpdatedAt());
    }
}