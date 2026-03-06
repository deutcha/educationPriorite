package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface IArticleService {
    List<ArticleDto> findArticles(Long id, Long rubriqueId, String slug, String statut);

    ArticleDto saveArticle(Long id, ArticleDto.ArticleSaveDto articleDto, MultipartFile imageFile);

    void deleteArticle(Long id);

    ArticleDto getArticleBySlug(String slug);

    Resource downloadDocument(String fileName) throws IOException;

}