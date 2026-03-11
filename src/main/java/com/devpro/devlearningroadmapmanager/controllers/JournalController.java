package com.devpro.devlearningroadmapmanager.controllers;

import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import com.devpro.devlearningroadmapmanager.dtos.ArticleSectionDto;
import com.devpro.devlearningroadmapmanager.dtos.JournalPdfDto;
import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import com.devpro.devlearningroadmapmanager.email.dto.MessageContact;
import com.devpro.devlearningroadmapmanager.email.service.IEmailService;
import com.devpro.devlearningroadmapmanager.service.IArticleSectionService;
import com.devpro.devlearningroadmapmanager.service.IArticleService;
import com.devpro.devlearningroadmapmanager.service.IJournalPdfService;
import com.devpro.devlearningroadmapmanager.service.IRubriqueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/v1/journal-manager")
@RequiredArgsConstructor
@Validated
@Tag(name = "Journal Manager", description = "Gestion centralisée des articles, journaux PDF, rubriques et contacts")
public class JournalController {

    private final IArticleService articleService;
    private final IArticleSectionService articleSectionService;
    private final ObjectMapper objectMapper;
    private final IJournalPdfService journalService;
    private final IRubriqueService rubriqueService;
    private final IEmailService messageService;

        // <editor-fold defaultstate="collapsed" desc="ARTICLES">

