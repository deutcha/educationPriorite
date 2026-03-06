package com.devpro.devlearningroadmapmanager.mappers;

import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import com.devpro.devlearningroadmapmanager.entities.Rubrique;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RubriqueMapper {

    Rubrique toEntity(RubriqueDto.RubriqueSaveDto request);

    RubriqueDto toDto(Rubrique rubrique);

    List<RubriqueDto> toDtoList(List<Rubrique> rubriques);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Rubrique partialUpdate(RubriqueDto.RubriqueSaveDto request, @MappingTarget Rubrique rubrique);
}
