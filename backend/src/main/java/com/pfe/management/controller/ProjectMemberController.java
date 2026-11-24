package com.pfe.management.controller;

import com.pfe.management.dto.request.ProjectMemberRequest;
import com.pfe.management.dto.response.ErrorResponse;
import com.pfe.management.dto.response.ProjectMemberResponse;
import com.pfe.management.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@Tag(name = "Project Members", description = "Endpoints de gestion des membres de projet PFE")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping
    @Operation(summary = "Ajouter un membre à un projet")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membre ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Données fournies invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour gérer les membres de ce projet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Projet ou utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "L'utilisateur est déjà membre du projet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjectMemberResponse> addMember(
            @Parameter(description = "Identifiant du projet", required = true) @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberRequest request
    ) {
        ProjectMemberResponse response = projectMemberService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Retirer un membre d'un projet")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membre retiré avec succès"),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour gérer les membres de ce projet",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Projet ou membre introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "Identifiant du projet", required = true) @PathVariable Long projectId,
            @Parameter(description = "Identifiant de l'utilisateur à retirer", required = true) @PathVariable Long userId
    ) {
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Lister les membres d'un projet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des membres récupérée avec succès",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectMemberResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé : vous devez être membre du projet ou administrateur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Projet introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @Parameter(description = "Identifiant du projet", required = true) @PathVariable Long projectId
    ) {
        List<ProjectMemberResponse> members = projectMemberService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }
}
