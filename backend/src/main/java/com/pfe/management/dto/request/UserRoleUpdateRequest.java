package com.pfe.management.dto.request;

import com.pfe.management.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Données pour la modification du rôle d'un utilisateur")
public class UserRoleUpdateRequest {

    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Nouveau rôle à attribuer", example = "SUPERVISOR", requiredMode = Schema.RequiredMode.REQUIRED)
    private Role role;

    public UserRoleUpdateRequest() {
    }

    public UserRoleUpdateRequest(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
