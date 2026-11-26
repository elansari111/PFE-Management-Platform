package com.pfe.management.controller;

import com.pfe.management.dto.request.UserActiveUpdateRequest;
import com.pfe.management.dto.request.UserCreateRequest;
import com.pfe.management.dto.request.UserRoleUpdateRequest;
import com.pfe.management.dto.response.ErrorResponse;
import com.pfe.management.dto.response.PageResponse;
import com.pfe.management.dto.response.UserResponse;
import com.pfe.management.entity.Role;
import com.pfe.management.service.UserService;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Utilisateurs", description = "Endpoints d'administration des utilisateurs PFE")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Lister les utilisateurs avec pagination et filtres optionnels")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste paginée des utilisateurs récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé : autorisations insuffisantes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Filtrer par rôle") @RequestParam(required = false) Role role,
            @Parameter(description = "Recherche sur le prénom, le nom et l'email") @RequestParam(required = false) String q,
            @Parameter(description = "Index de la page (0-indexé)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Champ de tri") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction du tri (ASC/DESC)") @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        PageResponse<UserResponse> response = userService.getAllUsers(role, q, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel utilisateur avec un rôle et un mot de passe")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou règle métier enfreinte",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour créer un utilisateur",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Changer le rôle d'un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rôle de l'utilisateur mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Rôle invalide ou règle métier enfreinte",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour modifier ce rôle",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> updateUserRole(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        UserResponse updated = userService.updateUserRole(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Activer ou désactiver un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut d'activation de l'utilisateur mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Statut invalide ou règle métier enfreinte",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentification requise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Droits insuffisants pour modifier ce compte",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> updateUserActive(
            @Parameter(description = "Identifiant de l'utilisateur", required = true) @PathVariable Long id,
            @Valid @RequestBody UserActiveUpdateRequest request
    ) {
        UserResponse updated = userService.updateUserActive(id, request);
        return ResponseEntity.ok(updated);
    }
}
