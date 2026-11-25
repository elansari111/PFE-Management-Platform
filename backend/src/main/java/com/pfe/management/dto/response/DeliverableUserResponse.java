package com.pfe.management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informations simplifiées sur un utilisateur associé au livrable")
public class DeliverableUserResponse {

    @Schema(description = "Identifiant de l'utilisateur", example = "3")
    private Long id;

    @Schema(description = "Nom complet de l'utilisateur", example = "Jean Dupont")
    private String fullName;

    public DeliverableUserResponse() {
    }

    public DeliverableUserResponse(Long id, String fullName) {
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
