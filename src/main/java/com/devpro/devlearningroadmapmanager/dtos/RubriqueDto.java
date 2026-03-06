package com.devpro.devlearningroadmapmanager.dtos;

import jakarta.validation.constraints.NotBlank;

public record RubriqueDto(
        Long id,
        String nom,
        String slug,
        String description
) {
    public record RubriqueSaveDto(
            @NotBlank(message = "Le nom de la rubrique est requis")
            String nom,
            String description
    ) {}
}
