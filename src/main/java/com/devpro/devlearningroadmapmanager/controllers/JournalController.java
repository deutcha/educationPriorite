package com.devpro.devlearningroadmapmanager.controllers;

import com.devpro.devlearningroadmapmanager.dtos.ArticleDto;
import com.devpro.devlearningroadmapmanager.dtos.JournalPdfDto;
import com.devpro.devlearningroadmapmanager.dtos.RubriqueDto;
import com.devpro.devlearningroadmapmanager.email.dto.MessageContact;
import com.devpro.devlearningroadmapmanager.email.service.IEmailService;
import com.devpro.devlearningroadmapmanager.service.IArticleService;
import com.devpro.devlearningroadmapmanager.service.IJournalPdfService;
import com.devpro.devlearningroadmapmanager.service.IRubriqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("api/v1/journal-manager")
@RequiredArgsConstructor
@Validated
@Tag(name = "Journal Manager", description = "Gestion centralisée des articles, journaux PDF, rubriques et contacts")
public class JournalController {

    private final IArticleService articleService;
    private final IJournalPdfService journalService;
    private final IRubriqueService rubriqueService;
    private final IEmailService messageService;

    // <editor-fold defaultstate="collapsed" desc="ARTICLES">

    @Operation(summary = "Lister les articles", description = "Recherche multi-critères des articles")
    @GetMapping("/articles")
    public ResponseEntity<List<ArticleDto>> getAllArticles(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long rubriqueId,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String statut
    ) {
        return ResponseEntity.ok(articleService.findArticles(id, rubriqueId, slug, statut));
    }

    @GetMapping("/articles/{slug}")
    public ResponseEntity<ArticleDto> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArticleDto> createArticle(
            @Valid @ModelAttribute @ParameterObject ArticleDto.ArticleSaveDto article,
            @RequestPart(required = false, name = "image") MultipartFile image
    ) {
        return new ResponseEntity<>(articleService.saveArticle(null, article, image), HttpStatus.CREATED);
    }

    @PatchMapping(value = "/articles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArticleDto> updateArticle(
            @PathVariable Long id,
            @Valid @ModelAttribute @ParameterObject ArticleDto.ArticleSaveDto article,
            @RequestPart(required = false, name = "image") MultipartFile image
    ) {
        return ResponseEntity.ok(articleService.saveArticle(id, article, image));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articles/download/{fileName}")
    public ResponseEntity<Resource> downloadIamgeArticle(@PathVariable String fileName) throws IOException {
        Resource resource = articleService.downloadDocument(fileName);

        Path path = resource.getFile().toPath();
        String contentType = Files.probeContentType(path);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="JOURNAUX PDF">

    @Operation(summary = "Lister les journaux", description = "Recherche par ID ou Statut")
    @GetMapping("/journal")
    public ResponseEntity<List<JournalPdfDto>> findJournals(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String statut
    ) {
        return ResponseEntity.ok(journalService.findJournals(id, statut));
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

    @Operation(summary = "Télécharger un fichier (PDF ou couverture)")
    @GetMapping("/journal/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        Resource resource = journalService.downloadDocument(fileName);

        String contentType = fileName.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
    public ResponseEntity<List<RubriqueDto>> getAllRubriques(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String slug
    ) {
        return ResponseEntity.ok(rubriqueService.findAllRubriques(id, slug));
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
}