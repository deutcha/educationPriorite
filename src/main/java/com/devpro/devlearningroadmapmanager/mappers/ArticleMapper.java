package com.devpro.devlearningroadmapmanager.mappers;

import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import com.devpro.devlearningroadmapmanager.entities.Article;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ArticleMapper {

    @Mapping(target = "rubrique.id", source = "rubriqueId")
    Article toEntity(ArticleDto.ArticleSaveDto request);

    @Mapping(target = "rubriqueId", source = "rubrique.id")
    @Mapping(target = "rubriqueNom", source = "rubrique.nom")
    ArticleDto toDto(Article article);

    List<ArticleDto> toDtoList(List<Article> articles);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "rubrique.id", source = "rubriqueId")
    Article partialUpdate(ArticleDto.ArticleSaveDto request, @MappingTarget Article article);
}
