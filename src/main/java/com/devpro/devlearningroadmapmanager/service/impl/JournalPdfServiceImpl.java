package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.config.FileStorageProperty;
import com.devpro.devlearningroadmapmanager.dtos.JournalPdfDto;
import com.devpro.devlearningroadmapmanager.entities.JournalPDF;
import com.devpro.devlearningroadmapmanager.mappers.JournalPdfMapper;
import com.devpro.devlearningroadmapmanager.repositories.JournalPdfRepository;
import com.devpro.devlearningroadmapmanager.service.IJournalPdfService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalPdfServiceImpl implements IJournalPdfService {

    private final JournalPdfRepository journalRepository;
    private final JournalPdfMapper journalMapper;
    private final FileStorageProperty fileStorageProperty;

    @Override
    public List<JournalPdfDto> findJournals(Long id, String statut) {
        Specification<JournalPDF> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (StringUtils.hasText(statut)) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return journalRepository.findAll(specification).stream()
                .map(journalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public JournalPdfDto saveJournal(Long id, JournalPdfDto.JournalPdfSaveDto dto, MultipartFile pdfFile, MultipartFile coverFile) {
        JournalPDF journal;

        if (id == null) {
            journal = journalMapper.toEntity(dto);
        } else {
            journal = journalRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Journal non trouvé"));
            journalMapper.partialUpdate(dto, journal);
        }

        try {
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String pdfName = storeFile(pdfFile, "PDF_");
                journal.setFichierPdf(pdfName);
            }

            if (coverFile != null && !coverFile.isEmpty()) {
                String coverName = storeFile(coverFile, "COVER_");
                journal.setImageCouverture(coverName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage des fichiers", e);
        }

        journal.setSlug(generateSlug(dto.titre()));
        journal.setDateAjout(Instant.now());
        journal = journalRepository.save(journal);
        return journalMapper.toDto(journal);
    }

    @Override
    @Transactional
    public void deleteJournal(Long id) {
        JournalPDF journal = journalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Journal non trouvé"));

        deletePhysicalFile(journal.getFichierPdf());
        deletePhysicalFile(journal.getImageCouverture());

        journalRepository.delete(journal);
    }

    @Override
    public JournalPdfDto getJournalBySlug(String slug) {
        return journalRepository.findBySlug(slug)
                .map(journalMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Journal introuvable pour le slug: " + slug));
    }

    @Override
    public Resource downloadDocument(String fileName) throws IOException {
        try {
            Path root = Paths.get(fileStorageProperty.getUploadDir()).toAbsolutePath().normalize();

            Path filePath = root.resolve(fileName).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Le fichier " + fileName + " est introuvable sur le serveur.");
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Erreur lors de la récupération du fichier : " + fileName);
        }
    }

    // --- Méthodes privées utilitaires ---

    private String storeFile(MultipartFile file, String prefix) throws IOException {
        Path root = Paths.get(fileStorageProperty.getUploadDir()).toAbsolutePath().normalize();
        if (!Files.exists(root)) Files.createDirectories(root);

        String fileName = prefix + UUID.randomUUID() + "_" + StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        Path target = root.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void deletePhysicalFile(String fileName) {
        if (fileName != null) {
            try {
                Path path = Paths.get(fileStorageProperty.getUploadDir()).resolve(fileName);
                Files.deleteIfExists(path);
            } catch (IOException ignored) {}
        }
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .concat("-" + UUID.randomUUID().toString().substring(0, 5));
    }


}