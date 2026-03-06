package com.devpro.devlearningroadmapmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rubriques")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rubrique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;
}
