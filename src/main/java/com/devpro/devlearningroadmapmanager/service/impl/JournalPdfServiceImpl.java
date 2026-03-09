package com.devpro.devlearningroadmapmanager.service.impl;

import com.devpro.devlearningroadmapmanager.cloud.service.CloudinaryService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<JournalPdfDto> findJournals(Long id, String statut, String search, Instant dateDebut, Instant dateFin, Pageable pageable) {
        Specification<JournalPDF> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }

            if (StringUtils.hasText(statut)) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }

            // Recherche sur titre et slug
            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("titre")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)
                ));
            }

            // Filtre dateDebut
            if (dateDebut != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateAjout"), dateDebut));
            }

            // Filtre dateFin
            if (dateFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateAjout"), dateFin));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return journalRepository.findAll(specification, pageable).map(journalMapper::toDto);
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

        if (pdfFile != null && !pdfFile.isEmpty()) {
            if (id != null && journal.getFichierPdf() != null) {
                cloudinaryService.deletePdf(journal.getFichierPdf());
            }
            String pdfUrl = cloudinaryService.uploadPdf(pdfFile);
            journal.setFichierPdf(pdfUrl);
        }

        if (coverFile != null && !coverFile.isEmpty()) {
            if (id != null && journal.getImageCouverture() != null) {
                cloudinaryService.deleteImage(journal.getImageCouverture());
            }
            String coverUrl = cloudinaryService.uploadCover(coverFile);
            journal.setImageCouverture(coverUrl);
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

        cloudinaryService.deletePdf(journal.getFichierPdf());
        cloudinaryService.deleteImage(journal.getImageCouverture());

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

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .concat("-" + UUID.randomUUID().toString().substring(0, 5));
    }


}