package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.cloud.service.CloudinaryService;
import com.devpro.devlearningroadmapmanager.dtos.ArticleSectionDto;
import com.devpro.devlearningroadmapmanager.entities.Article;
import com.devpro.devlearningroadmapmanager.entities.ArticleSection;
import com.devpro.devlearningroadmapmanager.repositories.ArticleRepository;
import com.devpro.devlearningroadmapmanager.repositories.ArticleSectionRepository;
import com.devpro.devlearningroadmapmanager.service.IArticleSectionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleSectionServiceImpl implements IArticleSectionService {

    private final ArticleSectionRepository sectionRepository;
    private final ArticleRepository articleRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<ArticleSectionDto> findSectionsByArticleId(Long articleId) {
        return sectionRepository.findByArticleIdOrderByOrdreAsc(articleId)
                .stream()
                .map(s -> new ArticleSectionDto(s.getId(), s.getTitre() ,s.getContenu(), s.getImage(), s.getOrdre()))
                .toList();
    }

    @Override
    @Transactional
    public ArticleSectionDto saveSection(Long articleId, ArticleSectionDto dto, MultipartFile image) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));

        ArticleSection section;

        if (dto.id() == null) {
            section = new ArticleSection();
            section.setArticle(article);
        } else {
            section = sectionRepository.findById(dto.id())
                    .orElseThrow(() -> new EntityNotFoundException("Section non trouvée"));
        }

        section.setTitre(dto.titre());
        section.setContenu(dto.contenu());
        section.setOrdre(dto.ordre());

        if (image != null && !image.isEmpty()) {
            if (section.getImage() != null) {
                cloudinaryService.deleteImage(section.getImage());
            }
            String imageUrl = cloudinaryService.uploadImage(image);
            section.setImage(imageUrl);
        }

        ArticleSection saved = sectionRepository.save(section);
        return new ArticleSectionDto(saved.getId(), saved.getTitre() ,saved.getContenu(), saved.getImage(), saved.getOrdre());
    }

    @Override
    @Transactional
    public void deleteSectionsByArticleId(Long articleId) {
        List<ArticleSection> sections = sectionRepository.findByArticleIdOrderByOrdreAsc(articleId);
        sections.forEach(s -> {
            if (s.getImage() != null) cloudinaryService.deleteImage(s.getImage());
        });
        sectionRepository.deleteByArticleId(articleId);
    }

    @Override
    @Transactional
    public void deleteSection(Long sectionId) {
        ArticleSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section non trouvée"));
        if (section.getImage() != null) cloudinaryService.deleteImage(section.getImage());
        sectionRepository.deleteById(sectionId);
    }
}