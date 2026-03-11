package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.cloud.service.CloudinaryService;
import com.devpro.devlearningroadmapmanager.config.FileStorageProperty;
import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import com.devpro.devlearningroadmapmanager.dtos.ArticleSectionDto;
import com.devpro.devlearningroadmapmanager.entities.Article;
import com.devpro.devlearningroadmapmanager.entities.ArticleSection;
import com.devpro.devlearningroadmapmanager.entities.Rubrique;
import com.devpro.devlearningroadmapmanager.mappers.ArticleMapper;
import com.devpro.devlearningroadmapmanager.repositories.ArticleRepository;
import com.devpro.devlearningroadmapmanager.repositories.ArticleSectionRepository;
import com.devpro.devlearningroadmapmanager.repositories.RubriqueRepository;
import com.devpro.devlearningroadmapmanager.service.IArticleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements IArticleService {

    private final ArticleRepository articleRepository;
    private final RubriqueRepository rubriqueRepository;
    private final ArticleSectionRepository sectionRepository;
    private final ArticleMapper articleMapper;
    private final FileStorageProperty fileStorageProperty;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<ArticleDto> findArticles(Long id, Long rubriqueId, String search, String statut,
                                         Instant dateDebut, Instant dateFin, Pageable pageable) {
        Specification<Article> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) predicates.add(cb.equal(root.get("id"), id));
            if (rubriqueId != null) predicates.add(cb.equal(root.get("rubrique").get("id"), rubriqueId));
            if (StringUtils.hasText(search)) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("titre")), searchTerm),
                        cb.like(cb.lower(root.get("slug")), searchTerm)
                ));
            }
            if (dateDebut != null) predicates.add(cb.greaterThanOrEqualTo(root.get("datePublication"), dateDebut));
            if (dateFin != null) predicates.add(cb.lessThanOrEqualTo(root.get("datePublication"), dateFin));
            if (StringUtils.hasText(statut)) predicates.add(cb.equal(root.get("statut"), statut));

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return articleRepository.findAll(specification, pageable).map(articleMapper::toDto);
    }

    @Override
    @Transactional
    public ArticleDto saveArticle(Long id, ArticleDto.ArticleSaveDto dto, MultipartFile imageFile,
                                  List<ArticleSectionDto> sections, List<MultipartFile> sectionImages) {
        Article article;

        if (id == null) {
            article = articleMapper.toEntity(dto);
        } else {
            article = articleRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));
            articleMapper.partialUpdate(dto, article);
        }

        Rubrique rubrique = rubriqueRepository.findById(dto.rubriqueId())
                .orElseThrow(() -> new EntityNotFoundException("Rubrique non trouvée"));
        article.setRubrique(rubrique);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (article.getImage() != null) cloudinaryService.deleteImage(article.getImage());
            article.setImage(cloudinaryService.uploadImage(imageFile));
        }

        article.setSlug(generateSlug(dto.titre()));
        article.setDatePublication(Instant.now());
        article = articleRepository.save(article);

        // Sections : on supprime les anciennes puis on recrée
        if (sections != null && !sections.isEmpty()) {
            List<ArticleSection> existingSections = sectionRepository.findByArticleIdOrderByOrdreAsc(article.getId());
            existingSections.forEach(s -> {
                if (s.getImage() != null) cloudinaryService.deleteImage(s.getImage());
            });
            sectionRepository.deleteByArticleId(article.getId());

            for (int i = 0; i < sections.size(); i++) {
                ArticleSectionDto sectionDto = sections.get(i);
                ArticleSection section = new ArticleSection();
                section.setArticle(article);
                section.setContenu(sectionDto.contenu());
                section.setOrdre(sectionDto.ordre() != null ? sectionDto.ordre() : i);

                // Image de la section si fournie
                MultipartFile sectionImage = (sectionImages != null && i < sectionImages.size())
                        ? sectionImages.get(i) : null;

                if (sectionImage != null && !sectionImage.isEmpty()) {
                    section.setImage(cloudinaryService.uploadImage(sectionImage));
                } else {
                    // Conserver l'URL existante si pas de nouveau fichier
                    section.setImage(sectionDto.image());
                }

                sectionRepository.save(section);
            }
        }

        return articleMapper.toDto(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));

        // Supprimer les images des sections
        List<ArticleSection> sections = sectionRepository.findByArticleIdOrderByOrdreAsc(id);
        sections.forEach(s -> {
            if (s.getImage() != null) cloudinaryService.deleteImage(s.getImage());
        });
        sectionRepository.deleteByArticleId(id);

        // Supprimer l'image principale
        if (article.getImage() != null) cloudinaryService.deleteImage(article.getImage());

        articleRepository.delete(article);
    }

    @Override
    public ArticleDto getArticleBySlug(String slug) {
        return articleRepository.findBySlug(slug)
                .map(articleMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Slug non trouvé : " + slug));
    }

    @Override
    public Resource downloadDocument(String fileName) throws IOException {
        try {
            Path root = Paths.get(fileStorageProperty.getUploadDir()).toAbsolutePath().normalize();
            Path filePath = root.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) return resource;
            throw new FileNotFoundException("Le fichier " + fileName + " est introuvable.");
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Erreur lors de la récupération du fichier : " + fileName);
        }
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .concat("-" + UUID.randomUUID().toString().substring(0, 5));
    }
}