        @Operation(summary = "Lister les articles", description = "Recherche multi-critères des articles")
        @GetMapping("/articles")
        public ResponseEntity<Page<ArticleDto>> getAllArticles(
                @RequestParam(required = false) Long id,
                @RequestParam(required = false) Long rubriqueId,
                @RequestParam(required = false) String search,
                @RequestParam(required = false) String statut,
                @RequestParam(required = false) Instant dateDebut,
                @RequestParam(required = false) Instant dateFin,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size
        ) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("datePublication").descending());
            return ResponseEntity.ok(articleService.findArticles(id, rubriqueId, search, statut, dateDebut, dateFin, pageable));
        }

        @GetMapping("/articles/{slug}")
        public ResponseEntity<ArticleDto> getArticleBySlug(@PathVariable String slug) {
            return ResponseEntity.ok(articleService.getArticleBySlug(slug));
        }

        @PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ArticleDto> createArticle(
                @Valid @ModelAttribute @ParameterObject ArticleDto.ArticleSaveDto article,
                @RequestPart(required = false, name = "image") MultipartFile image,
                @RequestPart(required = false, name = "sections") String sectionsJson,
                @RequestPart(required = false, name = "sectionImages") List<MultipartFile> sectionImages
        ) throws JsonProcessingException {
            List<ArticleSectionDto> sections = parseSections(sectionsJson);
            return new ResponseEntity<>(articleService.saveArticle(null, article, image, sections, sectionImages), HttpStatus.CREATED);
        }

        @PatchMapping(value = "/articles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ArticleDto> updateArticle(
                @PathVariable Long id,
                @Valid @ModelAttribute @ParameterObject ArticleDto.ArticleSaveDto article,
                @RequestPart(required = false, name = "image") MultipartFile image,
                @RequestPart(required = false, name = "sections") String sectionsJson,
                @RequestPart(required = false, name = "sectionImages") List<MultipartFile> sectionImages
        ) throws JsonProcessingException {
            List<ArticleSectionDto> sections = parseSections(sectionsJson);
            return ResponseEntity.ok(articleService.saveArticle(id, article, image, sections, sectionImages));
        }

        @DeleteMapping("/articles/{id}")
        public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
            articleService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        }

    // <editor-fold defaultstate="collapsed" desc="ARTICLE SECTIONS">

        @GetMapping("/articles/{articleId}/sections")
        public ResponseEntity<List<ArticleSectionDto>> getSectionsByArticle(@PathVariable Long articleId) {
            return ResponseEntity.ok(articleSectionService.findSectionsByArticleId(articleId));
        }

        @PostMapping(value = "/articles/{articleId}/sections", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ArticleSectionDto> createSection(
                @PathVariable Long articleId,
                @RequestPart(name = "section") String sectionJson,
                @RequestPart(required = false, name = "image") MultipartFile image
        ) throws JsonProcessingException {
            ArticleSectionDto dto = objectMapper.readValue(sectionJson, ArticleSectionDto.class);
            return new ResponseEntity<>(articleSectionService.saveSection(articleId, dto, image), HttpStatus.CREATED);
        }

        @PatchMapping(value = "/articles/{articleId}/sections/{sectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ArticleSectionDto> updateSection(
                @PathVariable Long articleId,
                @PathVariable Long sectionId,
                @RequestPart(name = "section") String sectionJson,
                @RequestPart(required = false, name = "image") MultipartFile image
        ) throws JsonProcessingException {
            ArticleSectionDto dto = objectMapper.readValue(sectionJson, ArticleSectionDto.class);
            // On force l'id depuis le path pour éviter les incohérences
            dto = new ArticleSectionDto(sectionId, dto.contenu(), dto.image(), dto.ordre());
            return ResponseEntity.ok(articleSectionService.saveSection(articleId, dto, image));
        }

        @DeleteMapping("/articles/{articleId}/sections/{sectionId}")
        public ResponseEntity<Void> deleteSection(@PathVariable Long articleId, @PathVariable Long sectionId) {
            articleSectionService.deleteSection(sectionId);
            return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/articles/{articleId}/sections")
        public ResponseEntity<Void> deleteAllSections(@PathVariable Long articleId) {
            articleSectionService.deleteSectionsByArticleId(articleId);
            return ResponseEntity.noContent().build();
        }

    // </editor-fold>
    // </editor-fold>



    // <editor-fold defaultstate="collapsed" desc="JOURNAUX PDF">

    @Operation(summary = "Lister les journaux", description = "Recherche par ID, Statut, texte ou période")
    @GetMapping("/journal")
    public ResponseEntity<Page<JournalPdfDto>> findJournals(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Instant dateDebut,
            @RequestParam(required = false) Instant dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateAjout").descending());
        return ResponseEntity.ok(journalService.findJournals(id, statut, search, dateDebut, dateFin, pageable));
    }

    @Operation(summary = "Récupérer un journal par son slug")
    @GetMapping("/journal/slug/{slug}")
    public ResponseEntity<JournalPdfDto> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(journalService.getJournalBySlug(slug));
    }

    @Operation(summary = "Créer un nouveau journal avec fichiers")
    @PostMapping(value = "/journal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JournalPdfDto> createJournal(
            @Valid @ModelAttribute @ParameterObject JournalPdfDto.JournalPdfSaveDto journalDto,
            @RequestPart(name = "pdfFile") MultipartFile pdfFile,
            @RequestPart(required = false, name = "coverFile") MultipartFile coverFile
    ) {
        JournalPdfDto created = journalService.saveJournal(null, journalDto, pdfFile, coverFile);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Mettre à jour un journal existant")
    @PatchMapping(value = "/journal/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JournalPdfDto> updateJournal(
            @PathVariable Long id,
            @Valid @ModelAttribute @ParameterObject JournalPdfDto.JournalPdfSaveDto journalDto,
            @RequestPart(required = false, name = "pdfFile") MultipartFile pdfFile,
            @RequestPart(required = false, name = "coverFile") MultipartFile coverFile
    ) {
        JournalPdfDto updated = journalService.saveJournal(id, journalDto, pdfFile, coverFile);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer un journal et ses fichiers")
    @DeleteMapping("/journal/{id}")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        journalService.deleteJournal(id);
        return ResponseEntity.noContent().build();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RUBRIQUES">

    @Operation(summary = "Lister les rubriques", description = "Recherche multi-critères (ID, Slug)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/rubrique")
    public ResponseEntity<Page<RubriqueDto>> getAllRubriques(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(rubriqueService.findAllRubriques(id, search, pageable));
    }

    @Operation(summary = "Créer une nouvelle rubrique")
    @PostMapping("/rubrique")
    public ResponseEntity<RubriqueDto> createRubrique(
            @Valid @RequestBody RubriqueDto.RubriqueSaveDto rubriqueDto
    ) {
        RubriqueDto created = rubriqueService.saveRubrique(null, rubriqueDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier une rubrique existante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rubrique mise à jour"),
            @ApiResponse(responseCode = "404", description = "Rubrique non trouvée")
    })
    @PatchMapping("/rubrique/{id}")
    public ResponseEntity<RubriqueDto> updateRubrique(
            @PathVariable Long id,
            @Valid @RequestBody RubriqueDto.RubriqueSaveDto rubriqueDto
    ) {
        RubriqueDto updated = rubriqueService.saveRubrique(id, rubriqueDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer une rubrique")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Suppression réussie"),
            @ApiResponse(responseCode = "404", description = "Rubrique non trouvée")
    })
    @DeleteMapping("/rubrique/{id}")
    public ResponseEntity<Void> deleteRubrique(@PathVariable Long id) {
        rubriqueService.deleteRubrique(id);
        return ResponseEntity.noContent().build();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="CONTACT">

    @PostMapping("/contact")
    public ResponseEntity<Void> contact(@RequestBody MessageContact message) {
        messageService.sendContactMessage(message);
        return ResponseEntity.ok().build();
    }

    // </editor-fold>

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        byte[] pdfBytes = url.openStream().readAllBytes();

        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        if (!fileName.endsWith(".pdf")) fileName += ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    // --- Utilitaire ---
    private List<ArticleSectionDto> parseSections(String sectionsJson) throws JsonProcessingException {
        if (sectionsJson == null || sectionsJson.isBlank()) return null;
        return objectMapper.readValue(sectionsJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ArticleSectionDto.class));
    }
}