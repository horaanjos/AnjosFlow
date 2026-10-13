package com.anjosflow.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
        @NotBlank(message = "O nome da disciplina é obrigatório.")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
        String name,
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String description,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "A cor deve estar no formato #RRGGBB.")
        String color) {
    public SubjectRequest {
        // Normaliza antes da validação: ' A ' continua inválido após remover espaços.
        name = name == null ? null : name.strip();
    }
}