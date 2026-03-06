package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import com.devpro.devlearningroadmapmanager.entities.Rubrique;
import com.devpro.devlearningroadmapmanager.mappers.RubriqueMapper;
import com.devpro.devlearningroadmapmanager.repositories.RubriqueRepository;
import com.devpro.devlearningroadmapmanager.service.IRubriqueService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RubriqueServiceImpl implements IRubriqueService {

    private final RubriqueRepository rubriqueRepository;
    private final RubriqueMapper rubriqueMapper;

    @Override
    public List<RubriqueDto> findAllRubriques(Long id, String slug) {
        Specification<Rubrique> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (StringUtils.hasText(slug)) {
                predicates.add(cb.equal(root.get("slug"), slug));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return rubriqueRepository.findAll(specification).stream()
                .map(rubriqueMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RubriqueDto saveRubrique(Long id, RubriqueDto.RubriqueSaveDto dto) {
        Rubrique rubrique;

        if (id == null) {
            rubrique = rubriqueMapper.toEntity(dto);

        } else {
            rubrique = rubriqueRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Rubrique non trouvée"));
            rubriqueMapper.partialUpdate(dto, rubrique);
        }
        rubrique.setSlug(dto.nom().toLowerCase().trim().replaceAll("\\s+", "-"));
        return rubriqueMapper.toDto(rubriqueRepository.save(rubrique));
    }

    @Override
    @Transactional
    public void deleteRubrique(Long id) {
        if (!rubriqueRepository.existsById(id)) {
            throw new EntityNotFoundException("Impossible de supprimer : Rubrique inexistante");
        }
        rubriqueRepository.deleteById(id);
    }
}