package com.devpro.devlearningroadmapmanager.repositories;

import com.devpro.devlearningroadmapmanager.entities.JournalPDF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JournalPdfRepository extends JpaRepository<JournalPDF, Long>, JpaSpecificationExecutor<JournalPDF> {
    Optional<JournalPDF> findBySlug(String slug);
}