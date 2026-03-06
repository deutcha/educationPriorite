package com.devpro.devlearningroadmapmanager.entities;

import com.devpro.devlearningroadmapmanager.enumeration.StatutArticle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "articles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private String image;
    private Instant datePublication;

    @Enumerated(EnumType.STRING)
    private StatutArticle statut;

    @ManyToOne
    @JoinColumn(name = "rubrique_id")
    private Rubrique rubrique;
}