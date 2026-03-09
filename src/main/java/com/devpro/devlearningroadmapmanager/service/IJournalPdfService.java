package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.JournalPdfDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface IJournalPdfService {
    Page<JournalPdfDto> findJournals(Long id, String statut, String search, Instant dateDebut, Instant dateFin, Pageable pageable);

    JournalPdfDto saveJournal(Long id, JournalPdfDto.JournalPdfSaveDto journalDto, MultipartFile pdfFile, MultipartFile coverFile);

    void deleteJournal(Long id);

    JournalPdfDto getJournalBySlug(String slug);

    Resource downloadDocument(String fileName) throws IOException;
}