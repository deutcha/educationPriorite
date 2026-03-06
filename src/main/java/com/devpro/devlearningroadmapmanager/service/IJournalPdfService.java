package com.devpro.devlearningroadmapmanager.service;

import com.devpro.devlearningroadmapmanager.dtos.JournalPdfDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IJournalPdfService {
    List<JournalPdfDto> findJournals(Long id, String statut);

    JournalPdfDto saveJournal(Long id, JournalPdfDto.JournalPdfSaveDto journalDto, MultipartFile pdfFile, MultipartFile coverFile);

    void deleteJournal(Long id);

    JournalPdfDto getJournalBySlug(String slug);

    Resource downloadDocument(String fileName) throws IOException;
}