package com.devpro.devlearningroadmapmanager.dtos;

import com.devpro.devlearningroadmapmanager.enumeration.StatutArticle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ArticleDto(
        Long id,
        String titre,
        String slug,
        String contenu,
        String image,
        Instant datePublication,
        StatutArticle statut,
        Long rubriqueId,
        String rubriqueNom
) {
    public record ArticleSaveDto(
            @NotBlank(message = "Le titre est obligatoire")
            String titre,
            @NotBlank(message = "Le contenu ne peut pas être vide")
            String contenu,
            @NotNull(message = "Le statut est requis")
            StatutArticle statut,
            @NotNull(message = "La rubrique est obligatoire")
            Long rubriqueId
    ) {}
}
