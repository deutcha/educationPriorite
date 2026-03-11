package com.devpro.devlearningroadmapmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_sections")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private String image;

    private Integer ordre;
}
