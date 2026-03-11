package com.devpro.devlearningroadmapmanager.dtos;

public record ArticleSectionDto(
        Long id,
        String titre,
        String contenu,
        String image,
        Integer ordre
) {}
