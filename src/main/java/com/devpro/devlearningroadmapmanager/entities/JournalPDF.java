package com.devpro.devlearningroadmapmanager.entities;

import com.devpro.devlearningroadmapmanager.enumeration.StatutJournal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "journaux_pdf")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalPDF {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String fichierPdf;
    private String imageCouverture;

    @Enumerated(EnumType.STRING)
    private StatutJournal statut;

    private Instant dateAjout;
}
