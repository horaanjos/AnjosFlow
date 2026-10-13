package com.anjosflow.subject;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException() {
        super("Disciplina não encontrada.");
    }
}