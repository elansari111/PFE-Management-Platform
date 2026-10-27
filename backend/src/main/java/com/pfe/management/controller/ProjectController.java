package com.pfe.management.controller;

import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.dto.request.ProjectUpdateRequest;
import com.pfe.management.dto.response.ErrorResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.ProjectResponse;
import com.pfe.management.entity.ProjectStatus;
import com.pfe.management.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Endpoints de gestion des projets PFE")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Lister les projets avec pagination et filtre optionnel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des projets récupérée avec succès"),
            @ApiResponse(responseCode = "400", description = "Paramètres de requête invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<ProjectResponse>> getAllProjects(
            @Parameter(description = "Filtre par statut") @RequestParam(required = false) ProjectStatus status,
            @Parameter(description = "Index de la page (0-indexé)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Critère de tri") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction du tri (ASC/DESC)") @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        PageResponse<ProjectResponse> response = projectService.getAllProjects(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les détails d'un projet par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projet trouvé"),
            @ApiResponse(responseCode = "404", description = "Projet introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Identifiant unique du projet", required = true) @PathVariable Long id
    ) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau projet PFE")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projet créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données envoyées invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Un projet avec ce titre existe déjà",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        ProjectResponse created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les informations d'un projet existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projet modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Projet introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflit avec un titre existant",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Identifiant du projet", required = true) @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        ProjectResponse updated = projectService.updateProject(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un projet par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projet supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Projet introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Identifiant du projet", required = true) @PathVariable Long id
    ) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
