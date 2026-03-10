package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import com.devpro.devlearningroadmapmanager.entities.Rubrique;
import com.devpro.devlearningroadmapmanager.mappers.RubriqueMapper;
import com.devpro.devlearningroadmapmanager.repositories.RubriqueRepository;
import com.devpro.devlearningroadmapmanager.service.IRubriqueService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RubriqueServiceImpl implements IRubriqueService {

    private final RubriqueRepository rubriqueRepository;
    private final RubriqueMapper rubriqueMapper;

    @Override
    public Page<RubriqueDto> findAllRubriques(Long id, String search, Pageable pageable) {
        Specification<Rubrique> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }

            // StringUtils.hasText() : le code vérifie si la chaîne search n'est pas nulle, pas vide,
            // et ne contient pas que des espaces. Si c'est vide, on ignore simplement le filtrage.
            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                // cb.or : c'est ici que la magie opère. Le Predicate combine trois conditions. L'article sera sélectionné si le mot-clé se trouve dans :
                //Le Titre de l'article.
                //Le Slug (l'URL simplifiée).
                // WHERE (titre LIKE %...% OR slug LIKE %...% )'
                Predicate searchPredicate = cb.or(
                        // cb.lower(...). Cela force la valeur stockée en base de données à passer en minuscules avant de la comparer.
                        cb.like(cb.lower(root.get("nom")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)
                );
                predicates.add(searchPredicate);
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return rubriqueRepository.findAll(specification, pageable)
                .map(rubriqueMapper::toDto);
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