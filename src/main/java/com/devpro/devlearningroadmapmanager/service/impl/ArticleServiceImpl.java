package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.config.FileStorageProperty;
import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import com.devpro.devlearningroadmapmanager.entities.Article;
import com.devpro.devlearningroadmapmanager.entities.Rubrique;
import com.devpro.devlearningroadmapmanager.mappers.ArticleMapper;
import com.devpro.devlearningroadmapmanager.repositories.ArticleRepository;
import com.devpro.devlearningroadmapmanager.repositories.RubriqueRepository;
import com.devpro.devlearningroadmapmanager.service.IArticleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements IArticleService {

    private final ArticleRepository articleRepository;
    private final RubriqueRepository rubriqueRepository;
    private final ArticleMapper articleMapper;
    private final FileStorageProperty fileStorageProperty;

    @Override
    public List<ArticleDto> findArticles(Long id, Long rubriqueId, String slug, String statut) {
        Specification<Article> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (rubriqueId != null) {
                predicates.add(cb.equal(root.get("rubrique").get("id"), rubriqueId));
            }
            if (StringUtils.hasText(slug)) {
                predicates.add(cb.equal(root.get("slug"), slug));
            }
            if (StringUtils.hasText(statut)) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return articleRepository.findAll(specification).stream()
                .map(articleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ArticleDto saveArticle(Long id, ArticleDto.ArticleSaveDto dto, MultipartFile imageFile) {
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
            try {
                String fileName = storeFile(imageFile);
                article.setImage(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors du stockage de l'image", e);
            }
        }
        article.setSlug(generateSlug(dto.titre()));
        article.setDatePublication(Instant.now());
        article = articleRepository.save(article);
        return articleMapper.toDto(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé"));

        if (article.getImage() != null) {
            try {
                Path path = Paths.get(fileStorageProperty.getUploadDir()).resolve(article.getImage());
                Files.deleteIfExists(path);
            } catch (IOException ignored) {}
        }

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

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Le fichier " + fileName + " est introuvable sur le serveur.");
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Erreur lors de la récupération du fichier : " + fileName);
        }
    }

    // --- Utilitaires ---

    private String storeFile(MultipartFile file) throws IOException {
        Path root = Paths.get(fileStorageProperty.getUploadDir()).toAbsolutePath().normalize();
        if (!Files.exists(root)) Files.createDirectories(root);

        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Path target = root.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .concat("-" + UUID.randomUUID().toString().substring(0, 5));
    }
}
