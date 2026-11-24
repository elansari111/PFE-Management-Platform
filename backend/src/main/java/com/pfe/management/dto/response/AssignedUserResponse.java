package com.pfe.management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informations sur l'utilisateur assigné")
public class AssignedUserResponse {

    @Schema(description = "Identifiant de l'utilisateur", example = "5")
    private Long id;

    @Schema(description = "Nom complet de l'utilisateur", example = "Jean Dupont")
    private String fullName;

    public AssignedUserResponse() {
    }

    public AssignedUserResponse(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
