package com.pfe.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête d'ajout d'un membre à un projet")
public class ProjectMemberRequest {

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    @Schema(description = "Identifiant unique de l'utilisateur à associer au projet", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    public ProjectMemberRequest() {
    }

    public ProjectMemberRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
