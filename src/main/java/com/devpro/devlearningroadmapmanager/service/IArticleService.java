package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import com.devpro.devlearningroadmapmanager.dtos.ArticleSectionDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface IArticleService {
    Page<ArticleDto> findArticles(Long id, Long rubriqueId, String search, String statut, Instant dateDebut, Instant dateFin, Pageable pageable);

    ArticleDto saveArticle(Long id, ArticleDto.ArticleSaveDto dto, MultipartFile imageFile,
                           List<ArticleSectionDto> sections, List<MultipartFile> sectionImages);

    void deleteArticle(Long id);

    ArticleDto getArticleBySlug(String slug);

    Resource downloadDocument(String fileName) throws IOException;

}