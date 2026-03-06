package com.devpro.devlearningroadmapmanager.repositories;

import com.devpro.devlearningroadmapmanager.entities.Rubrique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RubriqueRepository extends JpaRepository<Rubrique, Long>, JpaSpecificationExecutor<Rubrique> {
    Optional<Rubrique> findBySlug(String slug);
}
