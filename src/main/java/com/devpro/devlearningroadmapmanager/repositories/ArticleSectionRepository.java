package com.devpro.devlearningroadmapmanager.repositories;

import com.devpro.devlearningroadmapmanager.entities.ArticleSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleSectionRepository extends JpaRepository<ArticleSection, Long> {
    List<ArticleSection> findByArticleIdOrderByOrdreAsc(Long articleId);
    void deleteByArticleId(Long articleId);
}