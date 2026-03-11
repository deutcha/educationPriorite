package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.ArticleSectionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleSectionService {
    List<ArticleSectionDto> findSectionsByArticleId(Long articleId);
    ArticleSectionDto saveSection(Long articleId, ArticleSectionDto dto, MultipartFile image);
    void deleteSectionsByArticleId(Long articleId);
    void deleteSection(Long sectionId);
}
