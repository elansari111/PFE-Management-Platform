package com.pfe.management.controller;

import com.pfe.management.dto.request.DeliverableReviewRequest;
import com.pfe.management.dto.response.DeliverableFileResource;
import com.pfe.management.dto.response.DeliverableResponse;
import com.pfe.management.dto.response.ErrorResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.entity.DeliverableStatus;
import com.pfe.management.entity.DeliverableType;
import com.pfe.management.service.DeliverableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ContentDisposition;
import org.springframework.http.InvalidMediaTypeException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/deliverables")
@Tag(name = "Deliverables", description = "Endpoints de gestion et d'upload des livrables de projet PFE")
public class DeliverableController {

    private final DeliverableService deliverableService;

    public DeliverableController(DeliverableService deliverableService) {
        this.deliverableService = deliverableService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Déposer un nouveau livrable pour un projet (upload multipart)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livrable déposé avec succès"),
            @ApiResponse(responseCode = "400", description = "Fichier invalide ou extension non autorisée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour déposer sur ce projet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Projet introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Taille maximale de 20 Mo dépassée",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeliverableResponse> createDeliverable(
            @Parameter(description = "Identifiant du projet", required = true) @RequestParam("projectId") Long projectId,
            @Parameter(description = "Type de livrable", required = true) @RequestParam("type") DeliverableType type,
            @Parameter(description = "Fichier à uploader (max 20 Mo, pdf, docx, pptx, zip, md, txt)", required = true) @RequestParam("file") MultipartFile file
    ) {
        DeliverableResponse response = deliverableService.createDeliverable(projectId, type, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lister les livrables avec pagination et filtres optionnels")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée des livrables récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé aux livrables de ce projet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<DeliverableResponse>> getAllDeliverables(
            @Parameter(description = "Filtrer par identifiant du projet") @RequestParam(required = false) Long projectId,
            @Parameter(description = "Filtrer par statut") @RequestParam(required = false) DeliverableStatus status,
            @Parameter(description = "Index de la page (0-indexé)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction du tri (ASC/DESC)") @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        PageResponse<DeliverableResponse> response = deliverableService.getAllDeliverables(projectId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Télécharger le fichier physique associé à un livrable")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichier téléchargé avec succès"),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé pour ce projet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livrable ou fichier physique introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Resource> downloadDeliverable(
            @Parameter(description = "Identifiant du livrable", required = true) @PathVariable Long id
    ) {
                DeliverableFileResource fileResource = deliverableService.downloadDeliverable(id);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(fileResource.getContentType());
        } catch (InvalidMediaTypeException e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileResource.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(fileResource.getContentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(fileResource.getResource());
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Valider ou refuser un livrable (ADMIN ou SUPERVISOR membre du projet)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livrable révisé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou motif de rejet manquant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour réviser ce livrable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livrable introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeliverableResponse> reviewDeliverable(
            @Parameter(description = "Identifiant du livrable", required = true) @PathVariable Long id,
            @Valid @RequestBody DeliverableReviewRequest request
    ) {
        DeliverableResponse response = deliverableService.reviewDeliverable(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un livrable (ADMIN ou déposant tant que SUBMITTED)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livrable supprimé avec succès"),
            @ApiResponse(responseCode = "400", description = "Impossible de supprimer un livrable déjà révisé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour supprimer ce livrable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livrable introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDeliverable(
            @Parameter(description = "Identifiant du livrable", required = true) @PathVariable Long id
    ) {
        deliverableService.deleteDeliverable(id);
        return ResponseEntity.noContent().build();
    }
}
