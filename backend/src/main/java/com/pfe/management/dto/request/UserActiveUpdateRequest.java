package com.pfe.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Données pour l'activation ou la désactivation d'un utilisateur")
public class UserActiveUpdateRequest {

    @NotNull(message = "Le statut actif est obligatoire")
    @Schema(description = "Statut d'activation du compte", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean active;

    public UserActiveUpdateRequest() {
    }

    public UserActiveUpdateRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
