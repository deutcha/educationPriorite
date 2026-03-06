package com.devpro.devlearningroadmapmanager.dtos;

import com.devpro.devlearningroadmapmanager.enumeration.StatutJournal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record JournalPdfDto(
        Long id,
        String titre,
        String slug,
        String description,
        String fichierPdf,
        String imageCouverture,
        StatutJournal statut,
        Instant dateAjout
) {
    public record JournalPdfSaveDto(
            @NotBlank(message = "Le titre du journal est requis")
            String titre,
            StatutJournal statut,
            String description
    ) {}
}
