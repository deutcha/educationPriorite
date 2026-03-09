package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IRubriqueService {
    Page<RubriqueDto> findAllRubriques(Long id, String search, Pageable pageable);

    RubriqueDto saveRubrique(Long id, RubriqueDto.RubriqueSaveDto rubriqueDto);

    void deleteRubrique(Long id);
}