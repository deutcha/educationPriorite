package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import java.util.List;

public interface IRubriqueService {
    List<RubriqueDto> findAllRubriques(Long id, String slug);

    RubriqueDto saveRubrique(Long id, RubriqueDto.RubriqueSaveDto rubriqueDto);

    void deleteRubrique(Long id);
}